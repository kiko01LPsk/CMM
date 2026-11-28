package net.kiko.trident;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** "Call" key for the Godspear: every one of your Godspears stuck in the ground nearby flies back to you. */
public final class GodspearCall {

    public static final double RANGE = 128.0D;

    private GodspearCall() {}

    /** @return true if at least one spear answered */
    public static boolean call(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(RANGE);
        List<ThrownKikoTrident> spears = player.serverLevel().getEntitiesOfClass(ThrownKikoTrident.class, box,
                e -> e.isGodspear() && !e.isRecalled() && e.getOwner() == player);
        if (spears.isEmpty()) return false;
        for (ThrownKikoTrident spear : spears) spear.recall();
        player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
