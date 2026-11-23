package net.kiko.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kiko.KikoMod;
import net.kiko.item.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Sniper rifle aiming: press the Aim key (Z by default, changeable in Controls) to zoom in with a scope, again to zoom out. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SniperScope {

    public static final KeyMapping AIM = new KeyMapping("key.kiko.sniper_aim",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, PoseBindings.CATEGORY);

    private static final float ZOOM = 0.2F; // field of view multiplier (5x zoom)
    private static boolean aiming = false;
    private static float amount = 0.0F;      // 0 = not zoomed, 1 = fully zoomed (eases in and out)

    private SniperScope() {}

    private static boolean holdingSniper(Minecraft mc) {
        return mc.player != null && mc.player.getMainHandItem().is(ModItems.SNIPER_RIFLE.get());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (AIM.consumeClick()) {
            if (mc.screen == null && holdingSniper(mc)) aiming = !aiming;
        }
        if (!holdingSniper(mc) || mc.screen != null) aiming = false;
        if (mc.player != null) {
            boolean left = mc.player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT;
            PoseBindings.setAimPose(!aiming ? net.kiko.pose.PoseState.NONE
                    : left ? new net.kiko.pose.PoseState(net.kiko.pose.PoseKind.NONE, net.kiko.pose.PoseKind.BOW)
                           : new net.kiko.pose.PoseState(net.kiko.pose.PoseKind.BOW, net.kiko.pose.PoseKind.NONE));
        }
        float target = aiming ? 1.0F : 0.0F;
        amount += (target - amount) * 0.35F;
        if (Math.abs(target - amount) < 0.01F) amount = target;
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (amount > 0.0F) {
            event.setNewFovModifier(event.getNewFovModifier() * (1.0F + (ZOOM - 1.0F) * amount));
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (amount > 0.5F) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre event) {
        if (amount > 0.5F && event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) event.setCanceled(true);
    }

    /** Black ring around a round view plus a thin crosshair, drawn under the rest of the HUD. */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (amount < 0.5F) return;
        GuiGraphics g = event.getGuiGraphics();
        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;
        double r = h * 0.46D;
        int black = 0xFF000000;
        for (int y = 0; y < h; y++) {
            double dy = y + 0.5D - cy;
            if (Math.abs(dy) >= r) {
                g.fill(0, y, w, y + 1, black);
            } else {
                int half = (int) Math.sqrt(r * r - dy * dy);
                g.fill(0, y, cx - half, y + 1, black);
                g.fill(cx + half, y, w, y + 1, black);
            }
        }
        int line = 0xCC000000;
        g.fill(cx - (int) r, cy, cx + (int) r, cy + 1, line);
        g.fill(cx, cy - (int) r, cx + 1, cy + (int) r, line);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFF2020);
    }
}
