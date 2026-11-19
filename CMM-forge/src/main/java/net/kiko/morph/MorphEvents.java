package net.kiko.morph;

import net.kiko.KikoMod;
import net.kiko.network.ModNetwork;
import net.kiko.network.MorphS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/** Server side of the Morph wand: the chosen mob type lives in the player's persistent data and is sent to the clients. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorphEvents {

    private static final String TAG = "kiko_morph";

    private MorphEvents() {}

    /** Registry id of the mob the player is morphed into, or "" when not morphed. */
    public static String get(ServerPlayer player) {
        return player.getPersistentData().getString(TAG);
    }

    public static void set(ServerPlayer player, EntityType<?> type) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (key == null) return;
        apply(player, key.toString());
    }

    public static void clear(ServerPlayer player) {
        apply(player, "");
    }

    private static void apply(ServerPlayer player, String id) {
        if (id.isEmpty()) player.getPersistentData().remove(TAG);
        else player.getPersistentData().putString(TAG, id);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new MorphS2CPacket(player.getUUID(), id));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer
                && !get(target).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                    new MorphS2CPacket(target.getUUID(), get(target)));
        }
    }
}
