package net.kiko.shield;

import net.kiko.item.ModItems;
import net.kiko.mannequin.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A thrown Captain America's shield. Like a loyal trident / Mjolnir: after it hits something (or a block)
 * it flies back to its owner and lands in their hand. The real stack (damage, name...) travels with it.
 */
public class ThrownShield extends AbstractArrow {

    private static final float DAMAGE = 8.0F;

    private ItemStack shield = new ItemStack(ModItems.CAPTAIN_SHIELD.get());
    private boolean dealtDamage;
    private int returnTicks;

    public ThrownShield(EntityType<? extends ThrownShield> type, Level level) {
        super(type, level);
    }

    public ThrownShield(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_SHIELD.get(), owner, level);
        this.shield = stack.copy();
    }

    @Override
    protected ItemStack getPickupItem() {
        return shield.copy();
    }

    @Override
    public void tick() {
        if (inGroundTime > 4) dealtDamage = true;

        Entity owner = getOwner();
        if (dealtDamage && owner != null) {
            if (!owner.isAlive() || (owner instanceof ServerPlayer && owner.isSpectator())) {
                if (!level().isClientSide()) {
                    spawnAtLocation(getPickupItem(), 0.1F);
                    discard();
                }
                return;
            }
            setNoPhysics(true);
            Vec3 toOwner = owner.getEyePosition().subtract(position());
            setPosRaw(getX(), getY() + toOwner.y * 0.02D, getZ());
            if (level().isClientSide()) yOld = getY();
            Vec3 motion = getDeltaMovement().scale(0.9D).add(toOwner.normalize().scale(0.35D));
            double speed = motion.length();
            if (speed > 2.6D) motion = motion.scale(2.6D / speed);
            setDeltaMovement(motion);
            if (returnTicks == 0) playSound(net.kiko.item.ModSounds.SHIELD_THROW.get(), 1.0F, 1.0F);
            returnTicks++;
        }
        super.tick();
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = getOwner();
        DamageSource source = damageSources().trident(this, owner == null ? this : owner);
        dealtDamage = true;
        target.hurt(source, DAMAGE);
        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.3F);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.SHIELD_BLOCK;
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    /** Only its owner can catch it. */
    @Override
    public void playerTouch(Player player) {
        if (ownedBy(player)) super.playerTouch(player);
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, getPickupItem());
            return true;
        }
        return player.getInventory().add(getPickupItem());
    }

    @Override
    protected void tickDespawn() {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Shield", shield.save(new CompoundTag()));
        tag.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Shield")) shield = ItemStack.of(tag.getCompound("Shield"));
        dealtDamage = tag.getBoolean("DealtDamage");
    }
}
