package net.kiko.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: custom skin PNG of a morphed player (empty = none). */
public class MorphSkinS2CPacket {
    private final UUID player;
    private final byte[] png;

    public MorphSkinS2CPacket(UUID player, byte[] png) {
        this.player = player;
        this.png = png;
    }

    public MorphSkinS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readByteArray(MorphSkinC2SPacket.MAX_BYTES));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeByteArray(png);
    }

    public static void handle(MorphSkinS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.kiko.client.morph.ClientMorphSkins.set(msg.player, msg.png));
    }
}
