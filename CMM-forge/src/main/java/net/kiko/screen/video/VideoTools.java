package net.kiko.screen.video;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Finds (or, once, downloads) the two helper programs the screens need:
 * ffmpeg (decodes video and sound) and yt-dlp (turns a YouTube link into a stream address).
 * Everything lives in one folder next to the game, so nothing has to be installed by the player.
 */
public final class VideoTools {

    private static final String OS = System.getProperty("os.name", "").toLowerCase();
    public static final boolean WINDOWS = OS.contains("win");
    public static final boolean MAC = OS.contains("mac");

    private static final String FFMPEG_BASE = "https://github.com/eugeneware/ffmpeg-static/releases/download/b6.0/";
    private static final String YTDLP_BASE = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/";

    private static final long YTDLP_MAX_AGE_MS = TimeUnit.DAYS.toMillis(7);

    private static Path folder;
    private static volatile String ffmpeg;
    private static volatile String ytdlp;

    private VideoTools() {}

    /** Call once with the game directory before anything else. */
    public static synchronized void init(Path gameDir) {
        if (folder == null) folder = gameDir.resolve("cmm_video");
    }

    // ── ffmpeg ──────────────────────────────────────────────────────────────

    /** Path to a working ffmpeg, downloading it first if needed. Blocks; call from a worker thread. */
    public static synchronized String ffmpeg(Consumer<String> status) throws IOException {
        if (ffmpeg != null) return ffmpeg;
        // 1. one that is already installed
        for (String candidate : systemCandidates("ffmpeg")) {
            if (works(candidate, "-version")) return ffmpeg = candidate;
        }
        // 2. the one we downloaded earlier
        Path local = folder.resolve(WINDOWS ? "ffmpeg.exe" : "ffmpeg");
        if (Files.isRegularFile(local) && works(local.toString(), "-version")) return ffmpeg = local.toString();
        // 3. download it (a single compressed program, about 20-30 MB, one time)
        Files.createDirectories(folder);
        status.accept("Downloading video tools (one time only)...");
        Path archive = folder.resolve("ffmpeg.gz");
        download(FFMPEG_BASE + "ffmpeg-" + platform() + ".gz", archive, status, "video tools");
        status.accept("Unpacking video tools...");
        try (InputStream in = new java.util.zip.GZIPInputStream(Files.newInputStream(archive))) {
            Files.copy(in, local, StandardCopyOption.REPLACE_EXISTING);
        }
        local.toFile().setExecutable(true);
        Files.deleteIfExists(archive);
        if (!works(local.toString(), "-version")) throw new IOException("the downloaded video tools do not run on this computer");
        return ffmpeg = local.toString();
    }

    private static String platform() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        boolean arm = arch.contains("aarch64") || arch.contains("arm64");
        return WINDOWS ? "win32-x64" : MAC ? (arm ? "darwin-arm64" : "darwin-x64") : (arm ? "linux-arm64" : "linux-x64");
    }

    // ── yt-dlp ──────────────────────────────────────────────────────────────

    public static synchronized String ytdlp(Consumer<String> status) throws IOException {
        if (ytdlp != null) return ytdlp;
        String file = WINDOWS ? "yt-dlp.exe" : MAC ? "yt-dlp_macos" : "yt-dlp_linux";
        Path local = folder.resolve(WINDOWS ? "yt-dlp.exe" : "yt-dlp");
        boolean fresh = Files.isRegularFile(local)
                && System.currentTimeMillis() - local.toFile().lastModified() < YTDLP_MAX_AGE_MS;
        if (!fresh) {
            Files.createDirectories(folder);
            status.accept("Downloading YouTube support (one time only)...");
            try {
                download(YTDLP_BASE + file, local, status, "yt-dlp");
                local.toFile().setExecutable(true);
            } catch (IOException e) {
                if (!Files.isRegularFile(local)) throw e; // keep using the old one if the update failed
            }
        }
        return ytdlp = local.toString();
    }

    /** Forget the cached yt-dlp so the next call checks for a newer one (YouTube changes often). */
    public static synchronized boolean forceYtdlpUpdate() {
        ytdlp = null;
        if (folder == null) return false;
        File f = folder.resolve(WINDOWS ? "yt-dlp.exe" : "yt-dlp").toFile();
        // no point downloading again if it was fetched in the last hours
        if (f.exists() && System.currentTimeMillis() - f.lastModified() > TimeUnit.HOURS.toMillis(6)) {
            f.setLastModified(0L);
            return true;
        }
        return !f.exists();
    }

    // ── deno (YouTube now needs a small JavaScript engine to read its links) ──

    private static volatile String deno;

    /** Path to a downloaded deno, or null if we do not have one (yet). */
    public static String denoIfPresent() {
        if (deno != null) return deno;
        if (folder == null) return null;
        Path local = folder.resolve(WINDOWS ? "deno.exe" : "deno");
        return Files.isRegularFile(local) ? (deno = local.toString()) : null;
    }

    public static synchronized String deno(Consumer<String> status) throws IOException {
        String have = denoIfPresent();
        if (have != null) return have;
        String arch = System.getProperty("os.arch", "").toLowerCase();
        boolean arm = arch.contains("aarch64") || arch.contains("arm64");
        String file = WINDOWS ? "deno-x86_64-pc-windows-msvc.zip"
                : MAC ? (arm ? "deno-aarch64-apple-darwin.zip" : "deno-x86_64-apple-darwin.zip")
                : (arm ? "deno-aarch64-unknown-linux-gnu.zip" : "deno-x86_64-unknown-linux-gnu.zip");
        Files.createDirectories(folder);
        Path archive = folder.resolve("deno.zip");
        Path local = folder.resolve(WINDOWS ? "deno.exe" : "deno");
        status.accept("Downloading YouTube helper (one time only)...");
        download("https://github.com/denoland/deno/releases/latest/download/" + file, archive, status, "helper");
        unzipEntry(archive, WINDOWS ? "deno.exe" : "deno", local);
        local.toFile().setExecutable(true);
        Files.deleteIfExists(archive);
        return deno = local.toString();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static List<String> systemCandidates(String name) {
        String exe = WINDOWS ? name + ".exe" : name;
        return List.of(exe,
                "/opt/homebrew/bin/" + exe, "/usr/local/bin/" + exe, "/usr/bin/" + exe);
    }

    private static boolean works(String exe, String arg) {
        try {
            Process p = new ProcessBuilder(exe, arg).redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor(15, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void download(String url, Path target, Consumer<String> status, String label) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".part");
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(java.time.Duration.ofSeconds(20)).build();
            HttpResponse<InputStream> response = client.send(
                    HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "CMM-Mod").GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) throw new IOException("download of " + label + " failed (HTTP " + response.statusCode() + ")");
            long total = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            long done = 0;
            int lastPercent = -1;
            try (InputStream in = response.body(); var out = Files.newOutputStream(tmp)) {
                byte[] buf = new byte[1 << 16];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    done += n;
                    if (total > 0) {
                        int percent = (int) (done * 100 / total);
                        if (percent / 5 != lastPercent / 5) {
                            lastPercent = percent;
                            status.accept("Downloading " + label + "... " + percent + "%");
                        }
                    }
                }
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted");
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static void unzipEntry(Path zip, String endsWith, Path target) throws IOException {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.isDirectory() && ("/" + entry.getName()).endsWith(endsWith)) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IOException("the program was not found inside the download");
    }
}
