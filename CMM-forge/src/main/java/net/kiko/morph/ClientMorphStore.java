package net.kiko.morph;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Client side: which mob each player is morphed into (registry id), if any. */
public final class ClientMorphStore {
    private static final Map<UUID, String> MORPHS = new HashMap<>();

    private ClientMorphStore() {}

    public static void set(UUID player, String id) {
        if (id.isEmpty()) MORPHS.remove(player); else MORPHS.put(player, id);
    }

    public static String get(UUID player) {
        return MORPHS.getOrDefault(player, "");
    }

    public static Map<UUID, String> all() {
        return MORPHS;
    }

    public static void clear() {
        MORPHS.clear();
    }
}
