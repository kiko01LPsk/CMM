package net.kiko.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Squeaks when you hit something with it, and when you right-click. */
public class RubberChickenItem extends Item {

    public RubberChickenItem(Properties properties) {
        super(properties);
    }

    private static void squeak(Level level, double x, double y, double z) {
        // Server side only: the server sends the sound to every nearby player (including the user).
        float pitch = 0.9F + level.random.nextFloat() * 0.35F;
        level.playSound(null, x, y, z, ModSounds.RUBBER_CHICKEN_SQUEAK.get(), SoundSource.PLAYERS, 1.0F, pitch);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        if (!level.isClientSide) {
            squeak(level, target.getX(), target.getY(), target.getZ());
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            squeak(level, player.getX(), player.getY(), player.getZ());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
