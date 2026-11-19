package net.kiko.client;

import net.kiko.KikoMod;
import net.kiko.client.mannequin.MannequinImitation;
import net.kiko.pose.ClientPoseStore;
import net.kiko.pose.PoseKind;
import net.kiko.pose.PoseState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.kiko.client.screen.VideoManager.tick();
        net.kiko.client.morph.MorphRenderer.tick(Minecraft.getInstance());

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        for (PoseBindings.Binding binding : PoseBindings.all()) {
            while (binding.key().consumeClick()) {
                PoseBindings.toggle(binding);
            }
        }
        while (MannequinImitation.TOGGLE.consumeClick()) {
            MannequinImitation.toggle();
        }
        while (net.kiko.client.morph.MorphRenderer.RESET.consumeClick()) {
            net.kiko.network.ModNetwork.CHANNEL.sendToServer(new net.kiko.network.MorphResetC2SPacket());
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        net.kiko.client.screen.VideoManager.closeAll();
        PoseBindings.clear();
        ClientPoseStore.clear();
        MannequinImitation.clear();
        net.kiko.client.morph.MorphRenderer.clear();
    }

    /**
     * Runs after vanilla has set the arm poses for this frame and before the model animates,
     * so overriding the poses here is what the renderer ends up drawing.
     * Works for you (local state) and for other players (state sent by the server).
     */
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        // Morph wand: draw the player as the mob they turned into.
        if (net.kiko.client.morph.MorphRenderer.render(player, event.getPartialTick(), event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight())) {
            event.setCanceled(true);
            return;
        }

        // "Imitate mannequin": draw the player as a mannequin instead (skips the normal player rendering).
        if (MannequinImitation.render(player, event.getPartialTick(), event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight())) {
            event.setCanceled(true);
            return;
        }

        Object model = event.getRenderer().getModel();
        if (!(model instanceof PlayerModel<?> playerModel)) return;

        // Calling Mjolnir: the dominant arm raises in the horn pose until the hammer arrives.
        if (net.kiko.client.mjolnir.MjolnirClient.isCalling(player)) {
            if (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT) {
                playerModel.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.TOOT_HORN;
            } else {
                playerModel.leftArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.TOOT_HORN;
            }
        }

        PoseState state = player == Minecraft.getInstance().player
                ? PoseBindings.localState()
                : ClientPoseStore.get(player.getUUID());
        if (state.isNone()) return;

        if (state.right() != PoseKind.NONE) playerModel.rightArmPose = PoseBindings.toArmPose(state.right());
        if (state.left() != PoseKind.NONE) playerModel.leftArmPose = PoseBindings.toArmPose(state.left());
    }
}
