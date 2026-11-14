package net.kiko.client.screen;

import net.kiko.network.ModNetwork;
import net.kiko.network.MonitorSetC2SPacket;
import net.kiko.screen.MonitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/** The settings window of a monitor: paste a link or pick a file, then play, pause and jump around. */
public class MonitorScreen extends Screen {

    private final BlockPos pos;
    private final String initialUrl;
    private EditBox urlBox;
    private Button pauseButton;
    private boolean loop;
    private int volume;
    private String message = "";

    public MonitorScreen(BlockPos pos, MonitorBlockEntity be) {
        super(Component.literal("Monitor"));
        this.pos = pos;
        this.loop = be.isLoop();
        this.volume = be.getVolume();
        this.initialUrl = be.getUrl();
    }

    private MonitorBlockEntity be() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getBlockEntity(pos) instanceof MonitorBlockEntity be ? be : null;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int top = height / 2 - 70;

        urlBox = new EditBox(font, cx - 150, top + 22, 240, 20, Component.literal("Link"));
        urlBox.setMaxLength(1024);
        urlBox.setValue(shown(initialUrl));
        urlBox.setHint(Component.literal("Paste a YouTube or video link"));
        addRenderableWidget(urlBox);
        setInitialFocus(urlBox);
        addRenderableWidget(Button.builder(Component.literal("File..."), b -> pickFile())
                .bounds(cx + 94, top + 22, 56, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Play"), b -> play())
                .bounds(cx - 150, top + 50, 72, 20).build());
        pauseButton = addRenderableWidget(Button.builder(Component.literal("Pause"), b -> togglePause())
                .bounds(cx - 74, top + 50, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop"), b -> send(MonitorSetC2SPacket.STOP, 0))
                .bounds(cx + 2, top + 50, 72, 20).build());
        addRenderableWidget(Button.builder(loopText(), b -> {
            loop = !loop;
            b.setMessage(loopText());
            send(MonitorSetC2SPacket.SETTINGS, 0);
        }).bounds(cx + 78, top + 50, 72, 20).build());

        addRenderableWidget(Button.builder(Component.literal("<< 10s"), b -> send(MonitorSetC2SPacket.SEEK, -10))
                .bounds(cx - 150, top + 74, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<< 1 min"), b -> send(MonitorSetC2SPacket.SEEK, -60))
                .bounds(cx - 74, top + 74, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("1 min >>"), b -> send(MonitorSetC2SPacket.SEEK, 60))
                .bounds(cx + 2, top + 74, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("10s >>"), b -> send(MonitorSetC2SPacket.SEEK, 10))
                .bounds(cx + 78, top + 74, 72, 20).build());

        addRenderableWidget(new AbstractSliderButton(cx - 150, top + 98, 300, 20, volumeText(), volume / 100.0D) {
            @Override
            protected void updateMessage() {
                volume = (int) Math.round(value * 100.0D);
                setMessage(volumeText());
            }

            @Override
            protected void applyValue() {
                volume = (int) Math.round(value * 100.0D);
                send(MonitorSetC2SPacket.SETTINGS, 0);
            }
        });

        addRenderableWidget(Button.builder(Component.literal("Restart"), b -> play())
                .bounds(cx - 150, top + 122, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 48, top + 122, 96, 20).build());
    }

    /** A picked file is stored as a file: address; show the plain path in the box. */
    private static String shown(String url) {
        if (url.regionMatches(true, 0, "file:", 0, 5)) {
            try { return Path.of(java.net.URI.create(url)).toString(); } catch (RuntimeException ignored) {}
        }
        return url;
    }

    private void pickFile() {
        message = "Choose a video in the window that opens...";
        FilePicker.pick(path -> Minecraft.getInstance().execute(() -> {
            urlBox.setValue(path);
            message = "Picked. Press Play.";
        }), err -> Minecraft.getInstance().execute(() -> message = err));
    }

    /** The address that is really sent: web links stay as typed, file paths become file: addresses. */
    private String address() {
        String text = urlBox.getValue().trim();
        if (text.isEmpty()) return "";
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:")) return text;
        try {
            Path p = Path.of(text.replace("\"", ""));
            if (Files.isRegularFile(p)) return p.toUri().toString();
        } catch (RuntimeException ignored) {}
        message = "That is not a link and no file exists at that path.";
        return "";
    }

    private void play() {
        String url = address();
        if (url.isEmpty()) return;
        message = "";
        ModNetwork.CHANNEL.sendToServer(new MonitorSetC2SPacket(pos, url, MonitorSetC2SPacket.PLAY, 0, loop, volume));
    }

    private void togglePause() {
        MonitorBlockEntity be = be();
        if (be == null || !be.isPlaying()) return;
        send(be.isPaused() ? MonitorSetC2SPacket.RESUME : MonitorSetC2SPacket.PAUSE, 0);
    }

    private void send(int action, int delta) {
        MonitorBlockEntity be = be();
        String url = be == null ? "" : be.getUrl();
        ModNetwork.CHANNEL.sendToServer(new MonitorSetC2SPacket(pos, url, action, delta, loop, volume));
    }

    private Component loopText() { return Component.literal("Loop: " + (loop ? "On" : "Off")); }

    private Component volumeText() { return Component.literal("Volume: " + volume + "%"); }

    private static String time(double seconds) {
        int s = (int) seconds;
        return s >= 3600 ? String.format("%d:%02d:%02d", s / 3600, s / 60 % 60, s % 60) : String.format("%d:%02d", s / 60, s % 60);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        MonitorBlockEntity be = be();
        if (pauseButton != null && be != null) {
            pauseButton.setMessage(Component.literal(be.isPaused() ? "Resume" : "Pause"));
            pauseButton.active = be.isPlaying();
        }
        int top = height / 2 - 70;
        g.drawCenteredString(font, title, width / 2, top + 6, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
        String info;
        if (be == null || !be.isPlaying()) info = "Stopped";
        else info = (be.isPaused() ? "Paused at " : "Playing  ") + time(be.positionSeconds(Minecraft.getInstance().level.getGameTime()));
        g.drawCenteredString(font, Component.literal(info), width / 2, top + 148, 0xE0E0E0);
        g.drawCenteredString(font, Component.literal(message.isEmpty()
                        ? "Picked files play only for players who have that same file."
                        : message),
                width / 2, top + 162, message.isEmpty() ? 0x909090 : 0xFFD070);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
