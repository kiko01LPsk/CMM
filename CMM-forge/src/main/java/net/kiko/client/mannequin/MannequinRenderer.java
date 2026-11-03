package net.kiko.client.mannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kiko.KikoMod;
import net.kiko.mannequin.Mannequin;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;

/** Draws the mannequin as a plain white mannequin (no skin), with armor, items, elytra and head items. */
public class MannequinRenderer extends LivingEntityRenderer<Mannequin, MannequinModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(KikoMod.MOD_ID, "textures/entity/mannequin.png");

    private final MannequinModel wideModel;
    private final MannequinModel slimModel;

    public MannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new MannequinModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.4F);
        this.wideModel = this.model;
        this.slimModel = new MannequinModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public void render(Mannequin mannequin, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        MannequinModel m = mannequin.slimArms() ? slimModel : wideModel;
        this.model = m;
        m.setAllVisible(true);
        m.hat.visible = mannequin.isPartShown(PlayerModelPart.HAT);
        m.jacket.visible = mannequin.isPartShown(PlayerModelPart.JACKET);
        m.leftSleeve.visible = mannequin.isPartShown(PlayerModelPart.LEFT_SLEEVE);
        m.rightSleeve.visible = mannequin.isPartShown(PlayerModelPart.RIGHT_SLEEVE);
        m.leftPants.visible = mannequin.isPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        m.rightPants.visible = mannequin.isPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        this.shadowRadius = 0.4F * mannequin.getEntityScale();
        super.render(mannequin, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(Mannequin mannequin, PoseStack poseStack, float partialTick) {
        float s = 0.9375F * mannequin.getEntityScale();
        poseStack.scale(s, s, s);
    }

    @Override
    protected void setupRotations(Mannequin mannequin, PoseStack poseStack, float bob, float yBodyRot, float partialTick) {
        super.setupRotations(mannequin, poseStack, bob, Mth.rotLerp(partialTick, mannequin.yRotO, mannequin.getYRot()), partialTick);
        if (mannequin.getTiltX() != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(mannequin.getTiltX()));
        if (mannequin.getTiltZ() != 0.0F) poseStack.mulPose(Axis.ZP.rotationDegrees(mannequin.getTiltZ()));
    }

    @Override
    public ResourceLocation getTextureLocation(Mannequin mannequin) {
        return MannequinSkins.get(mannequin.getSkin(), TEXTURE);
    }

    /** Like the armor stand: a name is only shown when it was given one and made it visible. */
    @Override
    protected boolean shouldShowName(Mannequin mannequin) {
        return mannequin.isCustomNameVisible() && this.entityRenderDispatcher.distanceToSqr(mannequin) < 4096.0D;
    }
}
