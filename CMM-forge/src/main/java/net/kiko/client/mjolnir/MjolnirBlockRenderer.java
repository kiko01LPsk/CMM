package net.kiko.client.mjolnir;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.block.MjolnirBlock;
import net.kiko.block.MjolnirBlockEntity;
import net.kiko.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws Mjolnir standing head-down in the ground with a little tilt, like in the movies. */
public class MjolnirBlockRenderer implements BlockEntityRenderer<MjolnirBlockEntity> {

    /** How far the handle leans away from straight up, in degrees. */
    private static final float TILT = 9.0F;
    /** Height of the model centre above the top of the head (blocks) when the head is down: (22.3 - 8) / 16. The head top is at model y = 22 after the grip shift, so this lets it sit on the floor. */
    private static final float CENTRE_ABOVE_HEAD = 0.89375F;
    private static final float SCALE = 0.7F;

    private ItemStack stack;

    public MjolnirBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MjolnirBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        if (stack == null) stack = new ItemStack(ModItems.MJOLNIR.get());
        int rotation = be.getBlockState().getValue(MjolnirBlock.ROTATION);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation * 22.5F));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0.0D, CENTRE_ABOVE_HEAD, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, overlay,
                poseStack, buffer, be.getLevel(), (int) be.getBlockPos().asLong());
        poseStack.popPose();
    }
}
