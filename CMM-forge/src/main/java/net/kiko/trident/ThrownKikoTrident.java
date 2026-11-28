package net.kiko.trident;

import net.kiko.mannequin.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** The thrown version of {@link KikoTridentItem}: a copy of vanilla trident behaviour that remembers its own item. */
public class ThrownKikoTrident extends AbstractArrow {

    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownKikoTrident.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownKikoTrident.class, EntityDataSerializers.BOOLEAN);
    /** Godspear only: the owner called it, so it flies back even though it is stuck in the ground. */
    private static final EntityDataAccessor<Boolean> ID_RECALL = SynchedEntityData.defineId(ThrownKikoTrident.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> ID_STACK = SynchedEntityData.defineId(ThrownKikoTrident.class, EntityDataSerializers.ITEM_STACK);

    private ItemStack tridentItem = new ItemStack(Items.TRIDENT);
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public ThrownKikoTrident(EntityType<? extends ThrownKikoTrident> type, Level level) {
        super(type, level);
    }

    public ThrownKikoTrident(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.KIKO_TRIDENT.get(), owner, level);
        this.tridentItem = stack.copy();
        int loyalty = EnchantmentHelper.getLoyalty(stack);
        if (stack.getItem() instanceof GodspearItem) loyalty = Math.max(loyalty, GodspearItem.LOYALTY); // always returns
        this.entityData.set(ID_LOYALTY, (byte) loyalty);
        this.entityData.set(ID_FOIL, stack.hasFoil());
        this.entityData.set(ID_STACK, stack.copy());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
        this.entityData.define(ID_FOIL, false);
        this.entityData.define(ID_RECALL, false);
        this.entityData.define(ID_STACK, new ItemStack(Items.TRIDENT));
    }

    /** The item to draw (synced to clients). */
    public ItemStack getRenderStack() {
        return this.entityData.get(ID_STACK);
    }

    public boolean isGodspear() {
        return this.entityData.get(ID_STACK).getItem() instanceof GodspearItem;
    }

    public boolean isRecalled() {
        return this.entityData.get(ID_RECALL);
    }

    /** The owner calls it: a Godspear stuck in the ground pulls free and flies back. */
    public void recall() {
        this.entityData.set(ID_RECALL, true);
    }

    @Override
    public void tick() {
        if (isGodspear()) {
            // it stays stuck in the ground until its owner calls it (hitting a mob still sends it straight back)
            if (isRecalled()) this.dealtDamage = true;
        } else if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptableReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 toOwner = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015D * (double) loyalty, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }
                double speed = 0.05D * (double) loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(toOwner.normalize().scale(speed)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                ++this.clientSideReturnTridentTickCount;
            }
        }
        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        }
        return false;
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return this.dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = this.tridentItem.getItem() instanceof GodspearItem ? GodspearItem.THROWN_DAMAGE : 8.0F;
        if (target instanceof LivingEntity living) {
            damage += EnchantmentHelper.getDamageBonus(this.tridentItem, living.getMobType());
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;
        SoundEvent sound = SoundEvents.TRIDENT_HIT;
        if (target.hurt(source, damage)) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (target instanceof LivingEntity living) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(living, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, living);
                }
                this.doPostHurtEffects(living);
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float volume = 1.0F;
        if (this.level() instanceof ServerLevel && this.level().isThundering() && this.isChanneling()) {
            BlockPos pos = target.blockPosition();
            if (this.level().canSeeSky(pos)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level());
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(pos));
                    bolt.setCause(owner instanceof ServerPlayer sp ? sp : null);
                    this.level().addFreshEntity(bolt);
                    sound = SoundEvents.TRIDENT_THUNDER;
                    volume = 5.0F;
                }
            }
        }
        this.playSound(sound, volume, 1.0F);
    }

    public boolean isChanneling() {
        return EnchantmentHelper.hasChanneling(this.tridentItem);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Trident", 10)) {
            this.tridentItem = ItemStack.of(tag.getCompound("Trident"));
        }
        this.dealtDamage = tag.getBoolean("DealtDamage");
        int loyalty = EnchantmentHelper.getLoyalty(this.tridentItem);
        if (this.tridentItem.getItem() instanceof GodspearItem) loyalty = Math.max(loyalty, GodspearItem.LOYALTY);
        this.entityData.set(ID_LOYALTY, (byte) loyalty);
        this.entityData.set(ID_RECALL, tag.getBoolean("Recalled"));
        this.entityData.set(ID_FOIL, this.tridentItem.hasFoil());
        this.entityData.set(ID_STACK, this.tridentItem.copy());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Trident", this.tridentItem.save(new CompoundTag()));
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putBoolean("Recalled", isRecalled());
    }

    @Override
    public void tickDespawn() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
