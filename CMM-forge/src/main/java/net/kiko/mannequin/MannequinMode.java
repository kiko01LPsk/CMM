package net.kiko.mannequin;

import net.minecraft.core.Rotations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/** "Imitate mannequin": the pose, arm width, crouch and skin a player shows while they act as a mannequin. */
public class MannequinMode {

    public static final MannequinMode EMPTY = new MannequinMode(false, new float[18], false, false, "", false);

    private final boolean active;
    private final float[] pose;
    private final boolean slimArms;
    private final boolean crouching;
    private final String skin;
    private final boolean playerModel;

    public MannequinMode(boolean active, float[] pose, boolean slimArms, boolean crouching, String skin, boolean playerModel) {
        this.active = active;
        this.pose = pose;
        this.slimArms = slimArms;
        this.crouching = crouching;
        this.skin = skin;
        this.playerModel = playerModel;
    }

    public static MannequinMode of(MannequinSettings settings, String skin) {
        return new MannequinMode(true, settings.pose.clone(), settings.slimArms, settings.crouching, skin, settings.playerModel);
    }

    public MannequinMode(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
        this.pose = new float[18];
        for (int i = 0; i < pose.length; i++) pose[i] = buf.readFloat();
        this.slimArms = buf.readBoolean();
        this.crouching = buf.readBoolean();
        this.skin = buf.readUtf(32767);
        this.playerModel = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        for (float f : pose) buf.writeFloat(f);
        buf.writeBoolean(slimArms);
        buf.writeBoolean(crouching);
        buf.writeUtf(skin, 32767);
        buf.writeBoolean(playerModel);
    }

    /** What the server accepts from a client: finite angles and a valid skin. */
    public MannequinMode sanitized() {
        if (!active) return EMPTY;
        float[] clean = new float[18];
        for (int i = 0; i < 18; i++) {
            clean[i] = Float.isFinite(pose[i]) ? Mth.clamp(pose[i], -360.0F, 360.0F) : 0.0F;
        }
        return new MannequinMode(true, clean, slimArms, crouching, MannequinSkinData.isValid(skin) ? skin : "", playerModel);
    }

    public boolean isActive() { return active; }
    public boolean slimArms() { return slimArms; }
    public boolean crouching() { return crouching; }
    public String skin() { return skin; }
    public boolean playerModel() { return playerModel; }

    /** part: 0 head, 1 body, 2 left arm, 3 right arm, 4 left leg, 5 right leg. */
    public Rotations rotation(int part) {
        return new Rotations(pose[part * 3], pose[part * 3 + 1], pose[part * 3 + 2]);
    }
}
