package net.kiko.chair;

import net.kiko.item.ModItems;
import net.kiko.mannequin.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A flying chair: no damage (like a snowball) and it breaks on whatever it hits; nothing is left to pick up. */
public class ThrownChair extends ThrowableProjectile {

    public ThrownChair(EntityType<? extends ThrownChair> type, Level level) {
        super(type, level);
    }

    public ThrownChair(Level level, LivingEntity owner) {
        super(ModEntities.THROWN_CHAIR.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemParticleOption bits = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.PLASTIC_CHAIR.get()));
            for (int i = 0; i < 12; i++) {
                level().addParticle(bits, getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5D) * 0.2D, random.nextDouble() * 0.2D, (random.nextDouble() - 0.5D) * 0.2D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
