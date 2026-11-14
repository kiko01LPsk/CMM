package net.kiko.tripod;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * A studio camera on a tripod. It is an entity (like an armor stand), so it can point in any direction:
 * right click turns it 15 degrees, sneak + right click turns it back, punch it to pick it up.
 */
public class TripodCameraEntity extends Entity {

    public TripodCameraEntity(EntityType<? extends TripodCameraEntity> type, Level level) {
        super(type, level);
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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        float step = player.isShiftKeyDown() ? -15.0F : 15.0F;
        setYRot(Mth.wrapDegrees(getYRot() + step));
        setRot(getYRot(), 0.0F);
        this.yRotO = getYRot();
        this.hasImpulse = true;
        level().playSound(null, blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.4F, 1.6F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.getEntity() instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                level().addFreshEntity(new ItemEntity(level(), getX(), getY() + 0.3D, getZ(),
                        new ItemStack(ModItems.TRIPOD_CAMERA.get())));
            }
            level().playSound(null, blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 1.3F);
            discard();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.TRIPOD_CAMERA.get());
    }
}
