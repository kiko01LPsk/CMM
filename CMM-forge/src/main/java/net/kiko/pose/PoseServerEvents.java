package net.kiko.pose;

import net.kiko.KikoMod;
import net.kiko.network.ModNetwork;
import net.kiko.network.PoseS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PoseServerEvents {

    private PoseServerEvents() {}

    /** When a player starts seeing another player, tell them that player's current pose. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                    new PoseS2CPacket(target.getUUID(), ServerPoseStore.get(target.getUUID())));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPoseStore.remove(event.getEntity().getUUID());
    }
}
