package net.kiko.deer;

import net.kiko.item.ModItems;
import net.kiko.mannequin.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Deer, ported from the Deer Mod by Arisuwu Mei (GPL-3.0).
 * Behaves like a sheep (eats grass, shearing gives antlers that regrow) and a pig
 * (can be saddled and ridden with a deer-crackers-on-a-stick).
 */
public class DeerEntity extends Animal implements Shearable, ItemSteerable, Saddleable {

    public static final TagKey<net.minecraft.world.item.Item> DEER_FOOD =
            TagKey.create(Registries.ITEM, new ResourceLocation("kiko", "deer_food"));

    private static final EntityDataAccessor<Boolean> RED_NOSE = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOOST_TIME = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState eatGrassAnimationState = new AnimationState();
    private EatBlockGoal eatGrassGoal;
    private int eatGrassTimer;
    private final net.minecraft.world.entity.ItemBasedSteering steering =
            new net.minecraft.world.entity.ItemBasedSteering(this.entityData, BOOST_TIME, SADDLED);

    public DeerEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void registerGoals() {
        this.eatGrassGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(ModItems.DEER_CRACKERS_ON_A_STICK.get()), false));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(DEER_FOOD), false));
        this.goalSelector.addGoal(4, this.eatGrassGoal);
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F, 1.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RED_NOSE, false);
        this.entityData.define(SHEARED, false);
        this.entityData.define(SADDLED, false);
        this.entityData.define(BOOST_TIME, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (BOOST_TIME.equals(key) && this.level().isClientSide) {
            this.steering.onSynced();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("RedNose", hasRedNose());
        tag.putBoolean("Sheared", isSheared());
        this.steering.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setRedNose(tag.getBoolean("RedNose"));
        setSheared(tag.getBoolean("Sheared"));
        this.steering.readAdditionalSaveData(tag);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(DEER_FOOD);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return ModEntities.DEER.get().create(level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Red dye gives the deer a red nose, black dye takes it away again.
        if (stack.is(Items.RED_DYE) || stack.is(Items.BLACK_DYE)) {
            if (this.level() instanceof ServerLevel) {
                setRedNose(stack.is(Items.RED_DYE));
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        if (stack.is(Items.SHEARS) && readyForShearing()) {
            if (this.level() instanceof ServerLevel) {
                shear(SoundSource.PLAYERS);
                this.gameEvent(GameEvent.SHEAR, player);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        if (!isFood(stack) && isSaddled() && !isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        InteractionResult result = super.mobInteract(player, hand);
        if (!result.consumesAction() && stack.is(Items.SADDLE)) {
            return stack.interactLivingEntity(player, this, hand);
        }
        return result;
    }

    public boolean hasRedNose() {
        return this.entityData.get(RED_NOSE);
    }

    protected void setRedNose(boolean value) {
        this.entityData.set(RED_NOSE, value);
    }

    // ── Shearing ────────────────────────────────────────────────────────────

    @Override
    public void shear(SoundSource source) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, source, 1.0F, 1.0F);
        this.spawnAtLocation(ModItems.ANTLERS.get(), 1);
        setSheared(true);
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby() && !isSheared();
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean value) {
        this.entityData.set(SHEARED, value);
    }

    // ── Eating grass (antlers regrow) ───────────────────────────────────────

    @Override
    protected void customServerAiStep() {
        this.eatGrassTimer = this.eatGrassGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatGrassTimer = Math.max(0, this.eatGrassTimer - 1);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.eatGrassTimer > 0) {
                this.eatGrassAnimationState.startIfStopped(this.tickCount);
            } else {
                this.eatGrassAnimationState.stop();
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.eatGrassTimer = 40;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void ate() {
        super.ate();
        setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }
    }

    // ── Saddle / riding ─────────────────────────────────────────────────────

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override
    public void equipSaddle(SoundSource source) {
        this.steering.setSaddle(true);
        if (source != null) {
            this.level().playSound(null, this, SoundEvents.PIG_SADDLE, source, 0.5F, 1.0F);
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.steering.tickBoost();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        if (isSaddled() && this.getFirstPassenger() instanceof Player player
                && player.isHolding(ModItems.DEER_CRACKERS_ON_A_STICK.get())) {
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        return new Vec3(0.0D, 0.0D, 1.0D);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.4D * this.steering.boostFactor());
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.55D;
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }
}
