package net.kiko.client.screen;

import net.kiko.screen.video.VideoTools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Opens the computer's own "choose a file" window without any extra libraries (PowerShell / osascript / zenity). */
final class FilePicker {

    private FilePicker() {}

    /** Runs on its own thread. The callback gets the chosen path, or null if nothing was chosen; error text goes to onError. */
    static void pick(Consumer<String> onPicked, Consumer<String> onError) {
        Thread t = new Thread(() -> {
            try {
                String path = ask();
                if (path != null && !path.isBlank()) onPicked.accept(path.trim());
            } catch (Exception e) {
                onError.accept("Could not open a file window: " + e.getMessage());
            }
        }, "CMM file picker");
        t.setDaemon(true);
        t.start();
    }

    private static String ask() throws IOException, InterruptedException {
        if (VideoTools.WINDOWS) {
            String script = "Add-Type -AssemblyName System.Windows.Forms; "
                    + "$o = New-Object System.Windows.Forms.Form; $o.TopMost = $true; "
                    + "$f = New-Object System.Windows.Forms.OpenFileDialog; "
                    + "$f.Title = 'Pick a video'; "
                    + "$f.Filter = 'Video files|*.mp4;*.mkv;*.webm;*.mov;*.avi;*.m4v;*.flv;*.wmv;*.mpg;*.mpeg;*.gif|All files|*.*'; "
                    + "if ($f.ShowDialog($o) -eq 'OK') { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8; Write-Output $f.FileName }";
            return run(List.of("powershell", "-NoProfile", "-STA", "-WindowStyle", "Hidden", "-Command", script));
        }
        if (VideoTools.MAC) {
            return run(List.of("osascript", "-e", "POSIX path of (choose file with prompt \"Pick a video\")"));
        }
        try {
            return run(List.of("zenity", "--file-selection", "--title=Pick a video"));
        } catch (IOException noZenity) {
            return run(List.of("kdialog", "--getopenfilename", System.getProperty("user.home"), "*.mp4 *.mkv *.webm *.mov *.avi|Video files"));
        }
    }

    private static String run(List<String> cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor(10, TimeUnit.MINUTES);
        String line = out.strip();
        int nl = line.indexOf('\n');
        return nl >= 0 ? line.substring(0, nl).strip() : line;
    }
}
