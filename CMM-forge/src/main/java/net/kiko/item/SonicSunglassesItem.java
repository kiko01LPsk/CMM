package net.kiko.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Sunglasses worn on the head with two sonic modes. The "Sonic Sunglasses" key uses the current mode,
 * sneak + the key changes the mode. Redstone: switches the redstone block you look at (like the screwdriver).
 * Players: every other player nearby glows through walls (like a spectral arrow).
 */
public class SonicSunglassesItem extends ArmorItem {

    public static final byte USE = 0;
    public static final byte CYCLE = 1;

    private static final String MODE_TAG = "Mode";
    private static final double REDSTONE_RANGE = 8.0D;
    private static final double PLAYER_RANGE = 64.0D;

    public enum Mode {
        OFF("off"), REDSTONE("redstone"), PLAYERS("players");

        private final String key;
        Mode(String key) { this.key = key; }
        public String langKey() { return "item.kiko.sonic_sunglasses.mode." + key; }
        public Mode next() { Mode[] a = values(); return a[(ordinal() + 1) % a.length]; }
    }

    public SonicSunglassesItem(Properties properties) {
        super(ModArmorMaterials.SONIC_SUNGLASSES, Type.HELMET, properties);
    }

    public static Mode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int i = tag == null ? 0 : tag.getInt(MODE_TAG);
        Mode[] all = Mode.values();
        return i >= 0 && i < all.length ? all[i] : Mode.OFF;
    }

    public static void setMode(ItemStack stack, Mode mode) {
        stack.getOrCreateTag().putInt(MODE_TAG, mode.ordinal());
    }

    /** Server side: the player pressed the key while (maybe) wearing the glasses. */
    public static void handleKey(ServerPlayer player, byte action) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(stack.getItem() instanceof SonicSunglassesItem)) return;

        if (action == CYCLE) {
            Mode mode = getMode(stack).next();
            setMode(stack, mode);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SONIC_SWITCH.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
            player.displayClientMessage(Component.translatable("item.kiko.sonic_sunglasses.mode",
                    Component.translatable(mode.langKey())), true);
            return;
        }
        if (getMode(stack) != Mode.REDSTONE) return;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.SONIC_USE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        BlockHitResult hit = SonicScrewdriverItem.rayBlock(player, REDSTONE_RANGE);
        if (hit.getType() != HitResult.Type.BLOCK
                || !SonicScrewdriverItem.activate((ServerLevel) player.level(), hit.getBlockPos(), player)) {
            player.displayClientMessage(Component.translatable("item.kiko.sonic_screwdriver.notice.nothing_to_activate"), true);
        }
    }

    /** Server side, called every few ticks: players mode makes the other players glow. */
    public static void tickPlayers(ServerPlayer player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(stack.getItem() instanceof SonicSunglassesItem) || getMode(stack) != Mode.PLAYERS) return;
        for (Player other : player.level().players()) {
            if (other != player && !other.isSpectator() && other.distanceToSqr(player) <= PLAYER_RANGE * PLAYER_RANGE) {
                other.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.kiko.sonic_sunglasses.mode",
                Component.translatable(getMode(stack).langKey())).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.kiko.sonic_sunglasses.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
}
