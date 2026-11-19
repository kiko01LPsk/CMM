package net.kiko.item;

import net.kiko.morph.MorphEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Right-click a mob to turn into it. The "Reset morph" key (Controls) changes you back. */
public class MorphWandItem extends Item {

    public MorphWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Player) return InteractionResult.PASS;
        if (player instanceof ServerPlayer sp) {
            MorphEvents.set(sp, target.getType());
            sp.displayClientMessage(Component.translatable("message.kiko.morphed", target.getType().getDescription()), true);
            sp.level().playSound(null, sp.blockPosition(), net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
