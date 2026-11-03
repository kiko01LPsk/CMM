package net.kiko.client.mannequin;

import net.kiko.mannequin.Mannequin;
import net.kiko.mannequin.MannequinMode;
import net.kiko.mannequin.MannequinSettings;
import net.kiko.network.MannequinEditC2SPacket;
import net.kiko.network.MannequinSkinC2SPacket;
import net.kiko.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * The mannequin editor, opened by sneak + right-click. It is a narrow panel on the left so the mannequin
 * stays visible while you edit it. Every change is sent to the server.
 */
public class MannequinScreen extends Screen {

    private enum Tab { POSE, BODY, STYLE, SKIN }

    private static final String[] PART_KEYS =
            {"head", "body", "left_arm", "right_arm", "left_leg", "right_leg"};
    private static final String[] AXES = {"X", "Y", "Z"};
    private static final int PANEL_WIDTH = 180;

    private final Mannequin mannequin;
    private final MannequinSettings settings;
    private Tab tab = Tab.POSE;
    private int part = 0;
    private String pathText = "";
    private Component status = null;
    private int statusColor = 0xFFFFFF;

    public MannequinScreen(Mannequin mannequin) {
        super(Component.translatable("screen.kiko.mannequin.title"));
        this.mannequin = mannequin;
        this.settings = MannequinSettings.from(mannequin);
    }

    @Override
    public boolean isPauseScreen() {
        // The singleplayer server must keep ticking, otherwise the changes would not show up.
        return false;
    }

    private void send() {
        ModNetwork.CHANNEL.sendToServer(new MannequinEditC2SPacket(mannequin.getId(), settings.copy()));
    }

