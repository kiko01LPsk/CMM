package net.kiko.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.KikoMod;
import net.kiko.item.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Draws the lenses of "Sunglasses 2" half transparent. Worn armor is always drawn without blending, so the
 * armor texture only holds the frame and the lenses are drawn here on a translucent render type.
 */
public class SunglassesLensLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation LENS =
            new ResourceLocation(KikoMod.MOD_ID, "textures/models/armor/sunglasses2_lens.png");

    private final HumanoidModel<T> armorModel;

    public SunglassesLensLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent);
        this.armorModel = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SUNGLASSES2.get())) return;
        getParentModel().copyPropertiesTo(armorModel);
        armorModel.setAllVisible(false);
        armorModel.head.visible = true;
        armorModel.hat.visible = true;
        armorModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(LENS)),
                light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
