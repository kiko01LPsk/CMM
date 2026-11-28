package net.kiko.pose;

/**
 * Side-neutral list of arm poses (no client classes here, so it is safe in network code).
 * The client maps each kind to the vanilla HumanoidModel.ArmPose.
 * Send ordinals only; never reorder existing entries.
 */
public enum PoseKind {
    NONE,
    ITEM,
    BLOCK,
    BOW,
    TRIDENT,
    CROSSBOW_CHARGE,
    CROSSBOW_HOLD,
    SPYGLASS,
    HORN,
    BRUSH,
    PHONE_BROWSE,
    PHONE_CALL,
    PHONE_SELFIE,
    PHONE_SPEAKER;

    public static PoseKind byId(int id) {
        PoseKind[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}
