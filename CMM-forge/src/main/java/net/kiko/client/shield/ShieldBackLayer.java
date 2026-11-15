package net.kiko.client.shield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.item.ModItems;
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

/** Draws Captain America's shield on the back of a player who wears it in the chestplate slot. */
public class ShieldBackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float SCALE = 1.0F;

    public ShieldBackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!worn.is(ModItems.CAPTAIN_SHIELD.get())) return;

        poseStack.pushPose();
        // Body space: +x is the player's left, +y is down, +z is the back.
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 6.0D / 16.0D, 0.10D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // upright, star facing backwards
        poseStack.scale(SCALE, SCALE, SCALE);
        // the shield model is centred on the corner of its block
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Minecraft.getInstance().getItemRenderer().renderStatic(worn, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
        poseStack.popPose();
    }
}
