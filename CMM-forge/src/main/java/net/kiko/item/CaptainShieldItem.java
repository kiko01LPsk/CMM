package net.kiko.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/** A vanilla-style shield that can also be worn on the back (put it in the chestplate slot). */
public class CaptainShieldItem extends ShieldItem {

    public CaptainShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
        return slot == EquipmentSlot.CHEST;
    }
}
