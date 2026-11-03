package net.kiko.client.mannequin;

import net.kiko.mannequin.Mannequin;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Rotations;

/** The player model, posed from the mannequin's armor-stand pose instead of from walking animations. */
public class MannequinModel extends PlayerModel<Mannequin> {

    private final float armY;

    public MannequinModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.armY = this.leftArm.y;
    }

    private static void pose(ModelPart part, Rotations rotations) {
        part.xRot = (float) Math.toRadians(rotations.getX());
        part.yRot = (float) Math.toRadians(rotations.getY());
        part.zRot = (float) Math.toRadians(rotations.getZ());
    }

    @Override
    public void setupAnim(Mannequin mannequin, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        pose(head, mannequin.getHeadPose());
        pose(body, mannequin.getBodyPose());
        pose(leftArm, mannequin.getLeftArmPose());
        pose(rightArm, mannequin.getRightArmPose());
        pose(leftLeg, mannequin.getLeftLegPose());
        pose(rightLeg, mannequin.getRightLegPose());

        this.crouching = mannequin.isMannequinCrouching();
        if (crouching) {
            body.xRot += 0.5F;
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
            rightLeg.z = 4.0F;
            leftLeg.z = 4.0F;
            rightLeg.y = 12.2F;
            leftLeg.y = 12.2F;
            head.y = 4.2F;
            body.y = 3.2F;
            leftArm.y = armY + 3.2F;
            rightArm.y = armY + 3.2F;
        } else {
            rightLeg.z = 0.1F;
            leftLeg.z = 0.1F;
            rightLeg.y = 12.0F;
            leftLeg.y = 12.0F;
            head.y = 0.0F;
            body.y = 0.0F;
            leftArm.y = armY;
            rightArm.y = armY;
        }

        hat.copyFrom(head);
        jacket.copyFrom(body);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
    }
}
