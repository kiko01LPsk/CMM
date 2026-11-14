package net.kiko.network;

import net.kiko.shield.ShieldThrow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: sneak + left click with Captain America's shield in the main hand. */
public class ShieldThrowC2SPacket {

    public ShieldThrowC2SPacket() {}

    public ShieldThrowC2SPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(ShieldThrowC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) ShieldThrow.throwFrom(player);
    }
}
