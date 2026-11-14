package net.kiko.client.screen;

import net.kiko.network.ModNetwork;
import net.kiko.network.MonitorSetC2SPacket;
import net.kiko.screen.MonitorBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** The settings window of a monitor: paste a link, press Play. */
public class MonitorScreen extends Screen {

    private final BlockPos pos;
    private EditBox urlBox;
    private Button loopButton;
    private boolean loop;
    private int volume;
    private boolean playing;

    public MonitorScreen(BlockPos pos, MonitorBlockEntity be) {
        super(Component.literal("Monitor"));
        this.pos = pos;
        this.loop = be.isLoop();
        this.volume = be.getVolume();
        this.playing = be.isPlaying();
        this.initialUrl = be.getUrl();
    }

    private final String initialUrl;

    @Override
    protected void init() {
        int cx = width / 2;
        int top = height / 2 - 52;

        urlBox = new EditBox(font, cx - 150, top + 22, 300, 20, Component.literal("Link"));
        urlBox.setMaxLength(1024);
        urlBox.setValue(initialUrl);
        urlBox.setHint(Component.literal("Paste a YouTube or video link here"));
        addRenderableWidget(urlBox);
        setInitialFocus(urlBox);

        addRenderableWidget(Button.builder(Component.literal("Play"), b -> send(true, true))
                .bounds(cx - 150, top + 50, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop"), b -> send(false, false))
                .bounds(cx - 48, top + 50, 96, 20).build());
        loopButton = addRenderableWidget(Button.builder(loopText(), b -> {
            loop = !loop;
            b.setMessage(loopText());
            if (playing) send(true, false);
        }).bounds(cx + 54, top + 50, 96, 20).build());

        addRenderableWidget(new AbstractSliderButton(cx - 150, top + 78, 300, 20, volumeText(), volume / 100.0D) {
            @Override
            protected void updateMessage() {
                volume = (int) Math.round(value * 100.0D);
                setMessage(volumeText());
            }

            @Override
            protected void applyValue() {
                volume = (int) Math.round(value * 100.0D);
                if (playing) send(true, false);
            }
        });

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 50, top + 106, 100, 20).build());
    }

    private Component loopText() { return Component.literal("Loop: " + (loop ? "On" : "Off")); }

    private Component volumeText() { return Component.literal("Volume: " + volume + "%"); }

    private void send(boolean play, boolean restart) {
        String url = urlBox.getValue().trim();
        if (play && url.isEmpty()) return;
        playing = play;
        ModNetwork.CHANNEL.sendToServer(new MonitorSetC2SPacket(pos, url, play, restart, loop, volume));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, height / 2 - 52, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, Component.literal("Monitors placed next to each other join into one screen."),
                width / 2, height / 2 + 82, 0xA0A0A0);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
