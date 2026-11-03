package net.kiko.network;

import net.kiko.mannequin.MannequinMode;
import net.kiko.mannequin.ServerMannequinModeStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Client -> server: "I now imitate a mannequin like this" (or an inactive mode to stop). */
public class MannequinModeC2SPacket {

    private final MannequinMode mode;

    public MannequinModeC2SPacket(MannequinMode mode) {
        this.mode = mode;
    }

    public MannequinModeC2SPacket(FriendlyByteBuf buf) {
        this(new MannequinMode(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        mode.write(buf);
    }

    public static void handle(MannequinModeC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) return;
        MannequinMode clean = msg.mode.sanitized();
        ServerMannequinModeStore.set(sender.getUUID(), clean);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                new MannequinModeS2CPacket(sender.getUUID(), clean));
    }
}
