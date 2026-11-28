package net.kiko.client.walkman;

import net.kiko.item.WalkmanItem;
import net.kiko.walkman.WalkmanMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** One cassette slot on top, the player buttons under it, then the inventory. */
public class WalkmanScreen extends AbstractContainerScreen<WalkmanMenu> {

    private Button playButton;
    private Button speakerButton;

    public WalkmanScreen(WalkmanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos + 44;
        addButton(x + 8, y, 30, WalkmanMenu.PREVIOUS, Component.translatable("screen.kiko.walkman.previous"));
        addButton(x + 40, y, 22, WalkmanMenu.BACK, Component.translatable("screen.kiko.walkman.back"));
        playButton = addButton(x + 64, y, 48, WalkmanMenu.PLAY_PAUSE, Component.translatable("screen.kiko.walkman.play"));
        addButton(x + 114, y, 22, WalkmanMenu.FORWARD, Component.translatable("screen.kiko.walkman.forward"));
        addButton(x + 138, y, 30, WalkmanMenu.NEXT, Component.translatable("screen.kiko.walkman.next"));
        addButton(x + 64, y + 22, 48, WalkmanMenu.STOP, Component.translatable("screen.kiko.walkman.stop"));
        speakerButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, WalkmanMenu.SPEAKER);
        }).bounds(x + 8, y + 22, 52, 18).build());
        addRenderableWidget(new VolumeSlider(x + 116, y + 22, 52, 18));
    }

    private Button addButton(int x, int y, int w, int id, Component label) {
        return addRenderableWidget(Button.builder(label, b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(x, y, w, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        boolean has = menu.hasCassette();
        boolean playing = WalkmanItem.isPlaying(menu.walkman());
        for (var w : renderables) {
            if (w instanceof Button b && b != speakerButton) b.active = has;
        }
        if (playButton != null) {
            playButton.setMessage(Component.translatable(playing ? "screen.kiko.walkman.pause" : "screen.kiko.walkman.play"));
        }
        if (speakerButton != null) {
            speakerButton.setMessage(Component.translatable(WalkmanItem.isSpeaker(menu.walkman())
                    ? "screen.kiko.walkman.speaker_on" : "screen.kiko.walkman.speaker_off"));
        }
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2B2B);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFC6C6C6);
        // cassette slot (slot sits at 80,20)
        slotBox(g, x + 79, y + 19);
        g.drawCenteredString(font, Component.translatable("screen.kiko.walkman.cassette"), x + 88, y + 7, 0x404040);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slotBox(g, x + 7 + col * 18, y + 83 + row * 18);
        for (int col = 0; col < 9; col++) slotBox(g, x + 7 + col * 18, y + 141);
    }

    private static void slotBox(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF373737);
        g.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    @Override
    public void removed() {
        super.removed();
        WalkmanVolume.save();
    }

    /** Volume of the music, only for you. */
    private static final class VolumeSlider extends net.minecraft.client.gui.components.AbstractSliderButton {
        VolumeSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), WalkmanVolume.get());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.kiko.walkman.volume", Math.round(value * 100)));
        }

        @Override
        protected void applyValue() {
            WalkmanVolume.set((float) value);
        }
    }
}
