package net.kiko.client.phone;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Arm poses for holding the phone. The numbers are taken from MineDevice's phone poses
 * (browse, selfie, call at the ear, speaker held in front).
 */
public final class PhoneArmPoses {

    /** Looking at the screen. */
    public static final HumanoidModel.ArmPose BROWSE = HumanoidModel.ArmPose.create("KIKO_PHONE_BROWSE", false,
            (model, entity, arm) -> follow(model, arm, -0.9599311F, 0.3F, 0.06F, 0.35F, 0.5F, -1.35F));

    /** Arm stretched out in front, taking a picture of yourself. */
    public static final HumanoidModel.ArmPose SELFIE = HumanoidModel.ArmPose.create("KIKO_PHONE_SELFIE", false,
            (model, entity, arm) -> follow(model, arm, -1.7453293F, 0.12F, 0.12F, 0.8F, 0.92F, -1.9F));

    /** Phone at the ear. */
    public static final HumanoidModel.ArmPose CALL = HumanoidModel.ArmPose.create("KIKO_PHONE_CALL", false,
            (model, entity, arm) -> {
                ModelPart part = part(model, arm);
                float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
                part.xRot = -2.338741F;
                part.yRot = 0.08F * side;
                part.zRot = 0.04F * side;
            });

    /** Phone held out flat in front of the face (speaker / showing the screen). */
    public static final HumanoidModel.ArmPose SPEAKER = HumanoidModel.ArmPose.create("KIKO_PHONE_SPEAKER", false,
            (model, entity, arm) -> {
                ModelPart part = part(model, arm);
                float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
                part.x = -4.25F * side;
                part.z = -1.0F;
                part.xRot = Mth.clamp(model.head.xRot - 1.15F, -1.8F, 0.35F);
                part.yRot = model.head.yRot - 0.22F * side;
                part.zRot = 0.08F * side;
            });

    private PhoneArmPoses() {}

    /**
     * Touching this class creates the new arm poses. It has to happen while the game starts: if they are created later
     * (the first time you use the phone), the game's arm switch has already been built without them and crashes.
     */
    public static void init() {}

    private static ModelPart part(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    /** Raised arm that follows where the head looks. */
    private static void follow(HumanoidModel<?> model, HumanoidArm arm, float pitch, float yaw, float roll,
                               float headPitchFollow, float headYawFollow, float minPitch) {
        ModelPart part = part(model, arm);
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        part.x = -5.0F * side;
        part.y = 0.0F;
        part.z = 0.0F;
        part.xRot = Mth.clamp(model.head.xRot * headPitchFollow + pitch, minPitch, 0.35F);
        part.yRot = model.head.yRot * headYawFollow - yaw * side;
        part.zRot = roll * side - model.head.yRot * 0.08F * side;
    }
}