    @Override
    protected void init() {
        int x = 10;
        int w = PANEL_WIDTH - 20;
        int tabW = (w - 6) / 4;
        addTab(Tab.POSE, "screen.kiko.mannequin.tab.pose", x, 22, tabW);
        addTab(Tab.BODY, "screen.kiko.mannequin.tab.body", x + (tabW + 2), 22, tabW);
        addTab(Tab.STYLE, "screen.kiko.mannequin.tab.style", x + 2 * (tabW + 2), 22, tabW);
        addTab(Tab.SKIN, "screen.kiko.mannequin.tab.skin", x + 3 * (tabW + 2), 22, tabW);

        int y = 48;
        switch (tab) {
            case POSE -> {
                addRenderableWidget(Button.builder(
                        Component.translatable("screen.kiko.mannequin.part").append(": ")
                                .append(Component.translatable("screen.kiko.mannequin.part." + PART_KEYS[part])),
                        b -> { part = (part + 1) % PART_KEYS.length; rebuildWidgets(); })
                        .bounds(x, y, w, 20).build());
                y += 24;
                for (int axis = 0; axis < 3; axis++) {
                    final int index = part * 3 + axis;
                    addRenderableWidget(new ValueSlider(x, y, w, Component.literal(AXES[axis]), -180, 180, 0,
                            () -> settings.pose[index], v -> settings.pose[index] = (float) v));
                    y += 22;
                }
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.reset_part"), b -> {
                    for (int axis = 0; axis < 3; axis++) settings.pose[part * 3 + axis] = 0.0F;
                    send();
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
                y += 26;
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.imitate"), b -> {
                    MannequinImitation.start(MannequinMode.of(settings, mannequin.getSkin()));
                    onClose();
                }).bounds(x, y, w, 20).build());
            }
            case BODY -> {
                addRenderableWidget(new ValueSlider(x, y, w, Component.translatable("screen.kiko.mannequin.rotation"),
                        -180, 180, 0, () -> settings.yaw, v -> settings.yaw = (float) v));
                y += 24;
                addRenderableWidget(new ValueSlider(x, y, w, Component.translatable("screen.kiko.mannequin.scale"),
                        Mannequin.MIN_SCALE, Mannequin.MAX_SCALE, 2, () -> settings.scale, v -> settings.scale = (float) v));
                y += 24;
                addRenderableWidget(new ValueSlider(x, y, w, Component.translatable("screen.kiko.mannequin.tilt_x"),
                        -180, 180, 0, () -> settings.tiltX, v -> settings.tiltX = (float) v));
                y += 24;
                addRenderableWidget(new ValueSlider(x, y, w, Component.translatable("screen.kiko.mannequin.tilt_z"),
                        -180, 180, 0, () -> settings.tiltZ, v -> settings.tiltZ = (float) v));
                y += 28;
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.reset_body"), b -> {
                    settings.scale = 1.0F;
                    settings.tiltX = 0.0F;
                    settings.tiltZ = 0.0F;
                    send();
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
            }
            case STYLE -> {
                int half = (w - 2) / 2;
                toggle(x, y, w, "screen.kiko.mannequin.arms", () -> settings.showArms, v -> settings.showArms = v);
                y += 22;
                toggle(x, y, w, "screen.kiko.mannequin.base_plate", () -> !settings.noBasePlate, v -> settings.noBasePlate = !v);
                y += 22;
                toggle(x, y, w, "screen.kiko.mannequin.small", () -> settings.small, v -> settings.small = v);
                y += 22;
                toggle(x, y, w, "screen.kiko.mannequin.crouching", () -> settings.crouching, v -> settings.crouching = v);
                y += 26;
                part(x, y, half, "screen.kiko.mannequin.hat", PlayerModelPart.HAT);
                part(x + half + 2, y, half, "screen.kiko.mannequin.jacket", PlayerModelPart.JACKET);
                y += 22;
                part(x, y, half, "screen.kiko.mannequin.left_sleeve", PlayerModelPart.LEFT_SLEEVE);
                part(x + half + 2, y, half, "screen.kiko.mannequin.right_sleeve", PlayerModelPart.RIGHT_SLEEVE);
                y += 22;
                part(x, y, half, "screen.kiko.mannequin.left_pants", PlayerModelPart.LEFT_PANTS_LEG);
                part(x + half + 2, y, half, "screen.kiko.mannequin.right_pants", PlayerModelPart.RIGHT_PANTS_LEG);
            }
            case SKIN -> {
                int half = (w - 2) / 2;
                // Arm width: the button of the current width is greyed out.
                Button classic = Button.builder(Component.translatable("screen.kiko.mannequin.arms_classic"), b -> {
                    settings.slimArms = false;
                    send();
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build();
                classic.active = settings.slimArms;
                addRenderableWidget(classic);
                y += 22;
                Button slim = Button.builder(Component.translatable("screen.kiko.mannequin.arms_slim"), b -> {
                    settings.slimArms = true;
                    send();
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build();
                slim.active = !settings.slimArms;
                addRenderableWidget(slim);
                y += 30;

                EditBox path = new EditBox(font, x, y, w, 20, Component.translatable("screen.kiko.mannequin.skin.path"));
                path.setMaxLength(500);
                path.setHint(Component.translatable("screen.kiko.mannequin.skin.path"));
                path.setValue(pathText);
                path.setResponder(v -> pathText = v);
                addRenderableWidget(path);
                y += 22;
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.skin.browse"), b -> {
                    String chosen = MannequinSkinLoader.browse();
                    if (chosen == null || chosen.isEmpty()) {
                        setStatus(Component.translatable("screen.kiko.mannequin.skin.browse_failed"), 0xFFAA55);
                        return;
                    }
                    pathText = chosen;
                    loadSkin();
                }).bounds(x, y, half, 20).build());
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.skin.load"), b -> loadSkin())
                        .bounds(x + half + 2, y, half, 20).build());
                y += 22;
                addRenderableWidget(Button.builder(Component.translatable("screen.kiko.mannequin.skin.clear"), b -> {
                    ModNetwork.CHANNEL.sendToServer(new MannequinSkinC2SPacket(mannequin.getId(), ""));
                    setStatus(Component.translatable("screen.kiko.mannequin.skin.cleared"), 0xFFFFFF);
                }).bounds(x, y, w, 20).build());
            }
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x, height - 28, w, 20).build());
    }

    private void setStatus(Component text, int color) {
        this.status = text;
        this.statusColor = color;
    }

    private void loadSkin() {
        try {
            String skin = MannequinSkinLoader.load(pathText);
            ModNetwork.CHANNEL.sendToServer(new MannequinSkinC2SPacket(mannequin.getId(), skin));
            setStatus(Component.translatable("screen.kiko.mannequin.skin.loaded"), 0x55FF55);
        } catch (MannequinSkinLoader.SkinException e) {
            setStatus(Component.translatable(e.getMessage()), 0xFF5555);
        }
    }

    private void addTab(Tab target, String key, int x, int y, int w) {
        Button button = Button.builder(Component.translatable(key), b -> { tab = target; rebuildWidgets(); })
                .bounds(x, y, w, 20).build();
        button.active = tab != target;
        addRenderableWidget(button);
    }

    private void toggle(int x, int y, int w, String key, BooleanSupplier get, Consumer<Boolean> set) {
        Button[] self = new Button[1];
        self[0] = Button.builder(toggleText(key, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            self[0].setMessage(toggleText(key, get.getAsBoolean()));
            send();
        }).bounds(x, y, w, 20).build();
        addRenderableWidget(self[0]);
    }

    private void part(int x, int y, int w, String key, PlayerModelPart modelPart) {
        toggle(x, y, w, key, () -> (settings.parts & modelPart.getMask()) != 0, v ->
                settings.parts = (byte) (v ? settings.parts | modelPart.getMask() : settings.parts & ~modelPart.getMask()));
    }

    private static Component toggleText(String key, boolean on) {
        return Component.translatable(key).append(": ").append(on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    @Override
    public void tick() {
        super.tick();
        if (mannequin.isRemoved()) onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, PANEL_WIDTH, height, 0xB0000000);
        graphics.drawString(font, title, 10, 8, 0xFFFFFF, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (tab == Tab.SKIN) {
            Component hint = status != null ? status : Component.translatable("screen.kiko.mannequin.skin.hint");
            graphics.drawWordWrap(font, hint, 10, 168, PANEL_WIDTH - 20, status != null ? statusColor : 0xAAAAAA);
        }
    }

    /** A slider that writes into the settings and sends them on every change. */
    private final class ValueSlider extends AbstractSliderButton {
        private final Component label;
        private final double min;
        private final double max;
        private final int decimals;
        private final DoubleConsumer setter;

        ValueSlider(int x, int y, int w, Component label, double min, double max, int decimals,
                    DoubleSupplier getter, DoubleConsumer setter) {
            super(x, y, w, 20, CommonComponents.EMPTY, Mth.clamp((getter.getAsDouble() - min) / (max - min), 0.0, 1.0));
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double v = min + value * (max - min);
            setMessage(Component.empty().append(label).append(": ")
                    .append(String.format(java.util.Locale.ROOT, "%." + decimals + "f", v)));
        }

        @Override
        protected void applyValue() {
            setter.accept(min + value * (max - min));
            send();
        }
    }
}
