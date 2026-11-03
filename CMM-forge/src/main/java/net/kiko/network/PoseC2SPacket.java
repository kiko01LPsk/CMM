package net.kiko.network;

import net.kiko.pose.PoseKind;
import net.kiko.pose.PoseState;
import net.kiko.pose.ServerPoseStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Client -> server: "my pose is now this". */
public class PoseC2SPacket {

    private final PoseState state;

    public PoseC2SPacket(PoseState state) {
        this.state = state;
    }

    public PoseC2SPacket(FriendlyByteBuf buf) {
        this(new PoseState(PoseKind.byId(buf.readByte()), PoseKind.byId(buf.readByte())));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(state.right().ordinal());
        buf.writeByte(state.left().ordinal());
    }

    public static void handle(PoseC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) return;
        if (msg.state.equals(ServerPoseStore.get(sender.getUUID()))) return;

        ServerPoseStore.set(sender.getUUID(), msg.state);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender),
                new PoseS2CPacket(sender.getUUID(), msg.state));
    }
}
