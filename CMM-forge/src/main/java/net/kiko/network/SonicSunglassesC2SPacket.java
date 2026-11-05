package net.kiko.network;

import net.kiko.item.SonicSunglassesItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the Sonic Sunglasses key was pressed (use, or sneak + key = change mode). */
public class SonicSunglassesC2SPacket {
    private final byte action;

    public SonicSunglassesC2SPacket(byte action) { this.action = action; }
    public SonicSunglassesC2SPacket(FriendlyByteBuf buf) { this(buf.readByte()); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeByte(action); }

    public static void handle(SonicSunglassesC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) SonicSunglassesItem.handleKey(player, msg.action);
    }
}
