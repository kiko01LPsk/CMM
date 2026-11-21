package net.kiko.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A mold used to print banknotes. It stays in the crafting grid and loses durability each time. */
public class MoneyMoldItem extends Item {
    public MoneyMoldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (copy.hurt(1, net.minecraft.util.RandomSource.create(), (ServerPlayer) null)) {
            return ItemStack.EMPTY;
        }
        return copy;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
