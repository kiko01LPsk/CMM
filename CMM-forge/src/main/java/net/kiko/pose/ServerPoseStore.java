package net.kiko.pose;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server side: the pose each online player currently has. */
public final class ServerPoseStore {

    private static final Map<UUID, PoseState> STATES = new ConcurrentHashMap<>();

    private ServerPoseStore() {}

    public static PoseState get(UUID player) {
        return STATES.getOrDefault(player, PoseState.NONE);
    }

    public static void set(UUID player, PoseState state) {
        if (state.isNone()) {
            STATES.remove(player);
        } else {
            STATES.put(player, state);
        }
    }

    public static void remove(UUID player) {
        STATES.remove(player);
    }
}
