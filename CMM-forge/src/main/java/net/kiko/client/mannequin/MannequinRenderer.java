package net.kiko.client.mannequin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.piglin.Piglin;
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

    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");

    private static ResourceLocation tex(String path) {
        return new ResourceLocation("textures/entity/" + path + ".png");
    }

    /** Vanilla textures by Mannequin.MODEL_*; index 0 (player) and the custom model have none. */
    private static final ResourceLocation[] MOB_TEXTURES = {
            null,
            tex("iron_golem/iron_golem"),
            tex("villager/villager"),
            tex("creeper/creeper"),
            tex("zombie/zombie"),
            tex("zombie/husk"),
            tex("zombie/drowned"),
            tex("skeleton/skeleton"),
            tex("skeleton/wither_skeleton"),
            tex("illager/pillager"),
            tex("illager/vindicator"),
            tex("enderman/enderman"),
            tex("piglin/piglin"),
            tex("witch"),
            tex("pig/pig"),
            tex("cow/cow"),
            WHITE,
            tex("chicken"),
            tex("sheep/sheep"),
            tex("wolf/wolf"),
            tex("illager/evoker"),
            tex("piglin/zombified_piglin")};

    private static final ResourceLocation SHEEP_FUR = tex("sheep/sheep_fur");

    /** Mob models by Mannequin.MODEL_*; they are only posed by hand, never animated. */
    private final Model[] mobModels = new Model[Mannequin.MODEL_COUNT];
    private final ModelPart[] mobRoots = new ModelPart[Mannequin.MODEL_COUNT];
    private final Model sheepFur;
    private final ModelPart sheepFurRoot;

    private final MannequinModel wideModel;
    private final MannequinModel slimModel;
    /** The game's own player model layers: other mods that change players change the mannequin too. */
    private final MannequinModel playerWide;
    private final MannequinModel playerSlim;

    public MannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new MannequinModel(context.bakeLayer(MannequinLayers.WIDE), false), 0.4F);
        this.wideModel = this.model;
        this.slimModel = new MannequinModel(context.bakeLayer(MannequinLayers.SLIM), true);
        this.playerWide = new MannequinModel(context.bakeLayer(ModelLayers.PLAYER), false);
        this.playerSlim = new MannequinModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        ModelPart golem = context.bakeLayer(ModelLayers.IRON_GOLEM);
        mobModels[Mannequin.MODEL_GOLEM] = new IronGolemModel<IronGolem>(golem);
        ModelPart villager = context.bakeLayer(ModelLayers.VILLAGER);
        mobModels[Mannequin.MODEL_VILLAGER] = new VillagerModel<Mannequin>(villager);
        ModelPart creeper = context.bakeLayer(ModelLayers.CREEPER);
        mobModels[Mannequin.MODEL_CREEPER] = new CreeperModel<Mannequin>(creeper);
        ModelPart zombie = context.bakeLayer(ModelLayers.ZOMBIE);
        mobModels[Mannequin.MODEL_ZOMBIE] = new ZombieModel<Zombie>(zombie);
        mobModels[Mannequin.MODEL_HUSK] = mobModels[Mannequin.MODEL_ZOMBIE];
        ModelPart drowned = context.bakeLayer(ModelLayers.DROWNED);
        mobModels[Mannequin.MODEL_DROWNED] = new DrownedModel<Drowned>(drowned);
        ModelPart skeleton = context.bakeLayer(ModelLayers.SKELETON);
        mobModels[Mannequin.MODEL_SKELETON] = new SkeletonModel<Skeleton>(skeleton);
        mobModels[Mannequin.MODEL_WITHER_SKELETON] = mobModels[Mannequin.MODEL_SKELETON];
        ModelPart pillager = context.bakeLayer(ModelLayers.PILLAGER);
        mobModels[Mannequin.MODEL_PILLAGER] = new IllagerModel<Pillager>(pillager);
        mobModels[Mannequin.MODEL_VINDICATOR] = mobModels[Mannequin.MODEL_PILLAGER];
        ModelPart enderman = context.bakeLayer(ModelLayers.ENDERMAN);
        mobModels[Mannequin.MODEL_ENDERMAN] = new EndermanModel<EnderMan>(enderman);
        ModelPart piglin = context.bakeLayer(ModelLayers.PIGLIN);
        mobModels[Mannequin.MODEL_PIGLIN] = new PiglinModel<Piglin>(piglin);
        ModelPart witch = context.bakeLayer(ModelLayers.WITCH);
        mobModels[Mannequin.MODEL_WITCH] = new WitchModel<Witch>(witch);
        ModelPart pig = context.bakeLayer(ModelLayers.PIG);
        mobModels[Mannequin.MODEL_PIG] = new PigModel<Pig>(pig);
        ModelPart cow = context.bakeLayer(ModelLayers.COW);
        mobModels[Mannequin.MODEL_COW] = new CowModel<Cow>(cow);
        ModelPart chicken = context.bakeLayer(ModelLayers.CHICKEN);
        mobModels[Mannequin.MODEL_CHICKEN] = new ChickenModel<Chicken>(chicken);
        ModelPart sheep = context.bakeLayer(ModelLayers.SHEEP);
        mobModels[Mannequin.MODEL_SHEEP] = new SheepModel<Sheep>(sheep);
        sheepFurRoot = context.bakeLayer(ModelLayers.SHEEP_FUR);
        sheepFur = new SheepFurModel<Sheep>(sheepFurRoot);
        ModelPart wolf = context.bakeLayer(ModelLayers.WOLF);
        mobModels[Mannequin.MODEL_WOLF] = new WolfModel<Wolf>(wolf);
        ModelPart evoker = context.bakeLayer(ModelLayers.EVOKER);
        mobModels[Mannequin.MODEL_EVOKER] = new IllagerModel<Evoker>(evoker);
        ModelPart zombifiedPiglin = context.bakeLayer(ModelLayers.ZOMBIFIED_PIGLIN);
        mobModels[Mannequin.MODEL_ZOMBIFIED_PIGLIN] = new PiglinModel<ZombifiedPiglin>(zombifiedPiglin);
        mobRoots[Mannequin.MODEL_CHICKEN] = chicken;
        mobRoots[Mannequin.MODEL_SHEEP] = sheep;
        mobRoots[Mannequin.MODEL_WOLF] = wolf;
        mobRoots[Mannequin.MODEL_EVOKER] = evoker;
        mobRoots[Mannequin.MODEL_ZOMBIFIED_PIGLIN] = zombifiedPiglin;
        mobRoots[Mannequin.MODEL_GOLEM] = golem;
        mobRoots[Mannequin.MODEL_VILLAGER] = villager;
        mobRoots[Mannequin.MODEL_CREEPER] = creeper;
        mobRoots[Mannequin.MODEL_ZOMBIE] = zombie;
        mobRoots[Mannequin.MODEL_HUSK] = zombie;
        mobRoots[Mannequin.MODEL_DROWNED] = drowned;
        mobRoots[Mannequin.MODEL_SKELETON] = skeleton;
        mobRoots[Mannequin.MODEL_WITHER_SKELETON] = skeleton;
        mobRoots[Mannequin.MODEL_PILLAGER] = pillager;
        mobRoots[Mannequin.MODEL_VINDICATOR] = pillager;
        mobRoots[Mannequin.MODEL_ENDERMAN] = enderman;
        mobRoots[Mannequin.MODEL_PIGLIN] = piglin;
        mobRoots[Mannequin.MODEL_WITCH] = witch;
        mobRoots[Mannequin.MODEL_PIG] = pig;
        mobRoots[Mannequin.MODEL_COW] = cow;

        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(MannequinLayers.INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(MannequinLayers.OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public void render(Mannequin mannequin, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        int type = mannequin.getModelType();
        if (type == Mannequin.MODEL_CUSTOM && CustomModelCodec.get(mannequin.getCustomModel()) != null) {
            renderCustom(mannequin, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (type != Mannequin.MODEL_PLAYER && type != Mannequin.MODEL_CUSTOM) {
            renderMob(mannequin, partialTick, poseStack, buffer, packedLight);
            return;
        }
        MannequinModel m = mannequin.usesPlayerModel()
                ? (mannequin.slimArms() ? playerSlim : playerWide)
                : (mannequin.slimArms() ? slimModel : wideModel);
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

    private static boolean isHumanoid(int type) {
        return type == Mannequin.MODEL_ZOMBIE || type == Mannequin.MODEL_HUSK || type == Mannequin.MODEL_DROWNED
                || type == Mannequin.MODEL_SKELETON || type == Mannequin.MODEL_WITHER_SKELETON
                || type == Mannequin.MODEL_ENDERMAN || type == Mannequin.MODEL_PIGLIN
                || type == Mannequin.MODEL_ZOMBIFIED_PIGLIN;
    }

    private static boolean isIllager(int type) {
        return type == Mannequin.MODEL_PILLAGER || type == Mannequin.MODEL_VINDICATOR || type == Mannequin.MODEL_EVOKER;
    }

    private static boolean isCrossedArms(int type) {
        return type == Mannequin.MODEL_VILLAGER || type == Mannequin.MODEL_WITCH;
    }

    /** Poses the parts of a mob model from the mannequin's pose. */
    private static void poseMob(int type, ModelPart root, Mannequin mannequin) {
        pose(child(root, "head"), mannequin.getHeadPose(), 0.0F);
        pose(child(root, "hat"), mannequin.getHeadPose(), 0.0F);
        boolean quadruped = type == Mannequin.MODEL_PIG || type == Mannequin.MODEL_COW
                || type == Mannequin.MODEL_SHEEP || type == Mannequin.MODEL_WOLF;
        boolean chicken = type == Mannequin.MODEL_CHICKEN;
        // The body of the four-legged animals and the chicken is laid down by the model itself.
        if (!quadruped && !chicken) pose(child(root, "body"), mannequin.getBodyPose(), 0.0F);
        if (isIllager(type)) {
            // Real arms (not the crossed ones), so they follow the pose like on the other humanoids.
            ModelPart crossed = child(root, "arms");
            if (crossed != null) crossed.visible = false;
            ModelPart left = child(root, "left_arm");
            ModelPart right = child(root, "right_arm");
            if (left != null) left.visible = true;
            if (right != null) right.visible = true;
        }
        if (chicken) {
            pose(child(root, "left_wing"), mannequin.getLeftArmPose(), 0.0F);
            pose(child(root, "right_wing"), mannequin.getRightArmPose(), 0.0F);
            pose(child(root, "left_leg"), mannequin.getLeftLegPose(), 0.0F);
            pose(child(root, "right_leg"), mannequin.getRightLegPose(), 0.0F);
        } else if (type == Mannequin.MODEL_GOLEM || isHumanoid(type) || isIllager(type)) {
            pose(child(root, "left_arm"), mannequin.getLeftArmPose(), 0.0F);
            pose(child(root, "right_arm"), mannequin.getRightArmPose(), 0.0F);
            pose(child(root, "left_leg"), mannequin.getLeftLegPose(), 0.0F);
            pose(child(root, "right_leg"), mannequin.getRightLegPose(), 0.0F);
            // The piglin is built on the player model: its overlay parts follow.
            pose(child(root, "jacket"), mannequin.getBodyPose(), 0.0F);
            pose(child(root, "left_sleeve"), mannequin.getLeftArmPose(), 0.0F);
            pose(child(root, "right_sleeve"), mannequin.getRightArmPose(), 0.0F);
            pose(child(root, "left_pants"), mannequin.getLeftLegPose(), 0.0F);
            pose(child(root, "right_pants"), mannequin.getRightLegPose(), 0.0F);
        } else if (isCrossedArms(type)) {
            // The crossed arms stay as the game draws them; the single arms are hidden.
            pose(child(root, "left_leg"), mannequin.getLeftLegPose(), 0.0F);
            pose(child(root, "right_leg"), mannequin.getRightLegPose(), 0.0F);
            ModelPart left = child(root, "left_arm");
            ModelPart right = child(root, "right_arm");
            ModelPart both = child(root, "arms");
            if (left != null) left.visible = both == null;
            if (right != null) right.visible = both == null;
            if (both != null) both.visible = true;
        } else {
            pose(child(root, "left_front_leg"), mannequin.getLeftArmPose(), 0.0F);
            pose(child(root, "right_front_leg"), mannequin.getRightArmPose(), 0.0F);
            pose(child(root, "left_hind_leg"), mannequin.getLeftLegPose(), 0.0F);
            pose(child(root, "right_hind_leg"), mannequin.getRightLegPose(), 0.0F);
        }
    }

    /** Draws an uploaded model: bones named head, body, left/right arm and leg follow the mannequin's pose. */
    private void renderCustom(Mannequin mannequin, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight) {
        CustomModelCodec.Built built = CustomModelCodec.get(mannequin.getCustomModel());
        poseCustom(built, mannequin);
        float yaw = Mth.rotLerp(partialTick, mannequin.yRotO, mannequin.getYRot());
        float s = mannequin.getEntityScale() * (mannequin.isSmall() ? 0.5F : 1.0F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        if (mannequin.getTiltX() != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(mannequin.getTiltX()));
        if (mannequin.getTiltZ() != 0.0F) poseStack.mulPose(Axis.ZP.rotationDegrees(mannequin.getTiltZ()));
        poseStack.scale(-s, -s, s);
        poseStack.translate(0.0D, -1.501D, 0.0D);
        ResourceLocation texture = MannequinSkins.get(mannequin.getSkin(), WHITE, false);
        VertexConsumer consumer = buffer.getBuffer(mannequin.isInvisible()
                ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture));
        if (!mannequin.isInvisible()) {
            built.root.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();
    }

    private static void poseCustom(CustomModelCodec.Built built, Mannequin m) {
        poseBone(built, "head", m.getHeadPose());
        poseBone(built, "body", m.getBodyPose());
        poseBone(built, "leftarm", m.getLeftArmPose());
        poseBone(built, "rightarm", m.getRightArmPose());
        poseBone(built, "leftleg", m.getLeftLegPose());
        poseBone(built, "rightleg", m.getRightLegPose());
    }

    /** The mannequin's pose is added to the bone's own rotation. */
    private static void poseBone(CustomModelCodec.Built built, String name, Rotations r) {
        ModelPart part = built.bones.get(name);
        if (part == null) return;
        float[] rest = built.rest.get(part);
        part.xRot = rest[0] + (float) Math.toRadians(r.getX());
        part.yRot = rest[1] + (float) Math.toRadians(r.getY());
        part.zRot = rest[2] + (float) Math.toRadians(r.getZ());
    }

    /** Iron golem, villager and creeper: the head, body, arms and legs follow the mannequin's pose. */
    private void renderMob(Mannequin mannequin, float partialTick, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight) {
        int type = mannequin.getModelType();
        Model model = mobModels[type];
        ModelPart root = mobRoots[type] != null ? mobRoots[type] : ((HierarchicalModel<?>) model).root();
        poseMob(type, root, mannequin);
        // Models start out as the baby shape; mannequins are always the adult one.
        if (model instanceof EntityModel<?> entityModel) entityModel.young = false;
        if (type == Mannequin.MODEL_SHEEP) {
            poseMob(type, sheepFurRoot, mannequin);
            ((EntityModel<?>) sheepFur).young = false;
        }

        float yaw = Mth.rotLerp(partialTick, mannequin.yRotO, mannequin.getYRot());
        // Mob models are always drawn adult size; the "small" toggle only shrinks the player model.
        float s = mannequin.getEntityScale();
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
            if (type == Mannequin.MODEL_SHEEP) {
                VertexConsumer fur = buffer.getBuffer(RenderType.entityCutoutNoCull(SHEEP_FUR));
                sheepFur.renderToBuffer(poseStack, fur, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
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
