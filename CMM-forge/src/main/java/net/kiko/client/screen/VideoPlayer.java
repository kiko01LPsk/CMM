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
    final VideoTexture spinTexture;
    final ResourceLocation spinLocation;
    /** True while the buffering / error picture should be drawn over the screen. */
    volatile boolean showSpinner;
    final int spinSize;

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
    private boolean suspendedStreams;
    private double suspendedPos;
    private long suspendedSince;
    private volatile long streamStart;
    private volatile boolean streamSeeks = true;
    private volatile double streamTarget;
    private boolean fellBack;
    private long lastSpin;
    private int spinStep;
    private long lastErrorShown;
    private final java.nio.ByteBuffer spinBuffer;

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
        this.spinLocation = new ResourceLocation(KikoMod.MOD_ID, "video/screen_" + IDS.get() + "_spin");
        this.spinSize = Math.max(16, Math.min(Math.min(texW, texH) / 3, 128)) & ~1;
        this.spinTexture = new VideoTexture(spinSize, spinSize);
        Minecraft.getInstance().getTextureManager().register(spinLocation, spinTexture);
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
            VideoStream held = video;
            if (suspendedStreams && held != null && !failed && Math.abs(be.getPausedAt() - suspendedPos) < 0.5D) {
                // plain Resume: the stream is still open where we stopped, just carry on (no reloading)
                suspendedStreams = false;
                held.setSuspended(false);
                VideoAudio au = audio;
                if (au != null) au.suspended = false;
                sentReady = false;
                return;
            }
            sentReady = false;
            double delta = be.getPausedAt() - (held == null ? 0.0D : held.positionSeconds());
            if (held != null && !failed && !finished && held.hasFrame() && !held.ended()
                    && (be.isWaiting() || be.isPaused()) && delta > 0.3D && delta <= 120.0D) {
                // jump ahead on the open stream: no reopening, the pictures and sound in between are skipped
                held.skip(delta);
                VideoAudio au = audio;
                if (au != null) {
                    au.go = false; // silent until the picture has caught up too
                    au.targetBytes = ((long) ((be.getPausedAt() - au.baseSeconds) * AudioStream.RATE)) * 4L;
                    au.flush = true;
                }
                if (suspendedStreams) suspendedPos = be.getPausedAt();
                return;
            }
            triedStart = false;
            failed = false;
            finished = false;
            fellBack = false;
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
    private void begin(double forcedOffset) { begin(forcedOffset, true); }

    /** @param seek false: do not let ffmpeg jump into the stream (some YouTube streams hang on that), skip ahead instead */
    private void begin(double forcedOffset, boolean seek) {
        final int gen = ++generation;
        // the position is decided now, before the (slow) loading, so loading time is not skipped in the video
        final double offset = forcedOffset >= 0.0D ? forcedOffset : currentOffset();
        Thread worker = new Thread(() -> run(gen, offset, seek), "CMM video loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void run(int gen, double forcedOffset, boolean seek) {
        try {
            String ffmpeg = VideoTools.ffmpeg(this::setStatus);
            MediaResolver.Media media = MediaResolver.resolve(url, 720, this::setStatus);
            if (closed || gen != generation) return;
            setStatus("Loading video...");
            double offset = forcedOffset;
            int fps = (long) texW * texH > 450_000L ? 24 : 30;
            VideoStream v = new VideoStream(ffmpeg, media.videoUrl(), texW, texH, fps, offset, seek);
            AudioStream a = null;
            VideoAudio audioThread = null;
            try {
                a = new AudioStream(ffmpeg, media.audioUrl(), offset, seek);
                audioThread = new VideoAudio(a.pcm());
                audioThread.baseSeconds = seek && offset > 0.5D ? offset : 0.0D;
                if (!seek && offset > 0.5D) audioThread.targetBytes = ((long) (offset * AudioStream.RATE)) * 4L;
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
            streamStart = System.nanoTime();
            streamSeeks = seek;
            streamTarget = offset;
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
        suspendedStreams = false;
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
            if (v.hasFrame() && !v.skipping() && waiting && !sentReady && controllerPos != null) {
                // first picture is ready: this starts the screen's clock for everybody
                sentReady = true;
                ModNetwork.CHANNEL.sendToServer(new MonitorReadyC2SPacket(controllerPos));
            }
            // paused: keep the picture, hold the stream and the sound where they are
            if (paused && v.hasFrame() && !failed) {
                if (!suspendedStreams) {
                    suspendedStreams = true;
                    suspendedPos = pausedAt;
                    suspendedSince = System.nanoTime();
                    v.setSuspended(true);
                    VideoAudio held = audio;
                    if (held != null) held.suspended = true;
                } else if (System.nanoTime() - suspendedSince > 600_000_000_000L) {
                    stopStreams(); // paused for a very long time: let go of the connection
                    v = null;
                }
            }
            // jumping into some streams hangs: after a few seconds open it from the start and skip ahead instead
            if (v != null && !v.hasFrame() && !failed && streamSeeks && streamTarget > 0.5D && !fellBack
                    && System.nanoTime() - streamStart > 8_000_000_000L) {
                fellBack = true;
                stopStreams();
                v = null;
                begin(-1.0D, false);
            }
            // never sit in "buffering" forever
            if (v != null && !v.hasFrame() && !failed && System.nanoTime() - streamStart > 45_000_000_000L) {
                failed = true;
                MediaResolver.forget(url);
                setStatus("Monitor: the video took too long to load");
                stopStreams();
                v = null;
            }
        }
        if (v != null) {
            VideoAudio au = audio;
            if (au != null && v.hasFrame() && !v.skipping() && !suspendedStreams) au.go = true;
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
        boolean buffering = !failed && !finished && (cur == null || !cur.hasFrame() || cur.skipping()) && !(paused && hasPicture && !(cur != null && cur.skipping()));
        showSpinner = !hasPicture || buffering || (failed && !hasPicture);
        if (showSpinner) animatePlaceholder();
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

    private static int[] spinPixels, errorPixels;
    private static int spinSrc, errorSrc;
    private static boolean imagesLoaded;

    private static void loadImages() {
        if (imagesLoaded) return;
        imagesLoaded = true;
        for (int k = 0; k < 2; k++) {
            String name = k == 0 ? "monitor_buffering" : "monitor_error";
            try (var in = Minecraft.getInstance().getResourceManager()
                    .getResourceOrThrow(new ResourceLocation(KikoMod.MOD_ID, "textures/gui/" + name + ".png")).open();
                 com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(in)) {
                int w = img.getWidth(), h = img.getHeight();
                int[] px = new int[w * h];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) px[y * w + x] = img.getPixelRGBA(x, y);
                if (k == 0) { spinPixels = px; spinSrc = w; } else { errorPixels = px; errorSrc = w; }
            } catch (Exception e) {
                KikoMod.LOGGER.warn("Could not load {}: {}", name, e.toString());
            }
        }
    }

    /** Buffering picture (textures/gui/monitor_buffering.png, turning) or the error picture, in the middle of the screen. */
    private void animatePlaceholder() {
        long now = System.currentTimeMillis();
        if (now - lastSpin < 70L) return;
        lastSpin = now;
        spinStep = (spinStep + 1) % 16;
        loadImages();
        int[] src = failed ? errorPixels : spinPixels;
        int srcSize = failed ? errorSrc : spinSrc;
        int n = spinSize;
        double angle = failed ? 0.0D : spinStep * (Math.PI * 2.0D / 16.0D);
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double c = (n - 1) / 2.0D;
        double scale = (double) srcSize / n;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int r = 0, g = 0, b = 0, al = 0;
                if (src != null) {
                    double dx = x - c, dy = y - c;
                    // turn the picture clockwise by 'angle'
                    double sx = (dx * cos + dy * sin) * scale + (srcSize - 1) / 2.0D;
                    double sy = (-dx * sin + dy * cos) * scale + (srcSize - 1) / 2.0D;
                    int ix = (int) Math.round(sx), iy = (int) Math.round(sy);
                    if (ix >= 0 && iy >= 0 && ix < srcSize && iy < srcSize) {
                        int p = src[iy * srcSize + ix];
                        al = p >>> 24;
                        r = p & 0xFF;
                        g = (p >> 8) & 0xFF;
                        b = (p >> 16) & 0xFF;
                    }
                }
                int o = (y * n + x) * 4;
                spinBuffer.put(o, (byte) r).put(o + 1, (byte) g).put(o + 2, (byte) b).put(o + 3, (byte) al);
            }
        }
        spinBuffer.position(0);
        spinTexture.upload(spinBuffer);
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
        spinTexture.releaseId();
        Minecraft.getInstance().getTextureManager().release(spinLocation);
        Minecraft.getInstance().getTextureManager().release(location);
    }
}
