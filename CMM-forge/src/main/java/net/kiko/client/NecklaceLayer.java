package net.kiko.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.KikoMod;
import net.kiko.item.NecklaceItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/** Draws the necklace as a skin-layout texture over the torso (the texture is laid out like a player skin). */
public class NecklaceLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(KikoMod.MOD_ID, "textures/models/necklace_satanova.png");

    public NecklaceLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof NecklaceItem)) return;
        var body = getParentModel().body;
        poseStack.pushPose();
        // slightly bigger than the body so it sits on top of the skin (scaled around the body's own pivot)
        float px = body.x / 16.0F, py = body.y / 16.0F, pz = body.z / 16.0F;
        poseStack.translate(px, py, pz);
        poseStack.scale(1.04F, 1.0F, 1.08F);
        poseStack.translate(-px, -py, -pz);
        var consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        body.render(poseStack, consumer, light, LivingEntityRenderer.getOverlayCoords(player, 0.0F));
        poseStack.popPose();
    }
}
