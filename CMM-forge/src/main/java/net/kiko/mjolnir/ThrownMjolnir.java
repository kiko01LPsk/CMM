package net.kiko.mjolnir;

import net.kiko.item.ModItems;
import net.kiko.mannequin.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A thrown Mjolnir. Works like a loyal trident: after it hits something (or sticks in a block)
 * it flies back to its owner. A called hammer is the same entity, already on its way back.
 */
public class ThrownMjolnir extends AbstractArrow {

    private static final EntityDataAccessor<Boolean> ID_RETURNING =
            SynchedEntityData.defineId(ThrownMjolnir.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> ID_CALLED =
            SynchedEntityData.defineId(ThrownMjolnir.class, EntityDataSerializers.BOOLEAN);

    private static final float DAMAGE = 12.0F;

    private boolean dealtDamage;
    private boolean fromCreative;
    public int returnTicks;

    public ThrownMjolnir(EntityType<? extends ThrownMjolnir> type, Level level) {
        super(type, level);
    }

    public ThrownMjolnir(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_MJOLNIR.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ID_RETURNING, false);
        entityData.define(ID_CALLED, false);
    }

    public void setFromCreative(boolean value) {
        this.fromCreative = value;
    }

    /** Make it fly straight back to the owner (used by the call key). */
    public void startReturn() {
        dealtDamage = true;
        setNoPhysics(true);
        entityData.set(ID_RETURNING, true);
        entityData.set(ID_CALLED, true);
    }

    /** True for a hammer that was called with the key (not one that is just returning from a throw). */
    public boolean isCalled() {
        return entityData.get(ID_CALLED);
    }

    public boolean isReturning() {
        return entityData.get(ID_RETURNING);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.MJOLNIR.get());
    }

    // ── flight ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        if (inGroundTime > 4) dealtDamage = true;

        Entity owner = getOwner();
        if ((dealtDamage || isNoPhysics()) && owner != null) {
            if (!isAcceptableOwner(owner)) {
                if (!level().isClientSide()) {
                    spawnAtLocation(getPickupItem(), 0.1F);
                    discard();
                }
                return;
            }
            setNoPhysics(true);
            if (!level().isClientSide() && !isReturning()) entityData.set(ID_RETURNING, true);

            Vec3 toOwner = owner.getEyePosition().subtract(position());
            setPosRaw(getX(), getY() + toOwner.y * 0.02D, getZ());
            if (level().isClientSide()) yOld = getY();
            Vec3 motion = getDeltaMovement().scale(0.9D).add(toOwner.normalize().scale(0.4D));
            double speed = motion.length();
            if (speed > 3.0D) motion = motion.scale(3.0D / speed);
            setDeltaMovement(motion);
            if (returnTicks == 0) playSound(SoundEvents.TRIDENT_RETURN, 8.0F, 1.0F);
            returnTicks++;
        }
        super.tick();
    }

    private boolean isAcceptableOwner(Entity owner) {
        if (!owner.isAlive()) return false;
        return !(owner instanceof ServerPlayer) || !owner.isSpectator();
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

        if (target.hurt(source, DAMAGE)) {
            if (target.getType() == EntityType.ENDERMAN) return;
            if (target instanceof LivingEntity living) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(living, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, living);
                }
                doPostHurtEffects(living);
            }
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);

    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    /** Only its owner (who must still be worthy) can catch it. */
    @Override
    public void playerTouch(Player player) {
        if (ownedBy(player) && MjolnirSaved.isWorthy(player)) {
            super.playerTouch(player);
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        level().playSound(null, player.getX(), player.getY(), player.getZ(), net.kiko.item.ModSounds.MJOLNIR_SWING.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.0F);
        ItemStack stack = getPickupItem();
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
            return true;
        }
        return player.getInventory().add(stack);
    }

    /** A lying hammer never despawns by itself. */
    @Override
    protected void tickDespawn() {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", dealtDamage);
        tag.putBoolean("FromCreative", fromCreative);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dealtDamage = tag.getBoolean("DealtDamage");
        fromCreative = tag.getBoolean("FromCreative");
    }
}
