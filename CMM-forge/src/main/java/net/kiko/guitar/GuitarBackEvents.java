package net.kiko.guitar;

import net.kiko.KikoMod;
import net.kiko.item.GuitarItem;
import net.kiko.network.GuitarBackS2CPacket;
import net.kiko.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Server side of the guitar on the back: the flag lives in the player's persistent data and is sent to the clients. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuitarBackEvents {

    private static final String TAG = "kiko_guitar_on_back";

    private GuitarBackEvents() {}

    public static boolean isOnBack(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG);
    }

    public static void setOnBack(ServerPlayer player, boolean value) {
        if (value) player.getPersistentData().putBoolean(TAG, true);
        else player.getPersistentData().remove(TAG);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new GuitarBackS2CPacket(player.getUUID(), value));
    }

    private static boolean hasGuitar(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GuitarItem) return true;
        }
        return player.getOffhandItem().getItem() instanceof GuitarItem;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 == 0 && isOnBack(player) && !hasGuitar(player)) setOnBack(player, false);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isOnBack(player)) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new GuitarBackS2CPacket(player.getUUID(), true));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer
                && isOnBack(target)) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                    new GuitarBackS2CPacket(target.getUUID(), true));
        }
    }
}
