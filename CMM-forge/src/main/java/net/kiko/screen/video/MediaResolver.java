package net.kiko.screen.video;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Turns what the player typed into the address(es) ffmpeg should read. */
public final class MediaResolver {

    /** videoUrl and audioUrl may be the same address (one stream that holds both). */
    public record Media(String videoUrl, String audioUrl) {}

    private MediaResolver() {}

    public static boolean isHttp(String url) {
        try {
            String scheme = URI.create(url.trim()).getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isDirectFile(String url) {
        String path = URI.create(url).getPath();
        if (path == null) return false;
        path = path.toLowerCase(Locale.ROOT);
        for (String ext : new String[]{".mp4", ".webm", ".mkv", ".mov", ".m3u8", ".avi", ".flv", ".ogv", ".mp3", ".ogg", ".wav", ".gif"}) {
            if (path.endsWith(ext)) return true;
        }
        return false;
    }

    /** Blocks (runs yt-dlp for pages like YouTube); call from a worker thread. */
    public static Media resolve(String url, int maxHeight, Consumer<String> status) throws IOException {
        url = url.trim();
        if (!isHttp(url)) throw new IOException("Only http(s) links are supported");
        if (isDirectFile(url)) return new Media(url, url);

        status.accept("Finding the video...");
        String exe = VideoTools.ytdlp(status);
        try {
            return runYtdlp(exe, url, maxHeight, VideoTools.denoIfPresent());
        } catch (IOException first) {
            // YouTube changes all the time: get a newer yt-dlp and the JavaScript helper, then try once more
            boolean updated = VideoTools.forceYtdlpUpdate();
            boolean hadDeno = VideoTools.denoIfPresent() != null;
            if (!updated && hadDeno) throw first; // nothing new to try
            status.accept("Updating YouTube support...");
            exe = VideoTools.ytdlp(status);
            String deno = null;
            try { deno = VideoTools.deno(status); } catch (IOException ignored) {}
            status.accept("Finding the video...");
            try {
                return runYtdlp(exe, url, maxHeight, deno);
            } catch (IOException second) {
                throw second;
            }
        }
    }

    private static Media runYtdlp(String exe, String url, int maxHeight, String deno) throws IOException {
        String format = "bv*[height<=" + maxHeight + "]+ba/b[height<=" + maxHeight + "]/b";
        List<String> cmd = new ArrayList<>(List.of(exe, "--no-playlist", "--no-warnings", "-f", format));
        if (deno != null) { cmd.add("--js-runtimes"); cmd.add("deno:" + deno); }
        cmd.add("-g");
        cmd.add(url);
        Process process = new ProcessBuilder(cmd).redirectErrorStream(false).start();
        StringBuilder err = new StringBuilder();
        Thread errThread = new Thread(() -> {
            try { err.append(new String(process.getErrorStream().readAllBytes())); } catch (IOException ignored) {}
        }, "CMM yt-dlp stderr");
        errThread.setDaemon(true);
        errThread.start();
        String out = new String(process.getInputStream().readAllBytes());
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("YouTube did not answer in time");
            }
            errThread.join(2000);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("interrupted");
        }
        List<String> lines = new ArrayList<>();
        for (String line : out.split("\\R")) if (!line.isBlank()) lines.add(line.trim());
        if (process.exitValue() != 0 || lines.isEmpty()) {
            String msg = err.toString().strip();
            int at = msg.lastIndexOf("ERROR:");
            if (at >= 0) msg = msg.substring(at);
            throw new IOException(msg.isEmpty() ? "could not read that link" : msg.split("\\R")[0]);
        }
        return lines.size() >= 2 ? new Media(lines.get(0), lines.get(1)) : new Media(lines.get(0), lines.get(0));
    }
}
