package net.kiko.network;

import net.kiko.pose.ClientPoseStore;
import net.kiko.pose.PoseKind;
import net.kiko.pose.PoseState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: "this player's pose is now this". */
public class PoseS2CPacket {

    private final UUID player;
    private final PoseState state;

    public PoseS2CPacket(UUID player, PoseState state) {
        this.player = player;
        this.state = state;
    }

    public PoseS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), new PoseState(PoseKind.byId(buf.readByte()), PoseKind.byId(buf.readByte())));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeByte(state.right().ordinal());
        buf.writeByte(state.left().ordinal());
    }

    public static void handle(PoseS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientPoseStore.set(msg.player, msg.state);
    }
}
