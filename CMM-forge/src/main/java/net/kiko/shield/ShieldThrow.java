package net.kiko.shield;

import net.kiko.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ShieldThrow {

    private ShieldThrow() {}

    /** Throws the shield from the player's main hand; it leaves the inventory (also in creative) until caught. */
    public static void throwFrom(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.CAPTAIN_SHIELD.get()) || player.isSpectator()) return;
        ThrownShield thrown = new ThrownShield(player.level(), player, stack);
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.2F, 1.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.stopUsingItem();
        player.level().addFreshEntity(thrown);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.swing(InteractionHand.MAIN_HAND, true);
    }
}
