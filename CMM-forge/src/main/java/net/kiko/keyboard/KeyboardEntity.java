package net.kiko.keyboard;

import net.kiko.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * A keyboard that is an entity (like an armor stand), so it can point in any direction:
 * right click turns it 15 degrees, sneak + right click tilts it 15 degrees, punch it to pick it up.
 */
public class KeyboardEntity extends Entity {

    public KeyboardEntity(EntityType<? extends KeyboardEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** Flat when lying down, taller when it has been tilted up. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float tilt = Math.abs(Mth.sin(getXRot() * Mth.DEG_TO_RAD));
        return tilt > 0.5F ? EntityDimensions.scalable(0.7F, 0.7F) : EntityDimensions.scalable(0.8F, 0.25F);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            setXRot(Mth.wrapDegrees(getXRot() + 15.0F));
            refreshDimensions();
        } else {
            setYRot(Mth.wrapDegrees(getYRot() + 15.0F));
        }
        setRot(getYRot(), getXRot());
        this.yRotO = getYRot();
        this.xRotO = getXRot();
        this.hasImpulse = true;
        level().playSound(null, blockPosition(), SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.5F, 1.4F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.getEntity() instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                level().addFreshEntity(new ItemEntity(level(), getX(), getY() + 0.2D, getZ(),
                        new ItemStack(ModItems.KEYBOARD.get())));
            }
            level().playSound(null, blockPosition(), SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.7F, 1.0F);
            discard();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.KEYBOARD.get());
    }
}
