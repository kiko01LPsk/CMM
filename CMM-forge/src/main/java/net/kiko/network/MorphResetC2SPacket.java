package net.kiko.network;

import net.kiko.morph.MorphEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the Reset morph key was pressed. */
public class MorphResetC2SPacket {
    public MorphResetC2SPacket() {}
    public MorphResetC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(MorphResetC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && !MorphEvents.get(player).isEmpty()) MorphEvents.clear(player);
    }
}
