package net.kiko.client.display;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.block.DisplayCaseBlock;
import net.kiko.block.DisplayCaseBlockEntity;
import net.kiko.block.WallBookDisplayBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Draws the item inside a display case (turning slowly) or the book on a wall book display (flat on the board). */
public class DisplayCaseRenderer implements BlockEntityRenderer<DisplayCaseBlockEntity> {

    public DisplayCaseRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DisplayCaseBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = be.getItem();
        if (stack.isEmpty() || be.getLevel() == null) return;
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof DisplayCaseBlock block)) return;
        poseStack.pushPose();
        int lit = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos());
        if (state.getBlock() instanceof WallBookDisplayBlock) {
            Direction facing = state.getValue(WallBookDisplayBlock.FACING);
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
            poseStack.translate(0.0, 0.0, 0.355);
            poseStack.scale(0.7F, 0.7F, 0.7F);
        } else {
            poseStack.translate(0.5, block.getItemY(), 0.5);
            float time = net.minecraft.Util.getMillis() / 50.0F; // own clock: the server world time may not tick smoothly on the client
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0F));
            poseStack.scale(block.getItemScale(), block.getItemScale(), block.getItemScale());
            lit = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, lit, overlay,
                poseStack, buffer, be.getLevel(), 0);
        poseStack.popPose();
    }
}
