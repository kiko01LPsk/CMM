package net.kiko.client.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kiko.KikoMod;
import net.kiko.drone.DroneEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Draws a player wearing the drone as the drone (the normal player model is not drawn at all). */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DroneRenderEvents {

    private static final float SCALE = 1.25F;
    private static final float HOVER = 1.0F; // height of the drone above the player's feet, in blocks

    private static DroneModel model;

    private DroneRenderEvents() {}

    private static DroneModel model() {
        if (model == null) {
            model = new DroneModel(Minecraft.getInstance().getEntityModels().bakeLayer(DroneModel.LAYER));
        }
        return model;
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!DroneEvents.isWearing(player)) return;
        event.setCanceled(true);

        float partial = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();

        float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        Vec3 v = player.getDeltaMovement();
        double rad = Math.toRadians(yaw);
        double forward = v.x * -Math.sin(rad) + v.z * Math.cos(rad);
        double side = v.x * Math.cos(rad) + v.z * Math.sin(rad);
        float pitch = (float) Mth.clamp(forward * 90.0D, -25.0D, 25.0D);
        float roll = (float) Mth.clamp(side * 90.0D, -25.0D, 25.0D);

        pose.pushPose();
        pose.translate(0.0D, HOVER, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.mulPose(Axis.XP.rotationDegrees(-pitch));
        pose.mulPose(Axis.ZP.rotationDegrees(roll));
        pose.scale(-SCALE, -SCALE, SCALE);

        DroneModel m = model();
        m.spin(player.tickCount + partial);
        VertexConsumer vc = buffer.getBuffer(m.renderType(DroneModel.TEXTURE));
        m.renderToBuffer(pose, vc, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    /** No floating hand while you are a drone. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && DroneEvents.isWearing(player)) event.setCanceled(true);
    }
}
