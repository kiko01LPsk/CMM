package net.kiko.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Elytra-style wings that never break: wear them in the chest slot, jump, then jump again in the air to glide. */
public class AngelWingsItem extends ElytraItem {
    private static final double MAX_SPEED = 1.6D;

    public AngelWingsItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        // Same push a firework rocket gives, applied every tick, so no rockets are needed. Look up to climb, down to dive.
        Vec3 look = entity.getLookAngle();
        Vec3 motion = entity.getDeltaMovement();
        if (motion.length() < MAX_SPEED) {
            entity.setDeltaMovement(motion.add(
                    look.x * 0.1D + (look.x * 1.5D - motion.x) * 0.1D,
                    look.y * 0.1D + (look.y * 1.5D - motion.y) * 0.1D,
                    look.z * 0.1D + (look.z * 1.5D - motion.z) * 0.1D));
            entity.hurtMarked = true;
        }
        return true;
    }
}
