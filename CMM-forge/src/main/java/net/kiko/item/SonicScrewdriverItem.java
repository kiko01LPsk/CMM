package net.kiko.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * One sonic screwdriver with several modes (shown by its colour). Sneak + right-click changes the mode.
 * Disabled: does nothing. Red: scans mobs and players. Green: redstone. Teal: scans blocks.
 * Magenta: highlights a mob or player. Yellow: reads the surroundings (biome, light, time ...). White: lights up the surroundings while held.
 */
public class SonicScrewdriverItem extends Item {

    private static final String MODE_TAG = "Mode";
    private static final double REDSTONE_RANGE = 8.0D;
    private static final double SCAN_RANGE = 24.0D;

    /** Extra line shown when scanning these players (lower-case nick -> label, value). */
    private static final java.util.Map<String, String[]> PLAYER_EXTRAS = java.util.Map.of(
            "kiko01lpsk", new String[]{"Race", "Time Lord"},
            "bambi2009521", new String[]{"Sexuality", "Gay"},
            "tomcaik", new String[]{"Real name", "Tom\u010d\u00ed\u010da"},
            "justscorpy", new String[]{"Real name", "Scorp\u00edna"});

    public SonicScrewdriverItem(Properties properties) {
        super(properties);
    }

    // ── Mode ──────────────────────────────────────────────────────────────────

