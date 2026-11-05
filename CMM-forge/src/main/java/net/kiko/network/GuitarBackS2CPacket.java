package net.kiko.network;

import net.kiko.guitar.ClientGuitarBackStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: "this player carries a guitar on the back (or not)". */
public class GuitarBackS2CPacket {
    private final UUID player;
    private final boolean onBack;

    public GuitarBackS2CPacket(UUID player, boolean onBack) {
        this.player = player;
        this.onBack = onBack;
    }

    public GuitarBackS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeBoolean(onBack);
    }

    public static void handle(GuitarBackS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientGuitarBackStore.set(msg.player, msg.onBack);
    }
}
