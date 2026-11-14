package net.kiko.wheelchair;

import net.kiko.item.ModItems;
import net.kiko.client.wheelchair.WheelchairClient;
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

/**
 * A wheelchair you can sit in and drive: W/S roll forward/back, A/D turn on the spot, sneak to get out.
 * The big wheels and the small front wheels turn according to how far each side has rolled.
 */
public class WheelchairEntity extends Entity {

    private static final double MAX_FORWARD = 0.24D;
    private static final double MAX_BACKWARD = 0.09D;
    private static final double ACCEL = 0.035D;
    private static final double BRAKE = 0.06D;
    private static final float TURN = 4.0F;

    public static final float BIG_RADIUS = 4.5F / 16.0F;
    public static final float SMALL_RADIUS = 0.2F;
    private static final float HALF_TRACK = 7.0F / 16.0F;

    // smooth movement for everyone who is not driving
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYRot, lerpXRot;

    // wheel angles (radians), current and previous tick, for smooth rendering
    public float wheelL, wheelLO, wheelR, wheelRO, casterL, casterLO, casterR, casterRO;

    public WheelchairEntity(EntityType<? extends WheelchairEntity> type, Level level) {
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
        if (source.getEntity() instanceof Player player && !hasPassenger(player)) {
            if (!player.getAbilities().instabuild) {
                level().addFreshEntity(new ItemEntity(level(), getX(), getY() + 0.3D, getZ(),
                        new ItemStack(ModItems.WHEELCHAIR.get())));
            }
            level().playSound(null, blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 1.2F);
            ejectPassengers();
            discard();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.WHEELCHAIR.get());
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
        if (level().isClientSide) animateWheels();
    }

    private void drive(LivingEntity driver) {
        float forward = 0.0F;
        float left = 0.0F;
        if (driver != null && level().isClientSide) {
            forward = WheelchairClient.forward(driver);
            left = WheelchairClient.left(driver);
        }
        setYRot(getYRot() - left * TURN);
        setXRot(0.0F);

        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        Vec3 look = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 motion = getDeltaMovement();
        double speed = motion.x * look.x + motion.z * look.z;

        double target = forward > 0.0F ? MAX_FORWARD : forward < 0.0F ? -MAX_BACKWARD : 0.0D;
        double rate = (Math.abs(target) > Math.abs(speed) && target * speed >= 0.0D) ? ACCEL : BRAKE;
        speed += Mth.clamp(target - speed, -rate, rate);
        if (Math.abs(speed) < 0.003D && target == 0.0D) speed = 0.0D;

        double dy = isInWater() ? -0.02D : motion.y - 0.08D;
        Vec3 horizontal = new Vec3(look.x * speed, 0.0D, look.z * speed);

        // climb up to 0.6 blocks (slabs, stairs, carpets)
        if (onGround() && Math.abs(speed) > 0.01D) {
            AABB box = getBoundingBox();
            if (!level().noCollision(this, box.move(horizontal.x, 0.0D, horizontal.z))
                    && level().noCollision(this, box.move(horizontal.x, 0.6D, horizontal.z))
                    && level().noCollision(this, box.move(0.0D, 0.6D, 0.0D))) {
                move(MoverType.SELF, new Vec3(0.0D, 0.6D, 0.0D));
            }
        }
        move(MoverType.SELF, new Vec3(horizontal.x, dy, horizontal.z));
        if (horizontalCollision) speed = 0.0D;
        setDeltaMovement(look.x * speed, onGround() ? 0.0D : dy, look.z * speed);
    }

    private void animateWheels() {
        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        double lookX = -Mth.sin(yawRad);
        double lookZ = Mth.cos(yawRad);
        float dist = (float) ((getX() - xo) * lookX + (getZ() - zo) * lookZ);
        float turn = Mth.wrapDegrees(getYRot() - yRotO) * Mth.DEG_TO_RAD;
        float distL = dist + turn * HALF_TRACK;
        float distR = dist - turn * HALF_TRACK;
        wheelLO = wheelL;
        wheelRO = wheelR;
        casterLO = casterL;
        casterRO = casterR;
        wheelL -= distL / BIG_RADIUS;
        wheelR -= distR / BIG_RADIUS;
        casterL -= distL / SMALL_RADIUS;
        casterR -= distR / SMALL_RADIUS;
    }

    // ── rider ─────────────────────────────────────────────────────────────

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!hasPassenger(passenger)) return;
        move.accept(passenger, getX(), getY() - 0.25D, getZ());
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
