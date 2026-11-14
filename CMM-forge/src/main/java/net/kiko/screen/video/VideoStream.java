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

    public VideoStream(String ffmpeg, String url, int width, int height, int fps, double startSeconds) throws IOException {
        this.width = width;
        this.height = height;
        this.front = ByteBuffer.allocateDirect(width * height * 4);
        this.back = ByteBuffer.allocateDirect(width * height * 4);

        List<String> cmd = new ArrayList<>(List.of(ffmpeg, "-hide_banner", "-loglevel", "error", "-nostdin"));
        if (startSeconds > 0.5D) { cmd.add("-ss"); cmd.add(String.format(java.util.Locale.ROOT, "%.2f", startSeconds)); }
        if (!MediaResolver.isLocal(url)) {
            cmd.addAll(List.of("-reconnect", "1", "-reconnect_streamed", "1", "-reconnect_delay_max", "5", "-user_agent", "Mozilla/5.0"));
        }
        cmd.addAll(List.of("-re", "-protocol_whitelist", MediaResolver.isLocal(url) ? "file" : "http,https,tcp,tls,crypto",
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
        try (ReadableByteChannel ch = Channels.newChannel(process.getInputStream())) {
            while (!closed) {
                back.clear();
                while (back.hasRemaining()) {
                    if (ch.read(back) < 0) throw new java.io.EOFException();
                }
                back.flip();
                synchronized (lock) {
                    ByteBuffer t = front;
                    front = back;
                    back = t;
                    newFrame = true;
                }
                gotFrame = true;
            }
        } catch (IOException e) {
            if (!closed) {
                String msg;
                synchronized (lastError) { msg = lastError.toString().strip(); }
                if (!gotFrame) error = msg.isEmpty() ? "could not play this video" : lastLine(msg);
                ended = true;
            }
        }
    }

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
