package net.kiko.mannequin;

import net.minecraft.core.Rotations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/** Everything the edit screen can change on a mannequin; sent to the server as one packet. */
public class MannequinSettings {

    /** head, body, left arm, right arm, left leg, right leg; x, y, z each (degrees). */
    public final float[] pose = new float[18];
    public float yaw;
    public float scale = 1.0F;
    public float tiltX;
    public float tiltZ;
    public boolean showArms = true;
    public boolean noBasePlate = true;
    public boolean small;
    public boolean crouching;
    public boolean slimArms;
    public byte parts = Mannequin.ALL_PARTS;
    public byte model;
    public boolean playerModel;

    public MannequinSettings() {}

    public MannequinSettings(FriendlyByteBuf buf) {
        for (int i = 0; i < pose.length; i++) pose[i] = buf.readFloat();
        yaw = buf.readFloat();
        scale = buf.readFloat();
        tiltX = buf.readFloat();
        tiltZ = buf.readFloat();
        showArms = buf.readBoolean();
        noBasePlate = buf.readBoolean();
        small = buf.readBoolean();
        crouching = buf.readBoolean();
        slimArms = buf.readBoolean();
        parts = buf.readByte();
        model = buf.readByte();
        playerModel = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        for (float f : pose) buf.writeFloat(f);
        buf.writeFloat(yaw);
        buf.writeFloat(scale);
        buf.writeFloat(tiltX);
        buf.writeFloat(tiltZ);
        buf.writeBoolean(showArms);
        buf.writeBoolean(noBasePlate);
        buf.writeBoolean(small);
        buf.writeBoolean(crouching);
        buf.writeBoolean(slimArms);
        buf.writeByte(parts);
        buf.writeByte(model);
        buf.writeBoolean(playerModel);
    }

    public MannequinSettings copy() {
        MannequinSettings c = new MannequinSettings();
        System.arraycopy(pose, 0, c.pose, 0, pose.length);
        c.yaw = yaw;
        c.scale = scale;
        c.tiltX = tiltX;
        c.tiltZ = tiltZ;
        c.showArms = showArms;
        c.noBasePlate = noBasePlate;
        c.small = small;
        c.crouching = crouching;
        c.slimArms = slimArms;
        c.parts = parts;
        c.model = model;
        c.playerModel = playerModel;
        return c;
    }

    public static MannequinSettings from(Mannequin s) {
        MannequinSettings r = new MannequinSettings();
        Rotations[] poses = {s.getHeadPose(), s.getBodyPose(), s.getLeftArmPose(),
                s.getRightArmPose(), s.getLeftLegPose(), s.getRightLegPose()};
        for (int i = 0; i < 6; i++) {
            r.pose[i * 3] = poses[i].getX();
            r.pose[i * 3 + 1] = poses[i].getY();
            r.pose[i * 3 + 2] = poses[i].getZ();
        }
        r.yaw = Mth.wrapDegrees(s.getYRot());
        r.scale = s.getEntityScale();
        r.tiltX = s.getTiltX();
        r.tiltZ = s.getTiltZ();
        r.showArms = s.isShowArms();
        r.noBasePlate = s.isNoBasePlate();
        r.small = s.isSmall();
        r.crouching = s.isMannequinCrouching();
        r.slimArms = s.slimArms();
        r.parts = s.getPartsMask();
        r.model = (byte) s.getModelType();
        r.playerModel = s.usesPlayerModel();
        return r;
    }

    private static float angle(float v) {
        return Float.isFinite(v) ? Mth.clamp(v, -360.0F, 360.0F) : 0.0F;
    }

    private Rotations rot(int part) {
        return new Rotations(angle(pose[part * 3]), angle(pose[part * 3 + 1]), angle(pose[part * 3 + 2]));
    }

    /** Server side. Values from the network are clamped. */
    public void applyTo(Mannequin s) {
        s.setHeadPose(rot(0));
        s.setBodyPose(rot(1));
        s.setLeftArmPose(rot(2));
        s.setRightArmPose(rot(3));
        s.setLeftLegPose(rot(4));
        s.setRightLegPose(rot(5));
        s.setYRot(angle(yaw));
        s.setYHeadRot(angle(yaw));
        s.setEntityScale(scale);
        s.setTiltX(angle(tiltX));
        s.setTiltZ(angle(tiltZ));
        s.setShowArms(showArms);
        s.setNoBasePlate(noBasePlate);
        s.setMannequinSmall(small);
        s.setMannequinCrouching(crouching);
        s.setSlimArms(slimArms);
        s.setPartsMask(parts);
        s.setModelType(model);
        s.setPlayerModel(playerModel);
    }
}
