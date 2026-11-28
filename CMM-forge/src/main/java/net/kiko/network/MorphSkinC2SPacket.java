package net.kiko.network;

import net.kiko.morph.MorphEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "use this PNG as the skin of my morph" (empty = back to the normal skin). */
public class MorphSkinC2SPacket {
    public static final int MAX_BYTES = 30000;

    private final byte[] png;

    public MorphSkinC2SPacket(byte[] png) {
        this.png = png;
    }

    public MorphSkinC2SPacket(FriendlyByteBuf buf) {
        this(buf.readByteArray(MAX_BYTES));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByteArray(png);
    }

    public static void handle(MorphSkinC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null || MorphEvents.get(player).isEmpty()) return;
        if (msg.png.length == 0) {
            MorphEvents.setSkin(player, null);
            return;
        }
        boolean isPng = msg.png.length > 8 && (msg.png[0] & 0xFF) == 0x89 && msg.png[1] == 'P' && msg.png[2] == 'N' && msg.png[3] == 'G';
        if (isPng) MorphEvents.setSkin(player, msg.png);
    }
}
