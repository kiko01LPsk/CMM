package net.kiko.client.mannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.animal.IronGolem;
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

    private static final ResourceLocation[] MOB_TEXTURES = {
            null,
            new ResourceLocation("textures/entity/iron_golem/iron_golem.png"),
            new ResourceLocation("textures/entity/villager/villager.png"),
            new ResourceLocation("textures/entity/creeper/creeper.png")};

    /** Mob models by Mannequin.MODEL_*; index 0 is unused. They are only posed by hand, never animated. */
    private final HierarchicalModel<?>[] mobModels = new HierarchicalModel<?>[Mannequin.MODEL_COUNT];

    private final MannequinModel wideModel;
    private final MannequinModel slimModel;

    public MannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new MannequinModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.4F);
        this.wideModel = this.model;
        this.slimModel = new MannequinModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        mobModels[Mannequin.MODEL_GOLEM] = new IronGolemModel<IronGolem>(context.bakeLayer(ModelLayers.IRON_GOLEM));
        mobModels[Mannequin.MODEL_VILLAGER] = new VillagerModel<Mannequin>(context.bakeLayer(ModelLayers.VILLAGER));
        mobModels[Mannequin.MODEL_CREEPER] = new CreeperModel<Mannequin>(context.bakeLayer(ModelLayers.CREEPER));

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
        if (mannequin.getModelType() != Mannequin.MODEL_PLAYER) {
            renderMob(mannequin, partialTick, poseStack, buffer, packedLight);
            return;
        }
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

    private static void pose(ModelPart part, Rotations r, float extraX) {
        if (part == null) return;
        part.xRot = (float) Math.toRadians(r.getX()) + extraX;
        part.yRot = (float) Math.toRadians(r.getY());
        part.zRot = (float) Math.toRadians(r.getZ());
    }

    private static ModelPart child(ModelPart root, String name) {
        return root.hasChild(name) ? root.getChild(name) : null;
    }

    /** Iron golem, villager and creeper: the head, body, arms and legs follow the mannequin's pose. */
    private void renderMob(Mannequin mannequin, float partialTick, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight) {
        int type = mannequin.getModelType();
        HierarchicalModel<?> model = mobModels[type];
        ModelPart root = model.root();

        pose(child(root, "head"), mannequin.getHeadPose(), 0.0F);
        pose(child(root, "body"), mannequin.getBodyPose(), 0.0F);
        switch (type) {
            case Mannequin.MODEL_GOLEM -> {
                pose(child(root, "left_arm"), mannequin.getLeftArmPose(), 0.0F);
                pose(child(root, "right_arm"), mannequin.getRightArmPose(), 0.0F);
                pose(child(root, "left_leg"), mannequin.getLeftLegPose(), 0.0F);
                pose(child(root, "right_leg"), mannequin.getRightLegPose(), 0.0F);
            }
            case Mannequin.MODEL_VILLAGER -> {
                // The villager's arms are one crossed pair; they stay as the game draws them.
                pose(child(root, "left_leg"), mannequin.getLeftLegPose(), 0.0F);
                pose(child(root, "right_leg"), mannequin.getRightLegPose(), 0.0F);
            }
            default -> {
                pose(child(root, "left_front_leg"), mannequin.getLeftArmPose(), 0.0F);
                pose(child(root, "right_front_leg"), mannequin.getRightArmPose(), 0.0F);
                pose(child(root, "left_hind_leg"), mannequin.getLeftLegPose(), 0.0F);
                pose(child(root, "right_hind_leg"), mannequin.getRightLegPose(), 0.0F);
            }
        }

        float yaw = Mth.rotLerp(partialTick, mannequin.yRotO, mannequin.getYRot());
        float s = mannequin.getEntityScale() * (mannequin.isSmall() ? 0.5F : 1.0F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        if (mannequin.getTiltX() != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(mannequin.getTiltX()));
        if (mannequin.getTiltZ() != 0.0F) poseStack.mulPose(Axis.ZP.rotationDegrees(mannequin.getTiltZ()));
        poseStack.scale(-s, -s, s);
        poseStack.translate(0.0D, -1.501D, 0.0D);
        ResourceLocation texture = MannequinSkins.get(mannequin.getSkin(), MOB_TEXTURES[type], false);
        VertexConsumer consumer = buffer.getBuffer(mannequin.isInvisible()
                ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture));
        if (!mannequin.isInvisible()) {
            model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();
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
        if (mannequin.overrideTexture != null) return mannequin.overrideTexture;
        return MannequinSkins.get(mannequin.getSkin(), TEXTURE);
    }

    /** Like the armor stand: a name is only shown when it was given one and made it visible. */
    @Override
    protected boolean shouldShowName(Mannequin mannequin) {
        return mannequin.isCustomNameVisible() && this.entityRenderDispatcher.distanceToSqr(mannequin) < 4096.0D;
    }
}
