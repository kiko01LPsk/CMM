package net.kiko.client.mjolnir;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.item.ModItems;
import net.kiko.mjolnir.ThrownMjolnir;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the thrown hammer tumbling end over end along its flight. */
public class ThrownMjolnirRenderer extends EntityRenderer<ThrownMjolnir> {

    private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    private ItemStack stack;

    public ThrownMjolnirRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownMjolnir entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        if (stack == null) stack = new ItemStack(ModItems.MJOLNIR.get());
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        // Flies straight, head first: the hammer's top (+y) points along the flight.
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.translate(0.0D, -0.031D, 0.0D);
        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
                light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownMjolnir entity) {
        return BLOCK_ATLAS;
    }
}
