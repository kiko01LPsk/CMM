package net.kiko.client.shield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.item.ModItems;
import net.kiko.shield.ThrownShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The thrown shield flies flat like a disc, spinning around its own middle. */
public class ThrownShieldRenderer extends EntityRenderer<ThrownShield> {

    private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    private ItemStack stack;

    public ThrownShieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownShield entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        if (stack == null) stack = new ItemStack(ModItems.CAPTAIN_SHIELD.get());
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.1D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 55.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE,
                light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownShield entity) {
        return BLOCK_ATLAS;
    }
}
