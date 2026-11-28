package net.kiko.network;

import net.kiko.mjolnir.MjolnirCall;
import net.kiko.mjolnir.MjolnirFlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the player pressed the Mjolnir call key, or starts/stops the Mjolnir flight. */
public class MjolnirActionC2SPacket {

    public static final byte CALL = 0;
    public static final byte START_FLIGHT = 1;
    public static final byte STOP_FLIGHT = 2;
    public static final byte LIGHTNING = 3;
    public static final byte OPEN_WALKMAN = 4;

    private final byte action;

    public MjolnirActionC2SPacket(byte action) {
        this.action = action;
    }

    public MjolnirActionC2SPacket(FriendlyByteBuf buf) {
        this(buf.readByte());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(action);
    }

    public static void handle(MjolnirActionC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;
        switch (msg.action) {
            case CALL -> {
                if (!net.kiko.trident.GodspearCall.call(player)) MjolnirCall.call(player);
            }
            case START_FLIGHT -> MjolnirFlight.start(player);
            case STOP_FLIGHT -> MjolnirFlight.stop(player);
            case LIGHTNING -> MjolnirCall.lightning(player);
            case OPEN_WALKMAN -> net.kiko.item.WalkmanItem.openFromKey(player);
            default -> {
            }
        }
    }
}
