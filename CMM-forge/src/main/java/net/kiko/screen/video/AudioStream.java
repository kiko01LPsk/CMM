package net.kiko.screen.video;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** One ffmpeg process that decodes the sound of a video to 44.1 kHz stereo 16-bit PCM, paced in real time. */
public final class AudioStream implements AutoCloseable {

    public static final int RATE = 44100;

    private final Process process;

    public AudioStream(String ffmpeg, String url, double startSeconds) throws IOException {
        List<String> cmd = new ArrayList<>(List.of(ffmpeg, "-hide_banner", "-loglevel", "error", "-nostdin"));
        if (startSeconds > 0.5D) { cmd.add("-ss"); cmd.add(String.format(java.util.Locale.ROOT, "%.2f", startSeconds)); }
        if (!MediaResolver.isLocal(url)) {
            cmd.addAll(List.of("-reconnect", "1", "-reconnect_streamed", "1", "-reconnect_delay_max", "5", "-user_agent", "Mozilla/5.0"));
        }
        cmd.addAll(List.of("-protocol_whitelist", MediaResolver.isLocal(url) ? "file" : "http,https,tcp,tls,crypto",
                "-i", url, "-vn", "-sn", "-dn", "-f", "s16le", "-ar", String.valueOf(RATE), "-ac", "2", "-"));
        this.process = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.DISCARD).start();
    }

    public InputStream pcm() { return process.getInputStream(); }

    @Override
    public void close() { process.destroyForcibly(); }
}
