package net.kiko.client.chair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.chair.ThrownChair;
import net.kiko.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The chair tumbles end over end while it flies. */
public class ThrownChairRenderer extends EntityRenderer<ThrownChair> {

    private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    private ItemStack stack;

    public ThrownChairRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownChair entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        if (stack == null) stack = new ItemStack(ModItems.PLASTIC_CHAIR.get());
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * 40.0F)); // end over end
        poseStack.scale(0.9F, 0.9F, 0.9F);
        // the model sits in a block: move its middle (8, 8, 8) to the entity
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
                light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownChair entity) {
        return BLOCK_ATLAS;
    }
}
