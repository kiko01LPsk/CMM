package net.kiko.deer;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Like the carrot on a stick, but for deer. The entity type is a supplier because items register before entities. */
public class DeerCrackersOnAStickItem extends Item {

    private final Supplier<? extends EntityType<?>> steerable;
    private final int damage;

    public DeerCrackersOnAStickItem(Item.Properties properties, Supplier<? extends EntityType<?>> steerable, int damage) {
        super(properties);
        this.steerable = steerable;
        this.damage = damage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        Entity vehicle = player.getVehicle();
        if (player.isPassenger() && vehicle instanceof ItemSteerable steer
                && vehicle.getType() == this.steerable.get() && steer.boost()) {
            stack.hurtAndBreak(this.damage, player, p -> p.broadcastBreakEvent(hand));
            if (stack.isEmpty()) {
                ItemStack rod = new ItemStack(Items.FISHING_ROD);
                rod.setTag(stack.getTag());
                return InteractionResultHolder.success(rod);
            }
            return InteractionResultHolder.success(stack);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.pass(stack);
    }
}