    public static SonicMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int index = tag == null ? 0 : tag.getInt(MODE_TAG);
        SonicMode[] all = SonicMode.values();
        return index >= 0 && index < all.length ? all[index] : SonicMode.DISABLED;
    }

    public static void setMode(ItemStack stack, SonicMode mode) {
        stack.getOrCreateTag().putInt(MODE_TAG, mode.ordinal());
    }

    /** True for a sonic screwdriver in white (light) mode. */
    public static boolean isGlowing(ItemStack stack) {
        return stack.getItem() instanceof SonicScrewdriverItem && getMode(stack) == SonicMode.LIGHT;
    }

    private InteractionResultHolder<ItemStack> cycleMode(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide) {
            SonicMode mode = getMode(stack).next();
            setMode(stack, mode);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SONIC_SWITCH.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
            player.displayClientMessage(Component.translatable("item.kiko.sonic_screwdriver.mode",
                    Component.translatable(mode.langKey())), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // ── Using it ──────────────────────────────────────────────────────────────

    /** Sneak + right-click on a block changes the mode (nothing else uses the block then). */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return cycleMode(context.getLevel(), player, context.getItemInHand()).getResult();
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return cycleMode(level, player, stack);
        }
        SonicMode mode = getMode(stack);
        if (mode == SonicMode.DISABLED) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SONIC_USE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            switch (mode) {
                case REDSTONE -> redstone(serverPlayer);
                case SCANNER -> {
                    EntityHitResult hit = rayEntity(serverPlayer, SCAN_RANGE);
                    if (hit != null) scan(serverPlayer, hit.getEntity()); else notice(serverPlayer, "no_target");
                }
                case ANALYZER -> {
                    BlockHitResult hit = rayBlock(serverPlayer, SCAN_RANGE);
                    if (hit.getType() == HitResult.Type.BLOCK) analyze(serverPlayer, hit.getBlockPos());
                    else notice(serverPlayer, "no_target");
                }
                case HIGHLIGHT -> {
                    EntityHitResult hit = rayEntity(serverPlayer, SCAN_RANGE);
                    if (hit != null) highlight(serverPlayer, hit.getEntity()); else notice(serverPlayer, "no_target");
                }
                case ENVIRONMENT -> environment(serverPlayer);
                default -> { }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Left-click on a mob or player scans / highlights it (and does not hit it). */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        SonicMode mode = getMode(stack);
        if (mode != SonicMode.SCANNER && mode != SonicMode.HIGHLIGHT) return false;
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SONIC_USE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            if (mode == SonicMode.SCANNER) scan(serverPlayer, entity); else highlight(serverPlayer, entity);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        SonicMode mode = getMode(stack);
        tooltip.add(Component.translatable("item.kiko.sonic_screwdriver.mode", Component.translatable(mode.langKey()))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(mode.langKey() + ".info").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.kiko.sonic_screwdriver.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    // ── Aiming ────────────────────────────────────────────────────────────────

    private static BlockHitResult rayBlock(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    /** The entity the player is looking at (not hidden behind a block), or null. */
    private static EntityHitResult rayEntity(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult block = rayBlock(player, range);
        double maxDistance = block.getType() == HitResult.Type.MISS ? range : block.getLocation().distanceTo(eye);
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        return ProjectileUtil.getEntityHitResult(player, eye, end, box,
                e -> !e.isSpectator() && e.isPickable(), maxDistance * maxDistance);
    }

    private static void notice(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("item.kiko.sonic_screwdriver.notice." + key), true);
    }

    // ── Red: redstone ─────────────────────────────────────────────────────────

    private void redstone(ServerPlayer player) {
        BlockHitResult hit = rayBlock(player, REDSTONE_RANGE);
        if (hit.getType() != HitResult.Type.BLOCK || !activate((ServerLevel) player.level(), hit.getBlockPos(), player)) {
            notice(player, "nothing_to_activate");
        }
    }

    /** Switches a redstone-related block: toggles it, presses it or triggers it. */
    private static boolean activate(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof LeverBlock lever) {
            lever.pull(state, level, pos);
            level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
            return true;
        }
        if (block instanceof ButtonBlock button) {
            if (!state.getValue(BlockStateProperties.POWERED)) button.press(state, level, pos);
            level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
            return true;
        }
        if (block instanceof DoorBlock door) {
            door.setOpen(player, level, state, pos, !state.getValue(BlockStateProperties.OPEN));
            return true;
        }
        if (block instanceof TrapDoorBlock || block instanceof FenceGateBlock) {
            boolean open = !state.getValue(BlockStateProperties.OPEN);
            level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 10);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return true;
        }
        if (block instanceof RedstoneLampBlock) {
            level.setBlock(pos, state.cycle(BlockStateProperties.LIT), 2);
            return true;
        }
        if (block instanceof NoteBlock) {
            level.blockEvent(pos, block, 0, 0);
            level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);
            return true;
        }
        if (block instanceof DispenserBlock) {
            level.scheduleTick(pos, block, 4);
            return true;
        }
        if (block instanceof BellBlock bell) {
            bell.attemptToRing(level, pos, (Direction) null);
            return true;
        }
        return false;
    }

    // ── Green: scan a mob or player ───────────────────────────────────────────

    private static MutableComponent line(String label, String value) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private static String number(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String position(Vec3 p) {
        return String.format(java.util.Locale.ROOT, "%.1f, %.1f, %.1f", p.x, p.y, p.z);
    }

    private static void scan(ServerPlayer viewer, Entity target) {
        viewer.sendSystemMessage(Component.literal("--- Scan ---").withStyle(ChatFormatting.YELLOW));

        String typeName = target.getType().getDescription().getString();
        if (target instanceof Player other) {
            viewer.sendSystemMessage(line("Type", "Player - " + other.getGameProfile().getName()));
            String[] extra = PLAYER_EXTRAS.get(other.getGameProfile().getName().toLowerCase(java.util.Locale.ROOT));
            if (extra != null) viewer.sendSystemMessage(line(extra[0], extra[1]));
        } else {
            String id = String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(target.getType()));
            String nick = target.hasCustomName() ? " \"" + target.getCustomName().getString() + "\"" : "";
            viewer.sendSystemMessage(line("Type", "Mob - " + typeName + nick + " (" + id + ")"));
        }

        if (target instanceof LivingEntity living) {
            viewer.sendSystemMessage(line("Health", number(living.getHealth()) + " / " + number(living.getMaxHealth())));
            viewer.sendSystemMessage(line("Armor", String.valueOf(living.getArmorValue())));
            if (living.getActiveEffects().isEmpty()) {
                viewer.sendSystemMessage(line("Effects", "none"));
            } else {
                StringBuilder effects = new StringBuilder();
                for (MobEffectInstance effect : living.getActiveEffects()) {
                    if (effects.length() > 0) effects.append(", ");
                    effects.append(effect.getEffect().getDisplayName().getString())
                            .append(" ").append(effect.getAmplifier() + 1);
                    if (effect.isInfiniteDuration()) {
                        effects.append(" (infinite)");
                    } else {
                        int seconds = effect.getDuration() / 20;
                        effects.append(String.format(java.util.Locale.ROOT, " (%d:%02d)", seconds / 60, seconds % 60));
                    }
                }
                viewer.sendSystemMessage(line("Effects", effects.toString()));
            }
            if (target instanceof Player other) {
                viewer.sendSystemMessage(line("Food", other.getFoodData().getFoodLevel() + " / 20, level "
                        + other.experienceLevel));
                if (other instanceof ServerPlayer sp) {
                    viewer.sendSystemMessage(line("Game mode", sp.gameMode.getGameModeForPlayer().getName()));
                }
            } else {
                if (living.isBaby()) viewer.sendSystemMessage(line("Age", "baby"));
                if (target instanceof TamableAnimal pet && pet.isTame()) {
                    viewer.sendSystemMessage(line("Tamed", "yes"));
                }
            }
            ItemStack held = living.getMainHandItem();
            if (!held.isEmpty()) viewer.sendSystemMessage(line("Holding", held.getHoverName().getString()));
        }
        viewer.sendSystemMessage(line("Position", position(target.position())));
        viewer.sendSystemMessage(line("Distance", number(viewer.distanceTo(target)) + " blocks"));
    }

    // ── Teal: scan a block ────────────────────────────────────────────────────

    private static void analyze(ServerPlayer viewer, BlockPos pos) {
        ServerLevel level = (ServerLevel) viewer.level();
        BlockState state = level.getBlockState(pos);
        viewer.sendSystemMessage(Component.literal("--- Block scan ---").withStyle(ChatFormatting.YELLOW));
        viewer.sendSystemMessage(line("Block", state.getBlock().getName().getString()
                + " (" + ForgeRegistries.BLOCKS.getKey(state.getBlock()) + ")"));
        viewer.sendSystemMessage(line("Position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        float hardness = state.getDestroySpeed(level, pos);
        viewer.sendSystemMessage(line("Hardness", hardness < 0 ? "unbreakable" : number(hardness)
                + (state.requiresCorrectToolForDrops() ? " (needs the right tool)" : "")));
        viewer.sendSystemMessage(line("Light emitted", String.valueOf(state.getLightEmission(level, pos))));
        viewer.sendSystemMessage(line("Redstone power received", String.valueOf(level.getBestNeighborSignal(pos))));
        if (state.isSignalSource()) {
            viewer.sendSystemMessage(line("Redstone output", String.valueOf(state.getSignal(level, pos, Direction.UP))));
        }
        if (!state.getProperties().isEmpty()) {
            StringBuilder props = new StringBuilder();
            for (Property<?> property : state.getProperties()) {
                if (props.length() > 0) props.append(", ");
                props.append(property.getName()).append("=").append(state.getValue(property));
            }
            viewer.sendSystemMessage(line("State", props.toString()));
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            String id = String.valueOf(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(entity.getType()));
            String extra = "";
            if (entity instanceof Container container) {
                int used = 0;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (!container.getItem(i).isEmpty()) used++;
                }
                extra = ", " + used + " / " + container.getContainerSize() + " slots used";
            }
            viewer.sendSystemMessage(line("Block entity", id + extra));
        }
    }

    // ── Magenta: highlight ────────────────────────────────────────────────────

    private static void highlight(ServerPlayer viewer, Entity target) {
        if (!(target instanceof LivingEntity living)) {
            notice(viewer, "not_living");
            return;
        }
        boolean on = !living.hasEffect(MobEffects.GLOWING);
        if (on) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0, false, false));
        } else {
            living.removeEffect(MobEffects.GLOWING);
        }
        viewer.displayClientMessage(Component.translatable(
                on ? "item.kiko.sonic_screwdriver.notice.highlight_on" : "item.kiko.sonic_screwdriver.notice.highlight_off",
                target.getDisplayName()), true);
    }

    // ── Yellow: environment readout ───────────────────────────────────────────

    private static void environment(ServerPlayer viewer) {
        ServerLevel level = (ServerLevel) viewer.level();
        BlockPos pos = viewer.blockPosition();
        viewer.sendSystemMessage(Component.literal("--- Environment ---").withStyle(ChatFormatting.YELLOW));
        viewer.sendSystemMessage(line("Dimension", level.dimension().location().toString()));
        Holder<Biome> biome = level.getBiome(pos);
        viewer.sendSystemMessage(line("Biome", biome.unwrapKey().map(k -> k.location().toString()).orElse("unknown")));
        viewer.sendSystemMessage(line("Temperature", number(biome.value().getBaseTemperature())));
        viewer.sendSystemMessage(line("Position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        viewer.sendSystemMessage(line("Light", "block " + level.getBrightness(LightLayer.BLOCK, pos)
                + ", sky " + level.getBrightness(LightLayer.SKY, pos)));
        if (level.dimensionType().natural()) {
            long ticks = level.getDayTime() % 24000L;
            long hours = (ticks / 1000L + 6L) % 24L;
            long minutes = (ticks % 1000L) * 60L / 1000L;
            viewer.sendSystemMessage(line("Time", String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes)));
            viewer.sendSystemMessage(line("Weather", level.isThundering() ? "thunderstorm" : level.isRaining() ? "rain" : "clear"));
            viewer.sendSystemMessage(line("Moon phase", String.valueOf(level.getMoonPhase())));
        }
    }
}
