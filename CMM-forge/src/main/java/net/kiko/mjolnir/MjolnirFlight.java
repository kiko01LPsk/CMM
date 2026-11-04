package net.kiko.mjolnir;

import net.kiko.item.MjolnirItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server side of the Mjolnir flight. The movement itself is done by the flying player's own client
 * (see MjolnirClient); the server only decides when the flight starts and ends,
 * keeps the player from taking fall damage and from being kicked for "flying".
 */
public final class MjolnirFlight {

    private static final Set<UUID> FLYING = new HashSet<>();
    private static final Set<UUID> GRANTED_MAYFLY = new HashSet<>();
    private static final Map<UUID, Long> LAST_FLIGHT = new HashMap<>();

    private MjolnirFlight() {}

    public static boolean isFlying(ServerPlayer player) {
        return FLYING.contains(player.getUUID());
    }

    /** Fall damage is also ignored for 5 seconds after a flight. */
    public static boolean recentlyFlew(ServerPlayer player) {
        if (isFlying(player)) return true;
        Long last = LAST_FLIGHT.get(player.getUUID());
        return last != null && player.level().getGameTime() - last < 100L;
    }

    public static void start(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MjolnirItem) || !MjolnirSaved.isWorthy(player)) return;
        if (!MjolnirItem.isSpinning(stack) || MjolnirItem.isFlying(stack) || player.isSpectator()) return;

        MjolnirItem.setFlying(stack, true);
        FLYING.add(player.getUUID());

        // A dedicated server kicks survival players for "flying", so allow it while the flight lasts.
        if (player.server.isDedicatedServer() && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
            GRANTED_MAYFLY.add(player.getUUID());
        }

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.add(0.0D, 0.6D, 0.0D));
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        player.level().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 0.6F, 1.6F);
    }

    public static void stop(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean wasFlying = FLYING.remove(id);
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof MjolnirItem && MjolnirItem.isFlying(stack)) {
            MjolnirItem.setFlying(stack, false);
        }
        if (wasFlying) {
            LAST_FLIGHT.put(id, player.level().getGameTime());
            player.fallDistance = 0.0F;
        }
        restoreMayfly(player);
    }

    /** Called when the player leaves, so nothing stays behind. */
    public static void forget(ServerPlayer player) {
        FLYING.remove(player.getUUID());
        LAST_FLIGHT.remove(player.getUUID());
        restoreMayfly(player);
    }

    private static void restoreMayfly(ServerPlayer player) {
        if (GRANTED_MAYFLY.remove(player.getUUID())) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}
