package net.kiko.client.walkman;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.item.WalkmanItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the worn walkman on the belt, at the front left of the waist. */
public class WalkmanLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float SCALE = 0.55F;

    public WalkmanLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof WalkmanItem)) return;

        poseStack.pushPose();
        // Same space as the body parts (+x is the player's left, -z is the front); the pivot is the top of the torso.
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(2.2D / 16.0D, 10.0D / 16.0D, -2.4D / 16.0D);
        // Proper rotation (no mirroring): model up -> player up, the cassette window faces forward.
        poseStack.scale(-SCALE, -SCALE, SCALE);
        // Standing upright on the belt (turned a quarter turn from the flat model).
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
        poseStack.popPose();
    }
}
