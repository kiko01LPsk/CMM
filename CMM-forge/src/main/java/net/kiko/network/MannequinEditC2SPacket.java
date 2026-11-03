package net.kiko.network;

import net.kiko.mannequin.Mannequin;
import net.kiko.mannequin.MannequinSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "set this mannequin to these settings" (sent by the edit screen). */
public class MannequinEditC2SPacket {

    private final int entityId;
    private final MannequinSettings settings;

    public MannequinEditC2SPacket(int entityId, MannequinSettings settings) {
        this.entityId = entityId;
        this.settings = settings;
    }

    public MannequinEditC2SPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), new MannequinSettings(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        settings.write(buf);
    }

    public static void handle(MannequinEditC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null || !sender.getAbilities().mayBuild) return;
        Entity entity = sender.level().getEntity(msg.entityId);
        if (entity instanceof Mannequin mannequin && sender.distanceToSqr(mannequin) < 144.0D) {
            msg.settings.applyTo(mannequin);
        }
    }
}
