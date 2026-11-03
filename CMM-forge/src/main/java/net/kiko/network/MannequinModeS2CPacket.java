package net.kiko.network;

import net.kiko.mannequin.ClientMannequinModeStore;
import net.kiko.mannequin.MannequinMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client: "this player's mannequin mode is now this". */
public class MannequinModeS2CPacket {

    private final UUID player;
    private final MannequinMode mode;

    public MannequinModeS2CPacket(UUID player, MannequinMode mode) {
        this.player = player;
        this.mode = mode;
    }

    public MannequinModeS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), new MannequinMode(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        mode.write(buf);
    }

    public static void handle(MannequinModeS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientMannequinModeStore.set(msg.player, msg.mode);
    }
}
