package net.kiko.mannequin;

/** Ready-made poses. Angles in degrees: head, body, left arm, right arm, left leg, right leg (x, y, z each). */
public final class MannequinPresets {

    public record Preset(String id, float[] pose, boolean crouching) {}

    private static Preset p(String id, boolean crouching, float... pose) {
        return new Preset(id, pose, crouching);
    }

    public static final Preset[] ALL = {
            p("standing", false, 0, 0, 0, 0, 0, 0, -10, 0, -10, -15, 0, 10, -1, 0, -1, 1, 0, 1),
            p("attention", false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            p("t_pose", false, 0, 0, 0, 0, 0, 0, 0, 0, -90, 0, 0, 90, 0, 0, 0, 0, 0, 0),
            p("arms_up", false, -10, 0, 0, 0, 0, 0, 0, 0, -170, 0, 0, 170, 0, 0, 0, 0, 0, 0),
            p("victory", false, -15, 0, 0, 0, 0, 0, 0, 0, -140, 0, 0, 140, 0, 0, -8, 0, 0, 8),
            p("wave", false, 0, -10, 5, 0, 0, 0, -10, 0, -10, 0, 0, 150, -1, 0, -1, 1, 0, 1),
            p("salute", false, -5, 0, 0, 0, 0, 0, -10, 0, -10, -135, 0, -10, -1, 0, -1, 1, 0, 1),
            p("pointing", false, 0, -15, 0, 0, -10, 0, -10, 0, -10, -90, 0, 0, -1, 0, -1, 1, 0, 1),
            p("hands_on_hips", false, 0, 0, 0, 0, 0, 0, 8, 0, -38, 8, 0, 38, 0, 0, -6, 0, 0, 6),
            p("thinker", false, 20, 0, 0, 5, 0, 0, -10, 0, -10, -80, -40, 0, -1, 0, -1, 1, 0, 1),
            p("guard", false, 5, 0, 0, 0, 0, 0, -70, 25, 0, -70, -25, 0, -15, 0, -5, 15, 0, 5),
            p("walking", false, 0, 0, 0, 0, 0, 0, 25, 0, 0, -25, 0, 0, -25, 0, 0, 25, 0, 0),
            p("running", false, -10, 0, 0, 12, 0, 0, 60, 0, -8, -60, 0, 8, -55, 0, 0, 55, 0, 0),
            p("dancing", false, 0, 0, 10, 0, 0, -5, 0, 0, -150, 0, 0, 60, -20, 0, -5, 10, 0, 5),
            p("sitting", false, 0, 0, 0, 0, 0, 0, -35, 0, -10, -35, 0, 10, -90, 0, -5, -90, 0, 5),
            p("zombie", false, 0, 0, 0, 0, 0, 0, -90, 0, 0, -90, 0, 0, -3, 0, 0, 3, 0, 0),
            p("crouch", true, 10, 0, 0, 0, 0, 0, -10, 0, -10, -15, 0, 10, 0, 0, 0, 0, 0, 0),
    };

    private MannequinPresets() {}
}
