package net.kiko.client.screen;

import net.kiko.KikoMod;
import net.kiko.network.ModNetwork;
import net.kiko.network.MonitorReadyC2SPacket;
import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.video.AudioStream;
import net.kiko.screen.video.MediaResolver;
import net.kiko.screen.video.VideoStream;
import net.kiko.screen.video.VideoTools;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
    volatile long startTick;
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
    private volatile boolean paused;
    private volatile boolean waiting;
    private volatile double pausedAt;
    private int seq;
    private BlockPos controllerPos;
    private boolean sentReady;
    private boolean finished;
    private long lastSpin;
    private int spinStep;
    private long lastErrorShown;
    private final java.nio.ByteBuffer spinBuffer;
    private final int spinSize;

    VideoPlayer(String url, int texW, int texH, long now, LongSupplier gameTime,
                boolean paused, boolean waiting, double pausedAt, long startTick, int seq, BlockPos controller) {
        this.url = url;
        this.startTick = startTick;
        this.paused = paused;
        this.waiting = waiting;
        this.pausedAt = pausedAt;
        this.seq = seq;
        this.controllerPos = controller;
        this.texW = texW;
        this.texH = texH;
        this.lastTouched = now;
        this.gameTime = gameTime;
        this.location = new ResourceLocation(KikoMod.MOD_ID, "video/screen_" + IDS.incrementAndGet());
        this.texture = new VideoTexture(texW, texH);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        texture.clearBlack();
        this.spinSize = Math.max(16, Math.min(Math.min(texW, texH) / 3, 128)) & ~1;
        this.spinBuffer = java.nio.ByteBuffer.allocateDirect(spinSize * spinSize * 4);
        begin(-1.0D);
    }

    boolean matches(String url, int texW, int texH) {
        return this.url.equals(url) && this.texW == texW && this.texH == texH;
    }

    boolean hasPicture() { return hasPicture; }

    /** Called with the controller monitor's state. A new Play / Resume / jump command reloads the stream. */
    void sync(MonitorBlockEntity be, BlockPos controller) {
        this.controllerPos = controller;
        this.startTick = be.getStartTick();
        this.pausedAt = be.getPausedAt();
        this.waiting = be.isWaiting();
        this.paused = be.isPaused();
        if (be.getSeq() != seq) {
            seq = be.getSeq();
            triedStart = false;
            failed = false;
            finished = false;
            sentReady = false;
            status = "";
            stopStreams();
            begin(-1.0D);
        }
    }

    /** Where the video should be right now, in seconds. */
    private double currentOffset() {
        if (paused || waiting) return pausedAt;
        return Math.max(0.0D, (gameTime.getAsLong() - startTick) / 20.0D);
    }

    // ── start / restart ─────────────────────────────────────────────────────

    /** @param forcedOffset seconds to start at, or negative to join in where everybody else is */
    private void begin(double forcedOffset) {
        final int gen = ++generation;
        // the position is decided now, before the (slow) loading, so loading time is not skipped in the video
        final double offset = forcedOffset >= 0.0D ? forcedOffset : currentOffset();
        Thread worker = new Thread(() -> run(gen, offset), "CMM video loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void run(int gen, double forcedOffset) {
        try {
            String ffmpeg = VideoTools.ffmpeg(this::setStatus);
            MediaResolver.Media media = MediaResolver.resolve(url, 720, this::setStatus);
            if (closed || gen != generation) return;
            setStatus("Loading video...");
            double offset = forcedOffset;
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
            MediaResolver.forget(url);
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
            if (v.hasFrame() && waiting && !sentReady && controllerPos != null) {
                // first picture is ready: this starts the screen's clock for everybody
                sentReady = true;
                ModNetwork.CHANNEL.sendToServer(new MonitorReadyC2SPacket(controllerPos));
            }
            // paused: keep the picture, stop decoding and sound
            if (paused && v.hasFrame() && !failed) {
                stopStreams();
                v = null;
            }
        }
        if (v != null) {
            VideoAudio au = audio;
            if (au != null && v.hasFrame() && !paused) au.go = true;
            if (v.ended() && !failed) {
                if (v.hasFrame()) {
                    stopStreams();
                    if (loop) begin(0.0D); else finished = true;
                } else if (loop && !triedStart && v.error() != null && v.error().contains("Nothing was written")) {
                    // joined past the end of a short video: start again from the beginning
                    triedStart = true;
                    stopStreams();
                    begin(0.0D);
                } else {
                    failed = true;
                    MediaResolver.forget(url);
                    setStatus("Monitor: " + (v.error() == null ? "could not play this video" : v.error()));
                    stopStreams();
                }
            }
        }
        VideoStream cur = video;
        boolean buffering = !failed && !finished && (cur == null || !cur.hasFrame()) && !(paused && hasPicture);
        if (!hasPicture || buffering) animatePlaceholder();
        if (failed && distanceToPlayer < 12.0D && System.currentTimeMillis() - lastErrorShown > 5000L && !status.isEmpty()) {
            lastErrorShown = System.currentTimeMillis();
            Minecraft.getInstance().gui.setOverlayMessage(Component.literal(status), false);
        }
        String s = status;
        if (!s.equals(shownStatus)) {
            shownStatus = s;
            if (!s.isEmpty() && distanceToPlayer < 24.0D) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.literal(s), false);
            }
        }
    }

    /** Buffering ring in the middle of the screen (a red cross if the video cannot be played). */
    private void animatePlaceholder() {
        long now = System.currentTimeMillis();
        if (now - lastSpin < 80L) return;
        lastSpin = now;
        spinStep = (spinStep + 1) % 12;
        int n = spinSize;
        double c = (n - 1) / 2.0D;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int r = 0, g = 0, b = 0;
                double dx = x - c, dy = y - c;
                if (failed) {
                    double d = Math.min(Math.abs(dx - dy), Math.abs(dx + dy)) / Math.sqrt(2.0D);
                    if (d < n * 0.04D && Math.abs(dx) < n * 0.32D && Math.abs(dy) < n * 0.32D) { r = 220; g = 50; b = 50; }
                } else {
                    for (int i = 0; i < 12; i++) {
                        double ang = i * Math.PI / 6.0D;
                        double px = Math.sin(ang) * n * 0.38D, py = -Math.cos(ang) * n * 0.38D;
                        double d = Math.hypot(dx - px, dy - py);
                        if (d < n * 0.065D) {
                            int age = (spinStep - i + 12) % 12;
                            int v = 255 - age * 19;
                            r = g = b = Math.max(v, 30);
                        }
                    }
                }
                int o = (y * n + x) * 4;
                spinBuffer.put(o, (byte) r).put(o + 1, (byte) g).put(o + 2, (byte) b).put(o + 3, (byte) 255);
            }
        }
        spinBuffer.position(0);
        texture.uploadRegion((texW - n) / 2, (texH - n) / 2, n, n, spinBuffer);
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
