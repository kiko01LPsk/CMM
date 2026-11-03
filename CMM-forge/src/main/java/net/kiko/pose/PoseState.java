package net.kiko.pose;

/** The pose forced on each arm of a player. NONE means "vanilla behaviour for that arm". */
public record PoseState(PoseKind right, PoseKind left) {

    public static final PoseState NONE = new PoseState(PoseKind.NONE, PoseKind.NONE);

    public boolean isNone() {
        return right == PoseKind.NONE && left == PoseKind.NONE;
    }
}
