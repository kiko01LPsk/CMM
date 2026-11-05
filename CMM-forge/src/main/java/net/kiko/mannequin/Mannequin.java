package net.kiko.mannequin;

import net.kiko.item.ModItems;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * A mannequin made on top of the armor stand: it keeps all the armor stand behaviour (poses of every
 * limb, armor and items, arms, small size ...) and adds slim arms, crouching, model parts, a scale and a tilt.
 * Sneak + right-click it to open the editor.
 */
public class Mannequin extends ArmorStand {

    public static final byte ALL_PARTS = 0x7F;
    public static final float MIN_SCALE = 0.25F;

    /** What the mannequin looks like: the player model, or a mob model that takes a custom texture. */
    public static final int MODEL_PLAYER = 0;
    public static final int MODEL_GOLEM = 1;
    public static final int MODEL_VILLAGER = 2;
    public static final int MODEL_CREEPER = 3;
    public static final int MODEL_ZOMBIE = 4;
    public static final int MODEL_HUSK = 5;
    public static final int MODEL_DROWNED = 6;
    public static final int MODEL_SKELETON = 7;
    public static final int MODEL_WITHER_SKELETON = 8;
    public static final int MODEL_PILLAGER = 9;
    public static final int MODEL_VINDICATOR = 10;
    public static final int MODEL_ENDERMAN = 11;
    public static final int MODEL_PIGLIN = 12;
    public static final int MODEL_WITCH = 13;
    public static final int MODEL_PIG = 14;
    public static final int MODEL_COW = 15;
    /** A model the player uploaded (a Blockbench "Bedrock Entity" .geo.json file). */
    public static final int MODEL_CUSTOM = 16;
    public static final int MODEL_COUNT = 17;
    public static final float MAX_SCALE = 4.0F;

