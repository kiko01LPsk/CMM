package net.kiko.mannequin;

import net.kiko.KikoMod;
import net.kiko.network.MannequinModeS2CPacket;
import net.kiko.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MannequinModeServerEvents {

    private MannequinModeServerEvents() {}

    /** A player who starts seeing someone who is imitating a mannequin is told about it. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            MannequinMode mode = ServerMannequinModeStore.get(target.getUUID());
            if (mode.isActive()) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                        new MannequinModeS2CPacket(target.getUUID(), mode));
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerMannequinModeStore.remove(event.getEntity().getUUID());
    }
}
