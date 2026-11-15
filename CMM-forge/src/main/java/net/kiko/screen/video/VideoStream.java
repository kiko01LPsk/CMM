package net.kiko.screen.video;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs one ffmpeg process that decodes a video into raw RGBA frames of a fixed size (letter-boxed,
 * real-time paced) and hands the newest frame to whoever asks. Two direct buffers are swapped, so the
 * decoder never waits for the game and the game never waits for the decoder.
 */
public final class VideoStream implements AutoCloseable {

    public final int width;
    private final int fps;
    private volatile boolean suspended;
    private final double baseSeconds;
    private volatile long skipFrames;
    private volatile boolean needRebase = true;
    private volatile boolean forcePublish;
    private volatile long consumed;
    public final int height;

    private final Process process;
    private final Thread reader;
    private final Object lock = new Object();
    private ByteBuffer front;
    private ByteBuffer back;
    private boolean newFrame;

    private volatile boolean closed;
    private volatile boolean ended;
    private volatile boolean gotFrame;
    private volatile String error;
    private final StringBuilder lastError = new StringBuilder();

    /**
     * @param seek true: let ffmpeg jump to startSeconds (fast on files); false: open at 0 and throw the pictures
     *             away until startSeconds (slower, but works on streams that cannot be jumped into)
     */
    public VideoStream(String ffmpeg, String url, int width, int height, int fps, double startSeconds, boolean seek) throws IOException {
        this.baseSeconds = seek && startSeconds > 0.5D ? startSeconds : 0.0D;
        if (!(seek && startSeconds > 0.5D) && startSeconds > 0.5D) {
            this.skipFrames = (long) (startSeconds * fps);
        }
        this.width = width;
        this.fps = fps;
        this.height = height;
        this.front = ByteBuffer.allocateDirect(width * height * 4);
        this.back = ByteBuffer.allocateDirect(width * height * 4);

        List<String> cmd = new ArrayList<>(List.of(ffmpeg, "-hide_banner", "-loglevel", "error", "-nostdin"));
        if (seek && startSeconds > 0.5D) { cmd.add("-ss"); cmd.add(String.format(java.util.Locale.ROOT, "%.2f", startSeconds)); }
        if (!MediaResolver.isLocal(url)) {
            cmd.addAll(List.of("-reconnect", "1", "-reconnect_streamed", "1", "-reconnect_delay_max", "5", "-user_agent", "Mozilla/5.0"));
        }
        cmd.addAll(List.of("-protocol_whitelist", MediaResolver.isLocal(url) ? "file" : "http,https,tcp,tls,crypto",
                "-threads", "4", "-i", url, "-an", "-sn", "-dn",
                "-vf", "fps=" + fps + ",scale=" + width + ":" + height
                        + ":force_original_aspect_ratio=decrease:flags=fast_bilinear,pad=" + width + ":" + height
                        + ":(ow-iw)/2:(oh-ih)/2:black,format=rgba",
                "-f", "rawvideo", "-pix_fmt", "rgba", "-"));
        this.process = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.PIPE).start();

        Thread err = new Thread(() -> {
            try (var in = process.getErrorStream()) {
                byte[] buf = new byte[1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    synchronized (lastError) {
                        lastError.append(new String(buf, 0, n));
                        if (lastError.length() > 2000) lastError.delete(0, lastError.length() - 2000);
                    }
                }
            } catch (IOException ignored) {}
        }, "CMM video stderr");
        err.setDaemon(true);
        err.start();

        this.reader = new Thread(this::readLoop, "CMM video decoder");
        this.reader.setDaemon(true);
        this.reader.start();
    }

    private void readLoop() {
        long base = 0L;
        long suspendStart = 0L;
        long index = 0L;
        final long period = 1_000_000_000L / fps;
        try (ReadableByteChannel ch = Channels.newChannel(process.getInputStream())) {
            while (!closed) {
                back.clear();
                while (back.hasRemaining()) {
                    if (ch.read(back) < 0) throw new java.io.EOFException();
                }
                back.flip();
                consumed++;
                if (skipFrames > 0L) { // jumping ahead: decode and drop pictures as fast as they come
                    if (--skipFrames == 0L) { needRebase = true; forcePublish = true; }
                    continue;
                }
                boolean immediate = !gotFrame || needRebase || forcePublish;
                if (needRebase || !gotFrame) {
                    base = System.nanoTime();
                    index = 0L;
                    suspendStart = 0L; // an old pause must not push the clock ahead
                    needRebase = false;
                }
                boolean dropped = false;
                if (immediate) {
                    forcePublish = false;
                } else {
                    // ffmpeg is not paced: frames are shown on time here, and pausing simply stops taking them
                    while (!closed) {
                        long now = System.nanoTime();
                        if (skipFrames > 0L) { // a jump was asked for while this picture was waiting
                            if (--skipFrames == 0L) { needRebase = true; forcePublish = true; }
                            dropped = true;
                            break;
                        }
                        if (suspended) {
                            if (suspendStart == 0L) suspendStart = now;
                            Thread.sleep(20L);
                            continue;
                        }
                        if (suspendStart != 0L) { base += now - suspendStart; suspendStart = 0L; }
                        long due = base + index * period;
                        if (now - due > 400_000_000L) { base = now - index * period; due = now; } // fell behind: do not rush
                        long wait = due - now;
                        if (wait <= 0L) break;
                        Thread.sleep(wait / 1_000_000L, (int) (wait % 1_000_000L));
                    }
                    if (closed) break;
                }
                if (dropped) continue;
                synchronized (lock) {
                    ByteBuffer t = front;
                    front = back;
                    back = t;
                    newFrame = true;
                }
                gotFrame = true;
                index++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (!closed) {
                String msg;
                synchronized (lastError) { msg = lastError.toString().strip(); }
                if (!gotFrame) error = msg.isEmpty() ? "could not play this video" : lastLine(msg);
                ended = true;
            }
        }
    }

    /** Jumps ahead without reopening: the pictures in between are decoded and thrown away. */
    public void skip(double seconds) {
        if (seconds > 0.0D) skipFrames += (long) (seconds * fps);
    }

    public boolean skipping() { return skipFrames > 0L || forcePublish; }

    /** Where in the video the decoder is now, in seconds. */
    public double positionSeconds() { return baseSeconds + consumed / (double) fps; }

    /** Pausing: the stream stays open but no more pictures are shown until resumed. */
    public void setSuspended(boolean value) { suspended = value; }

    public boolean isSuspended() { return suspended; }

    private static String lastLine(String s) {
        String[] lines = s.split("\\R");
        return lines[lines.length - 1];
    }

    /** If a new frame has arrived, gives it to the consumer (RGBA, position 0, limit = size) and returns true. */
    public boolean consumeFrame(Consumer<ByteBuffer> consumer) {
        synchronized (lock) {
            if (!newFrame) return false;
            newFrame = false;
            front.position(0);
            consumer.accept(front);
            return true;
        }
    }

    public boolean hasFrame() { return gotFrame; }
    public boolean ended() { return ended; }
    public String error() { return error; }

    @Override
    public void close() {
        closed = true;
        process.destroyForcibly();
    }
}
