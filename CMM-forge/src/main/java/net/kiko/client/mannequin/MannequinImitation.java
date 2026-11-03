package net.kiko.client.mannequin;

import com.mojang.blaze3d.platform.InputConstants;
import net.kiko.KikoMod;
import net.kiko.client.PoseBindings;
import net.kiko.mannequin.ClientMannequinModeStore;
import net.kiko.mannequin.Mannequin;
import net.kiko.mannequin.MannequinMode;
import net.kiko.network.MannequinModeC2SPacket;
import net.kiko.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * "Imitate mannequin": while it is on, the player is drawn as a mannequin with the chosen pose and skin
 * (everyone who can see the player sees it). The game still treats them as a normal player.
 */
public final class MannequinImitation {

    /** Exit / toggle key. J is not used by the game by default; it can be changed in Controls. */
    public static final KeyMapping TOGGLE = new KeyMapping("key.kiko.mannequin_mode",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, PoseBindings.CATEGORY);

    private static final class Dummy {
        Mannequin mannequin;
        MannequinMode applied;
    }

    private static final Map<UUID, Dummy> DUMMIES = new HashMap<>();
    private static MannequinMode last;

    private MannequinImitation() {}

    public static void clear() {
        DUMMIES.clear();
        last = null;
        ClientMannequinModeStore.clear();
    }

    /** Called by the edit screen's button. */
    public static void start(MannequinMode mode) {
        last = mode;
        ModNetwork.CHANNEL.sendToServer(new MannequinModeC2SPacket(mode));
        message(Component.translatable("screen.kiko.mannequin.imitate.on", TOGGLE.getTranslatedKeyMessage()));
    }

    /** The key: leave the mode, or go back into the last one. */
    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (ClientMannequinModeStore.get(mc.player.getUUID()).isActive()) {
            ModNetwork.CHANNEL.sendToServer(new MannequinModeC2SPacket(MannequinMode.EMPTY));
            message(Component.translatable("screen.kiko.mannequin.imitate.off"));
        } else if (last != null) {
            start(last);
        } else {
            message(Component.translatable("screen.kiko.mannequin.imitate.none"));
        }
    }

    private static void message(Component text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(text, true);
    }

    /**
     * Draws the player as a mannequin. Returns true when it did, so the normal player rendering is skipped.
     */
    public static boolean render(AbstractClientPlayer player, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight) {
        MannequinMode mode = ClientMannequinModeStore.get(player.getUUID());
        if (!mode.isActive()) {
            DUMMIES.remove(player.getUUID());
            return false;
        }

        Dummy dummy = DUMMIES.get(player.getUUID());
        if (dummy == null || dummy.mannequin.level() != player.level()) {
            dummy = new Dummy();
            dummy.mannequin = new Mannequin(player.level(), player.getX(), player.getY(), player.getZ());
            DUMMIES.put(player.getUUID(), dummy);
        }
        Mannequin m = dummy.mannequin;

        if (dummy.applied != mode) {
            m.setHeadPose(mode.rotation(0));
            m.setBodyPose(mode.rotation(1));
            m.setLeftArmPose(mode.rotation(2));
            m.setRightArmPose(mode.rotation(3));
            m.setLeftLegPose(mode.rotation(4));
            m.setRightLegPose(mode.rotation(5));
            m.setSlimArms(mode.slimArms());
            m.setMannequinCrouching(mode.crouching());
            m.setSkinUnchecked(mode.skin());
            dummy.applied = mode;
        }

        m.setPos(player.getX(), player.getY(), player.getZ());
        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        m.setYRot(yaw);
        m.yRotO = yaw;
        m.setInvisible(player.isInvisible());
        m.setCustomName(player.getDisplayName());
        m.setCustomNameVisible(player != Minecraft.getInstance().player);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            m.setItemSlot(slot, player.getItemBySlot(slot));
        }

        EntityRenderer<? super Mannequin> renderer =
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(m);
        renderer.render(m, yaw, partialTick, poseStack, buffer, packedLight);
        return true;
    }
}
