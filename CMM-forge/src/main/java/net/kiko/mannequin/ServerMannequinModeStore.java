package net.kiko.mannequin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server side: which online players are currently imitating a mannequin. */
public final class ServerMannequinModeStore {

    private static final Map<UUID, MannequinMode> MODES = new ConcurrentHashMap<>();

    private ServerMannequinModeStore() {}

    public static MannequinMode get(UUID player) {
        return MODES.getOrDefault(player, MannequinMode.EMPTY);
    }

    public static void set(UUID player, MannequinMode mode) {
        if (mode.isActive()) MODES.put(player, mode); else MODES.remove(player);
    }

    public static void remove(UUID player) {
        MODES.remove(player);
    }
}
