package net.kiko.cardboard;

import net.kiko.item.ModItems;
import net.kiko.client.cardboard.CardboardBoxClient;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** A cardboard box you can sit in and drive like a car: W/S accelerate and reverse, A/D steer, sneak to get out. */
public class CardboardBoxEntity extends Entity {

    private static final double MAX_FORWARD = 0.58D;
    private static final double MAX_BACKWARD = 0.14D;
    private static final double ACCEL = 0.025D;
    private static final double BRAKE = 0.03D;
    private static final float TURN = 5.0F;


    // smooth movement for everyone who is not driving
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYRot, lerpXRot;


    public CardboardBoxEntity(EntityType<? extends CardboardBoxEntity> type, Level level) {
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
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || isVehicle()) return InteractionResult.PASS;
        if (!level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.getEntity() instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                level().addFreshEntity(new ItemEntity(level(), getX(), getY() + 0.3D, getZ(),
                        new ItemStack(ModItems.CARDBOARD_BOX.get())));
            }
            level().playSound(null, blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.6F, 1.2F);
            ejectPassengers();
            discard();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.CARDBOARD_BOX.get());
    }

    // ── movement ──────────────────────────────────────────────────────────

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = Math.max(steps, 2);
    }

    @Override
    public void tick() {
        super.tick();
        this.fallDistance = 0.0F;
        LivingEntity driver = getControllingPassenger();
        if (driver != null) driver.fallDistance = 0.0F;

        boolean local = driver instanceof Player p ? p.isLocalPlayer() : !level().isClientSide;
        if (local) {
            this.lerpSteps = 0;
            drive(driver);
        } else {
            if (lerpSteps > 0) {
                double nx = getX() + (lerpX - getX()) / lerpSteps;
                double ny = getY() + (lerpY - getY()) / lerpSteps;
                double nz = getZ() + (lerpZ - getZ()) / lerpSteps;
                float ny2 = getYRot() + (float) Mth.wrapDegrees(lerpYRot - getYRot()) / lerpSteps;
                float nx2 = getXRot() + (float) (lerpXRot - getXRot()) / lerpSteps;
                lerpSteps--;
                setPos(nx, ny, nz);
                setRot(ny2, nx2);
            }
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private void drive(LivingEntity driver) {
        float forward = 0.0F;
        float left = 0.0F;
        if (driver != null && level().isClientSide) {
            forward = CardboardBoxClient.forward(driver);
            left = CardboardBoxClient.left(driver);
        }
        setXRot(0.0F);

        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        Vec3 look = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 motion = getDeltaMovement();
        double speed = motion.x * look.x + motion.z * look.z;
        // steer like a car: you only turn while rolling, and reversing flips the steering
        float grip = (float) Mth.clamp(Math.abs(speed) / 0.12D, 0.0D, 1.0D);
        setYRot(getYRot() - left * TURN * grip * (speed < 0.0D ? -1.0F : 1.0F));
        yawRad = getYRot() * Mth.DEG_TO_RAD;
        look = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        double target = forward > 0.0F ? MAX_FORWARD : forward < 0.0F ? -MAX_BACKWARD : 0.0D;
        double rate = (Math.abs(target) > Math.abs(speed) && target * speed >= 0.0D) ? ACCEL : BRAKE;
        speed += Mth.clamp(target - speed, -rate, rate);
        if (Math.abs(speed) < 0.003D && target == 0.0D) speed = 0.0D;

        double dy = isInWater() ? -0.02D : motion.y - 0.08D;
        Vec3 horizontal = new Vec3(look.x * speed, 0.0D, look.z * speed);

        // climb up to one full block: lift first, then roll forward without falling back down in the same move
        boolean climbed = false;
        if (onGround() && Math.abs(speed) > 0.01D) {
            AABB box = getBoundingBox();
            if (!level().noCollision(this, box.move(horizontal.x, 0.0D, horizontal.z))
                    && level().noCollision(this, box.move(horizontal.x, 1.02D, horizontal.z))
                    && level().noCollision(this, box.move(0.0D, 1.02D, 0.0D))) {
                move(MoverType.SELF, new Vec3(0.0D, 1.02D, 0.0D));
                climbed = true;
            }
        }
        move(MoverType.SELF, new Vec3(horizontal.x, climbed ? 0.0D : dy, horizontal.z));
        if (horizontalCollision) speed = 0.0D;
        setDeltaMovement(look.x * speed, climbed ? 0.0D : (onGround() ? 0.0D : dy), look.z * speed);
    }

    // ── rider ─────────────────────────────────────────────────────────────

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!hasPassenger(passenger)) return;
        // sit a little towards the back of the box
        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        move.accept(passenger, getX() + Mth.sin(yawRad) * 0.14D, getY() - 0.58D, getZ() - Mth.cos(yawRad) * 0.14D);
        float turn = Mth.wrapDegrees(getYRot() - yRotO);
        passenger.setYRot(passenger.getYRot() + turn);
        passenger.setYHeadRot(passenger.getYHeadRot() + turn);
        passenger.setYBodyRot(getYRot());
        float diff = Mth.wrapDegrees(passenger.getYRot() - getYRot());
        float clamped = Mth.clamp(diff, -105.0F, 105.0F);
        passenger.yRotO += clamped - diff;
        passenger.setYRot(passenger.getYRot() + clamped - diff);
        passenger.setYHeadRot(passenger.getYRot());
    }
}
