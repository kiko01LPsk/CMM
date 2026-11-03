package net.kiko.pose;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client side: the poses of OTHER players, as told to us by the server. */
public final class ClientPoseStore {

    private static final Map<UUID, PoseState> STATES = new ConcurrentHashMap<>();

    private ClientPoseStore() {}

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

    public static void clear() {
        STATES.clear();
    }
}
