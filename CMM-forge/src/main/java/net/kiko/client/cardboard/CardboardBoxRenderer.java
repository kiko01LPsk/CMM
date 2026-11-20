package net.kiko.client.cardboard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.block.ModBlocks;
import net.kiko.cardboard.CardboardBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/** Draws the drivable box with the model of the open cardboard box block. */
public class CardboardBoxRenderer extends EntityRenderer<CardboardBoxEntity> {

    private final EntityRendererProvider.Context context;

    public CardboardBoxRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
        this.shadowRadius = 0.6F;
    }

    @Override
    public void render(CardboardBoxEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        context.getBlockRenderDispatcher().renderSingleBlock(
                ModBlocks.CARDBOARD_BOX_OPEN.get().defaultBlockState(), poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CardboardBoxEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