    private static final EntityDataAccessor<Boolean> DATA_SLIM =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CROUCHING =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_PARTS =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TILT_X =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TILT_Z =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> DATA_SMALL =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_SKIN =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_PLAYER_MODEL =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<String> DATA_CUSTOM_MODEL =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Byte> DATA_MODEL =
            SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BYTE);

    /** Client only: the texture the imitation dummy uses when the player kept the basic skin. */
    public net.minecraft.resources.ResourceLocation overrideTexture;

    private long lastHitTick;

    public Mannequin(EntityType<? extends Mannequin> type, Level level) {
        super(type, level);
        // A standing mannequin: arms visible, no base plate, everything hanging straight.
        setShowArms(true);
        setNoBasePlate(true);
        Rotations zero = new Rotations(0.0F, 0.0F, 0.0F);
        setHeadPose(zero);
        setBodyPose(zero);
        setLeftArmPose(zero);
        setRightArmPose(zero);
        setLeftLegPose(zero);
        setRightLegPose(zero);
    }

    public Mannequin(Level level, double x, double y, double z) {
        this(ModEntities.MANNEQUIN.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_SLIM, false);
        entityData.define(DATA_CROUCHING, false);
        entityData.define(DATA_PARTS, ALL_PARTS);
        entityData.define(DATA_SCALE, 1.0F);
        entityData.define(DATA_TILT_X, 0.0F);
        entityData.define(DATA_TILT_Z, 0.0F);
        entityData.define(DATA_SKIN, "");
        entityData.define(DATA_SMALL, false);
        entityData.define(DATA_MODEL, (byte) 0);
        entityData.define(DATA_CUSTOM_MODEL, "");
        entityData.define(DATA_PLAYER_MODEL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("MannequinSlimArms", slimArms());
        tag.putBoolean("MannequinCrouching", isMannequinCrouching());
        tag.putByte("MannequinParts", getPartsMask());
        tag.putFloat("MannequinScale", getEntityScale());
        tag.putFloat("MannequinTiltX", getTiltX());
        tag.putFloat("MannequinTiltZ", getTiltZ());
        tag.putByte("MannequinModel", (byte) getModelType());
        tag.putString("MannequinSkin", getSkin());
        tag.putString("MannequinCustomModel", getCustomModel());
        tag.putBoolean("MannequinPlayerModel", usesPlayerModel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MannequinSlimArms")) setSlimArms(tag.getBoolean("MannequinSlimArms"));
        if (tag.contains("MannequinCrouching")) setMannequinCrouching(tag.getBoolean("MannequinCrouching"));
        if (tag.contains("MannequinParts")) setPartsMask(tag.getByte("MannequinParts"));
        if (tag.contains("MannequinScale")) setEntityScale(tag.getFloat("MannequinScale"));
        if (tag.contains("MannequinTiltX")) setTiltX(tag.getFloat("MannequinTiltX"));
        if (tag.contains("MannequinTiltZ")) setTiltZ(tag.getFloat("MannequinTiltZ"));
        if (tag.contains("MannequinModel")) setModelType(tag.getByte("MannequinModel"));
        if (tag.contains("MannequinPlayerModel")) setPlayerModel(tag.getBoolean("MannequinPlayerModel"));
        if (tag.contains("MannequinCustomModel")) setCustomModel(tag.getString("MannequinCustomModel"));
        if (tag.contains("MannequinSkin")) setSkin(tag.getString("MannequinSkin"));
        if (tag.contains("Small")) setMannequinSmall(tag.getBoolean("Small"));
    }

    // ── Values ────────────────────────────────────────────────────────────────

    public boolean slimArms() { return entityData.get(DATA_SLIM); }
    public void setSlimArms(boolean value) { entityData.set(DATA_SLIM, value); }

    /** The vanilla "small" setter is private, so the mannequin keeps its own flag (used for the baby proportions and hitbox). */
    @Override
    public boolean isSmall() { return entityData.get(DATA_SMALL); }
    public void setMannequinSmall(boolean value) { entityData.set(DATA_SMALL, value); }

    public boolean isMannequinCrouching() { return entityData.get(DATA_CROUCHING); }
    public void setMannequinCrouching(boolean value) { entityData.set(DATA_CROUCHING, value); }

    public byte getPartsMask() { return entityData.get(DATA_PARTS); }
    public void setPartsMask(byte parts) { entityData.set(DATA_PARTS, parts); }

    public boolean isPartShown(PlayerModelPart part) {
        return (getPartsMask() & part.getMask()) == part.getMask();
    }

    /** A custom skin: a base64 encoded 64x64 or 64x32 PNG, or "" for the plain white mannequin. */
    public String getSkin() { return entityData.get(DATA_SKIN); }

    /** Client-side dummy for "imitate mannequin": the value was already checked on the server. */
    public void setSkinUnchecked(String base64) { entityData.set(DATA_SKIN, base64); }

    public boolean hasSkin() { return !getSkin().isEmpty(); }

    public void setSkin(String base64) {
        entityData.set(DATA_SKIN, MannequinSkinData.isValid(base64, getModelType()) ? base64 : "");
    }

    /** An uploaded model: gzip + base64 text (see CustomModelCodec on the client), or "". */
    public String getCustomModel() { return entityData.get(DATA_CUSTOM_MODEL); }

    /** True: drawn with the game's own player model, so mods that change players (Fresh Moves ...) change it too. */
    public boolean usesPlayerModel() { return entityData.get(DATA_PLAYER_MODEL); }

    public void setPlayerModel(boolean value) { entityData.set(DATA_PLAYER_MODEL, value); }

    public boolean hasCustomModel() { return !getCustomModel().isEmpty(); }

    public void setCustomModel(String data) {
        entityData.set(DATA_CUSTOM_MODEL, MannequinSkinData.isValidModel(data) ? data : "");
    }

    public int getModelType() { return entityData.get(DATA_MODEL); }

    /** Changing the model drops a skin that does not fit the new one. */
    public void setModelType(int model) {
        int m = model >= 0 && model < MODEL_COUNT ? model : MODEL_PLAYER;
        entityData.set(DATA_MODEL, (byte) m);
        if (!MannequinSkinData.isValid(getSkin(), m)) entityData.set(DATA_SKIN, "");
    }

    public float getEntityScale() { return entityData.get(DATA_SCALE); }

    public void setEntityScale(float scale) {
        entityData.set(DATA_SCALE, Float.isFinite(scale) ? Mth.clamp(scale, MIN_SCALE, MAX_SCALE) : 1.0F);
    }

    public float getTiltX() { return entityData.get(DATA_TILT_X); }
    public void setTiltX(float value) { entityData.set(DATA_TILT_X, value); }

    public float getTiltZ() { return entityData.get(DATA_TILT_Z); }
    public void setTiltZ(float value) { entityData.set(DATA_TILT_Z, value); }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SCALE.equals(key) || DATA_SMALL.equals(key)) {
            refreshDimensions();
        }
    }

    /** The hitbox grows and shrinks with the scale. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getEntityScale());
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    public InteractionResult interactAt(Player player, Vec3 hit, InteractionHand hand) {
        if (player.isShiftKeyDown() && player.getAbilities().mayBuild && !player.isSpectator()) {
            if (level().isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.kiko.client.mannequin.MannequinClient.open(this));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.interactAt(player, hit, hand);
    }

    // ── Breaking (the vanilla armor stand would drop an armor stand) ──────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            kill();
            return false;
        }
        if (isInvulnerableTo(source)) return false;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            breakMannequin();
            return false;
        }
        Entity attacker = source.getEntity();
        boolean arrow = source.getDirectEntity() instanceof AbstractArrow;
        if (!(attacker instanceof Player) && !arrow) return false;
        if (attacker instanceof Player player) {
            if (!player.getAbilities().mayBuild) return false;
            if (player.getAbilities().instabuild) {
                playBreakSound();
                discard();
                return true;
            }
        }
        long now = level().getGameTime();
        if (now - lastHitTick > 5L && !arrow) {
            level().broadcastEntityEvent(this, (byte) 32);
            gameEvent(GameEvent.ENTITY_DAMAGE, attacker);
            lastHitTick = now;
        } else {
            breakMannequin();
        }
        return true;
    }

    private void playBreakSound() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ARMOR_STAND_BREAK, getSoundSource(), 1.0F, 1.0F);
    }

    private void breakMannequin() {
        Block.popResource(level(), blockPosition(), createItemStack());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = getItemBySlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level(), blockPosition().above(), stack);
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        playBreakSound();
        discard();
    }

    /**
     * The mannequin item, carrying this mannequin's pose, skin, scale and style, so a broken or
     * picked mannequin can be placed again exactly as it was (equipment is dropped separately).
     */
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(ModItems.MANNEQUIN.get());
        CompoundTag all = new CompoundTag();
        addAdditionalSaveData(all);
        CompoundTag kept = new CompoundTag();
        for (String key : new String[]{"Small", "ShowArms", "NoBasePlate", "Pose", "Invisible",
                "MannequinSlimArms", "MannequinCrouching", "MannequinParts", "MannequinScale",
                "MannequinTiltX", "MannequinTiltZ", "MannequinSkin", "MannequinModel", "MannequinCustomModel", "MannequinPlayerModel"}) {
            if (all.contains(key)) kept.put(key, all.get(key).copy());
        }
        stack.addTagElement("EntityTag", kept);
        return stack;
    }

    @Override
    public ItemStack getPickResult() {
        return createItemStack();
    }
}
