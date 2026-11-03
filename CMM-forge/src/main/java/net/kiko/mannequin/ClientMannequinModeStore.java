package net.kiko.mannequin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client side: the mannequin mode of every player we can see (ourselves included), as told by the server. */
public final class ClientMannequinModeStore {

    private static final Map<UUID, MannequinMode> MODES = new ConcurrentHashMap<>();

    private ClientMannequinModeStore() {}

    public static MannequinMode get(UUID player) {
        return MODES.getOrDefault(player, MannequinMode.EMPTY);
    }

    public static void set(UUID player, MannequinMode mode) {
        if (mode.isActive()) MODES.put(player, mode); else MODES.remove(player);
    }

    public static void clear() {
        MODES.clear();
    }
}
