package net.kiko.client.trident;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.trident.ThrownKikoTrident;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/** Draws the thrown trident / spear with its own 3D item model, tip first. */
public class ThrownKikoTridentRenderer extends EntityRenderer<ThrownKikoTrident> {

    private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");

    public ThrownKikoTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownKikoTrident entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        // The tip of the model (+y) points along the flight.
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(entity.getRenderStack(), ItemDisplayContext.NONE,
                light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownKikoTrident entity) {
        return BLOCK_ATLAS;
    }
}
