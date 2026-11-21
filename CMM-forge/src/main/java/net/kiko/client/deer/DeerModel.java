package net.kiko.client.deer;

import net.kiko.KikoMod;
import net.kiko.deer.DeerEntity;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Deer model, ported from the Deer Mod by Arisuwu Mei (GPL-3.0). */
public class DeerModel extends AgeableHierarchicalModel<DeerEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(KikoMod.MOD_ID, "deer"), "main");

    private static final AnimationDefinition EAT_GRASS = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(105.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(95.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0833F, KeyframeAnimations.degreeVec(105.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.degreeVec(95.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4167F, KeyframeAnimations.degreeVec(105.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    private final ModelPart root;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart redNose;
    private final ModelPart antlers;
    private final ModelPart body;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart saddle;

    public DeerModel(ModelPart root) {
        super(0.6F, 16.0F);
        this.root = root;
        this.neck = root.getChild("neck");
        this.head = this.neck.getChild("head");
        this.redNose = this.head.getChild("red_nose");
        this.antlers = this.head.getChild("antlers");
        this.body = root.getChild("body");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.saddle = root.getChild("saddle");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, -11.0F));
        neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(32, 43).addBox(-2.0F, -10.0F, -1.5F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, -0.5F, 0.2356F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 31).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(14, 47).addBox(-2.0F, 0.0F, -6.0F, 4.0F, 3.0F, 3.0F), PartPose.offset(0.0F, -13.0F, -2.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 2.0F, -5.0F));
        head.addOrReplaceChild("red_nose", CubeListBuilder.create().texOffs(0, 57)
                .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, -5.0F));

        PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.0F, -2.0F, 1.0F));
        rightEar.addOrReplaceChild("ear_r1", CubeListBuilder.create()
                .texOffs(6, 54).addBox(-4.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(22, 53).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.6981F));
        PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.0F, -2.0F, 1.0F));
        leftEar.addOrReplaceChild("ear_r2", CubeListBuilder.create()
                .texOffs(6, 54).mirror().addBox(2.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(22, 53).mirror().addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, -0.6981F));

        PartDefinition antlers = head.addOrReplaceChild("antlers", CubeListBuilder.create(), PartPose.offset(0.0F, 28.0F, 12.0F));
        antlers.addOrReplaceChild("antlers_l", CubeListBuilder.create()
                .texOffs(28, 47).mirror().addBox(0.0F, -2.0F, -23.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(28, 47).mirror().addBox(1.0F, -4.0F, -22.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(28, 47).mirror().addBox(2.0F, -5.0F, -21.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(28, 50).mirror().addBox(2.0F, -7.0F, -22.0F, 1.0F, 1.0F, 1.0F).mirror(false)
                .texOffs(28, 47).mirror().addBox(3.0F, -6.0F, -21.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(28, 47).mirror().addBox(4.0F, -7.0F, -22.0F, 1.0F, 2.0F, 1.0F).mirror(false)
                .texOffs(28, 50).mirror().addBox(5.0F, -8.0F, -23.0F, 1.0F, 1.0F, 1.0F).mirror(false)
                .texOffs(28, 50).mirror().addBox(0.0F, -5.0F, -21.0F, 1.0F, 1.0F, 1.0F).mirror(false),
                PartPose.offset(1.0F, -31.0F, 11.0F));
        antlers.addOrReplaceChild("antlers_r", CubeListBuilder.create()
                .texOffs(28, 47).addBox(-2.0F, -33.0F, -12.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(28, 47).addBox(-3.0F, -35.0F, -11.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(28, 47).addBox(-4.0F, -36.0F, -10.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(28, 50).addBox(-4.0F, -38.0F, -11.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(28, 47).addBox(-5.0F, -37.0F, -10.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(28, 47).addBox(-6.0F, -38.0F, -11.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(28, 50).addBox(-7.0F, -39.0F, -12.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(28, 50).addBox(-2.0F, -36.0F, -11.0F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-2.0F, -21.0F, 8.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(14, 53).addBox(-1.0F, -23.0F, 10.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(0, 0).addBox(-4.0F, -19.0F, -13.0F, 8.0F, 9.0F, 22.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 43).addBox(-0.5F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(-3.5F, -10.0F, -10.0F));
        body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 43).mirror().addBox(-1.5F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F).mirror(false),
                PartPose.offset(3.5F, -10.0F, -10.0F));
        body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 43).addBox(-0.5F, 0.0F, -2.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(-3.5F, -10.0F, 8.0F));
        body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 43).mirror().addBox(-1.5F, 0.0F, -2.0F, 2.0F, 10.0F, 2.0F).mirror(false),
                PartPose.offset(3.5F, -10.0F, 8.0F));

        root.addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(0, 31)
                .addBox(-4.0F, -5.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.22F)), PartPose.offset(0.0F, 10.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DeerEntity deer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.redNose.visible = deer.hasRedNose();
        this.antlers.visible = !deer.isSheared() && !deer.isBaby();
        this.saddle.visible = deer.isSaddled();
        this.neck.resetPose();
        this.animate(deer.eatGrassAnimationState, EAT_GRASS, ageInTicks);
    }
}
