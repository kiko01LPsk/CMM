package net.kiko.network;

import net.kiko.morph.ClientMorphStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: "this player is morphed into this mob type" (empty = not morphed). */
public class MorphS2CPacket {
    private final UUID player;
    private final String id;

    public MorphS2CPacket(UUID player, String id) {
        this.player = player;
        this.id = id;
    }

    public MorphS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readUtf(256));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeUtf(id, 256);
    }

    public static void handle(MorphS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientMorphStore.set(msg.player, msg.id);
    }
}
