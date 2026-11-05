package net.kiko.client.guitar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.guitar.ClientGuitarBackStore;
import net.kiko.item.GuitarItem;
import net.kiko.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the guitar on the player's back (face outwards, neck pointing down and to the left). */
public class GuitarBackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float SCALE = 0.55F;
    private static final float TILT = 20.0F;
    private static ItemStack stack;

    public GuitarBackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ClientGuitarBackStore.isOnBack(player.getUUID())) return;
        if (player.getMainHandItem().getItem() instanceof GuitarItem
                || player.getOffhandItem().getItem() instanceof GuitarItem) return;
        if (stack == null) stack = new ItemStack(ModItems.GUITAR.get());

        poseStack.pushPose();
        // Body space: +x is the player's left, +y is down, +z is the back.
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(5.3D / 16.0D, 13.0D / 16.0D, 2.3D / 16.0D);
        // Neck leans toward the player's left.
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT));
        // The flat guitar: front face -> back of the player, headstock -> down.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(SCALE, SCALE, SCALE);
        // The model's centre of the guitar is a little below the middle of the item cube.
        poseStack.translate(0.0D, 6.7D / 16.0D, 0.0D);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
        poseStack.popPose();
    }
}
