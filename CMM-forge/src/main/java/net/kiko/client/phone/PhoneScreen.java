package net.kiko.client.phone;

import net.kiko.pose.PoseKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

/**
 * The phone screen. Opening it raises the arm (browsing); the buttons play the other phone animations:
 * call, speaker and selfie. Closing keeps call / speaker / selfie going, "Put phone down" ends them.
 */
public class PhoneScreen extends Screen {

    private final InteractionHand hand;
    private PoseKind pose = PoseKind.PHONE_BROWSE;

    public PhoneScreen(InteractionHand hand) {
        super(Component.translatable("screen.kiko.phone.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        PhoneClient.setPose(hand, pose);
        int cx = width / 2;
        int top = height / 2 - 52;
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.phone.browse"), b -> choose(PoseKind.PHONE_BROWSE))
                .bounds(cx - 72, top + 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.phone.call"), b -> choose(PoseKind.PHONE_CALL))
                .bounds(cx + 2, top + 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.phone.speaker"), b -> choose(PoseKind.PHONE_SPEAKER))
                .bounds(cx - 72, top + 52, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.phone.selfie"), b -> choose(PoseKind.PHONE_SELFIE))
                .bounds(cx + 2, top + 52, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.phone.lower"), b -> {
            PhoneClient.setPose(hand, PoseKind.NONE);
            pose = PoseKind.NONE;
            onClose();
        }).bounds(cx - 72, top + 80, 144, 20).build());
    }

    private void choose(PoseKind kind) {
        pose = kind;
        PhoneClient.setPose(hand, kind);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2;
        int top = height / 2 - 52;
        // a little phone body behind the buttons
        g.fill(cx - 84, top - 6, cx + 84, top + 108, 0xFF1B1B1F);
        g.fill(cx - 81, top - 3, cx + 81, top + 105, 0xFF2D2A38);
        g.drawCenteredString(font, title, cx, top + 8, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    /** Browsing only lasts while the screen is open; the other poses carry on. */
    @Override
    public void onClose() {
        if (pose == PoseKind.PHONE_BROWSE) PhoneClient.setPose(hand, PoseKind.NONE);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
