package net.kiko.chair;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** A plastic chair that only kiko01LPsk can throw: hold right click like a trident and let go. */
public class PlasticChairItem extends Item {

    public static final String THROWER = "kiko01LPsk";

    public PlasticChairItem(Properties properties) {
        super(properties);
    }

    public static boolean canThrow(Player player) {
        return THROWER.equalsIgnoreCase(player.getGameProfile().getName());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR; // the same arm movement as a trident
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canThrow(player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Only " + THROWER + " can throw this chair."), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || !canThrow(player)) return;
        if (getUseDuration(stack) - timeLeft < 10) return; // not pulled back far enough
        if (!level.isClientSide) {
            ThrownChair chair = new ThrownChair(level, player);
            chair.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.9F, 1.0F);
            level.addFreshEntity(chair);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
    }
}
