package net.kiko.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kiko.network.ModNetwork;
import net.kiko.network.PoseC2SPacket;
import net.kiko.pose.PoseKind;
import net.kiko.pose.PoseState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.HumanoidModel.ArmPose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side pose keybinds. Every key is unbound by default, so players choose their own.
 * Pressing a pose key toggles that pose on; pressing it again turns it off.
 * Pressing a different pose key switches to that pose.
 * Every change is sent to the server so other players see the pose too.
 */
public final class PoseBindings {

    public static final String CATEGORY = "key.categories.kiko";

    /** One keybind: which pose each arm gets (NONE = leave that arm alone). */
    public record Binding(KeyMapping key, PoseKind right, PoseKind left) {}

    private static final List<Binding> BINDINGS = new ArrayList<>();
    private static Binding active = null;

    static {
        // id, pose, offer a "both hands" key as well
        addType("trident",         PoseKind.TRIDENT,         true);
        addType("horn",            PoseKind.HORN,            true);
        addType("spyglass",        PoseKind.SPYGLASS,        true);
        addType("block",           PoseKind.BLOCK,           true);
        addType("brush",           PoseKind.BRUSH,           true);
        addType("item",            PoseKind.ITEM,            true);
        addType("bow",             PoseKind.BOW,             false);
        addType("crossbow",        PoseKind.CROSSBOW_HOLD,   false);
        addType("crossbow_charge", PoseKind.CROSSBOW_CHARGE, false);
    }

    private PoseBindings() {}

    private static void addType(String id, PoseKind pose, boolean allowBoth) {
        add(id + "_right", pose, PoseKind.NONE);
        add(id + "_left", PoseKind.NONE, pose);
        if (allowBoth) {
            add(id + "_both", pose, pose);
        }
    }

    private static void add(String suffix, PoseKind right, PoseKind left) {
        KeyMapping key = new KeyMapping("key.kiko.pose." + suffix,
                InputConstants.UNKNOWN.getValue(), CATEGORY);
        BINDINGS.add(new Binding(key, right, left));
    }

    public static List<Binding> all() {
        return Collections.unmodifiableList(BINDINGS);
    }

    /** Toggle a pose and tell the server. Only call while connected to a world. */
    public static void toggle(Binding binding) {
        active = (active == binding) ? null : binding;
        ModNetwork.CHANNEL.sendToServer(new PoseC2SPacket(localState()));
    }

    /** Forget the local pose (used when leaving a world). Does not send anything. */
    public static void clear() {
        active = null;
        aimPose = PoseState.NONE;
    }

    /** Set by the sniper rifle while aiming: the main arm is raised in the bow pose (unless a pose key is active). */
    private static PoseState aimPose = PoseState.NONE;

    public static void setAimPose(PoseState pose) {
        if (aimPose.equals(pose)) return;
        aimPose = pose;
        ModNetwork.CHANNEL.sendToServer(new PoseC2SPacket(localState()));
    }

    public static PoseState localState() {
        if (active != null) return new PoseState(active.right(), active.left());
        return aimPose;
    }

    public static ArmPose toArmPose(PoseKind kind) {
        return switch (kind) {
            case ITEM -> ArmPose.ITEM;
            case BLOCK -> ArmPose.BLOCK;
            case BOW -> ArmPose.BOW_AND_ARROW;
            case TRIDENT -> ArmPose.THROW_SPEAR;
            case CROSSBOW_CHARGE -> ArmPose.CROSSBOW_CHARGE;
            case CROSSBOW_HOLD -> ArmPose.CROSSBOW_HOLD;
            case SPYGLASS -> ArmPose.SPYGLASS;
            case HORN -> ArmPose.TOOT_HORN;
            case BRUSH -> ArmPose.BRUSH;
            case NONE -> ArmPose.EMPTY;
        };
    }
}
