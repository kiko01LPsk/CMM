package net.kiko.morph;

import net.kiko.KikoMod;
import net.kiko.network.ModNetwork;
import net.kiko.network.MorphS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/** Server side of the Morph wand: the chosen mob type lives in the player's persistent data and is sent to the clients. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorphEvents {

    private static final String TAG = "kiko_morph";
    private static final String SKIN_TAG = "kiko_morph_skin";

    private MorphEvents() {}

    /** Registry id of the mob the player is morphed into, or "" when not morphed. */
    public static String get(ServerPlayer player) {
        return player.getPersistentData().getString(TAG);
    }

    /** A baby target is stored as "id#baby@uuid": baby flag for the baby variant, and the clicked mob's UUID so resource-pack skin variants (picked by UUID) match. */
    public static void set(ServerPlayer player, net.minecraft.world.entity.LivingEntity target) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (key == null) return;
        apply(player, key + (target.isBaby() ? "#baby" : "") + "@" + target.getUUID());
    }

    public static void clear(ServerPlayer player) {
        apply(player, "");
    }

    private static void apply(ServerPlayer player, String id) {
        boolean hadSkin = player.getPersistentData().contains(SKIN_TAG);
        player.getPersistentData().remove(SKIN_TAG); // a new morph starts with the normal skin
        if (id.isEmpty()) player.getPersistentData().remove(TAG);
        else player.getPersistentData().putString(TAG, id);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new MorphS2CPacket(player.getUUID(), id));
        if (hadSkin) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new net.kiko.network.MorphSkinS2CPacket(player.getUUID(), new byte[0]));
        }
    }

    /** Custom skin PNG of the current morph (null = normal skin). */
    public static void setSkin(ServerPlayer player, byte[] png) {
        if (png == null || png.length == 0) {
            player.getPersistentData().remove(SKIN_TAG);
            png = new byte[0];
        } else {
            player.getPersistentData().putByteArray(SKIN_TAG, png);
        }
        final byte[] data = png;
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new net.kiko.network.MorphSkinS2CPacket(player.getUUID(), data));
    }

    private static void sendSkin(ServerPlayer owner, java.util.function.Supplier<PacketDistributor.PacketTarget> target) {
        if (owner.getPersistentData().contains(SKIN_TAG)) {
            ModNetwork.CHANNEL.send(target.get(), new net.kiko.network.MorphSkinS2CPacket(
                    owner.getUUID(), owner.getPersistentData().getByteArray(SKIN_TAG)));
        }
    }

    /**
     * Runs before the mob's own right-click behaviour, so the wand also works on villagers, llamas, horses and the like
     * (otherwise their trade screen / saddle / feeding would take the click first).
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getItemStack().getItem() instanceof net.kiko.item.MorphWandItem)) return;
        if (event.getTarget() instanceof net.minecraft.world.entity.player.Player
                || !(event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target)) return;
        if (event.getEntity() instanceof ServerPlayer sp) {
            set(sp, target);
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.kiko.morphed",
                    target.getType().getDescription()), true);
            sp.level().playSound(null, sp.blockPosition(), net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
            sendSkin(player, () -> PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
            sendSkin(player, () -> PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer
                && !get(target).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                    new MorphS2CPacket(target.getUUID(), get(target)));
            sendSkin(target, () -> PacketDistributor.PLAYER.with(() -> viewer));
        }
    }
}
