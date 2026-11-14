package net.kiko.client.screen;

import net.kiko.KikoMod;
import net.kiko.screen.video.AudioStream;
import net.kiko.screen.video.MediaResolver;
import net.kiko.screen.video.VideoStream;
import net.kiko.screen.video.VideoTools;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Plays one link for one screen: resolves it, starts the decoder processes, copies new frames into a texture
 * and feeds the sound. Everything slow happens on a worker thread; the game thread only uploads frames.
 */
final class VideoPlayer {

    private static final AtomicInteger IDS = new AtomicInteger();

    final String url;
    final long startTick;
    final int texW, texH;
    final ResourceLocation location;
    final VideoTexture texture;

    volatile boolean loop = true;
    volatile int volume = 50;
    long lastTouched;
    double distanceToPlayer = 64.0D;

    private final LongSupplier gameTime;
    private volatile VideoStream video;
    private volatile VideoAudio audio;
    private volatile AudioStream audioProcess;
    private volatile boolean closed;
    private volatile boolean failed;
    private volatile String status = "";
    private String shownStatus = "";
    private int generation;
    private boolean hasPicture;
    private boolean triedStart;

    VideoPlayer(String url, long startTick, int texW, int texH, long now, LongSupplier gameTime) {
        this.url = url;
        this.startTick = startTick;
        this.texW = texW;
        this.texH = texH;
        this.lastTouched = now;
        this.gameTime = gameTime;
        this.location = new ResourceLocation(KikoMod.MOD_ID, "video/screen_" + IDS.incrementAndGet());
        this.texture = new VideoTexture(texW, texH);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        begin(-1.0D);
    }

    boolean matches(String url, long startTick, int texW, int texH) {
        return this.url.equals(url) && this.startTick == startTick && this.texW == texW && this.texH == texH;
    }

    boolean hasPicture() { return hasPicture; }

    // ── start / restart ─────────────────────────────────────────────────────

    /** @param forcedOffset seconds to start at, or negative to join in where everybody else is */
    private void begin(double forcedOffset) {
        final int gen = ++generation;
        Thread worker = new Thread(() -> run(gen, forcedOffset), "CMM video loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void run(int gen, double forcedOffset) {
        try {
            String ffmpeg = VideoTools.ffmpeg(this::setStatus);
            MediaResolver.Media media = MediaResolver.resolve(url, 720, this::setStatus);
            if (closed || gen != generation) return;
            setStatus("Loading video...");
            double offset = forcedOffset >= 0.0D ? forcedOffset : Math.max(0.0D, (gameTime.getAsLong() - startTick) / 20.0D);
            int fps = (long) texW * texH > 450_000L ? 24 : 30;
            VideoStream v = new VideoStream(ffmpeg, media.videoUrl(), texW, texH, fps, offset);
            AudioStream a = null;
            VideoAudio audioThread = null;
            try {
                a = new AudioStream(ffmpeg, media.audioUrl(), offset);
                audioThread = new VideoAudio(a.pcm());
                audioThread.start();
            } catch (Exception e) {
                KikoMod.LOGGER.warn("Monitor has no sound: {}", e.toString());
            }
            if (closed || gen != generation) {
                v.close();
                if (a != null) a.close();
                if (audioThread != null) audioThread.shutdown();
                return;
            }
            stopStreams();
            this.video = v;
            this.audioProcess = a;
            this.audio = audioThread;
            setStatus("");
        } catch (Exception e) {
            failed = true;
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            KikoMod.LOGGER.warn("Monitor could not play {}: {}", url, message);
            setStatus("Monitor: " + message);
        }
    }

    private void stopStreams() {
        VideoStream v = video;
        VideoAudio au = audio;
        AudioStream ap = audioProcess;
        video = null;
        audio = null;
        audioProcess = null;
        if (v != null) v.close();
        if (au != null) au.shutdown();
        if (ap != null) ap.close();
    }

    private void setStatus(String text) { this.status = text; }

    // ── called by the renderer every frame (game thread) ────────────────────

    void poll() {
        VideoStream v = video;
        if (v != null) {
            if (v.consumeFrame(texture::upload)) hasPicture = true;
            if (v.ended() && !failed) {
                if (v.hasFrame()) {
                    stopStreams();
                    if (loop) begin(0.0D);
                } else if (loop && !triedStart && v.error() != null && v.error().contains("Nothing was written")) {
                    // joined past the end of a short video: start again from the beginning
                    triedStart = true;
                    stopStreams();
                    begin(0.0D);
                } else {
                    failed = true;
                    setStatus("Monitor: " + (v.error() == null ? "could not play this video" : v.error()));
                    stopStreams();
                }
            }
        }
        String s = status;
        if (!s.equals(shownStatus)) {
            shownStatus = s;
            if (!s.isEmpty() && distanceToPlayer < 24.0D) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.literal(s), false);
            }
        }
    }

    void setSound(float gain, float x, float y, float z) {
        VideoAudio a = audio;
        if (a != null) {
            a.gain = gain;
            a.relX = x;
            a.relY = y;
            a.relZ = z;
        }
    }

    void close() {
        closed = true;
        generation++;
        stopStreams();
        texture.releaseId();
        Minecraft.getInstance().getTextureManager().release(location);
    }
}
