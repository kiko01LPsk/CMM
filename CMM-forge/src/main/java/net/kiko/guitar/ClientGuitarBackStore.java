package net.kiko.guitar;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Client side: which players currently carry their guitar on the back. */
public final class ClientGuitarBackStore {
    private static final Set<UUID> ON_BACK = new HashSet<>();

    private ClientGuitarBackStore() {}

    public static void set(UUID player, boolean onBack) {
        if (onBack) ON_BACK.add(player); else ON_BACK.remove(player);
    }

    public static boolean isOnBack(UUID player) {
        return ON_BACK.contains(player);
    }
}
