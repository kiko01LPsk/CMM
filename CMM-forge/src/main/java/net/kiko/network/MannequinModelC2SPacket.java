package net.kiko.network;

import net.kiko.mannequin.Mannequin;
import net.kiko.mannequin.MannequinSkinData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "give this mannequin this uploaded model" (gzip + base64 text, or "" to remove it). */
public class MannequinModelC2SPacket {

    private final int entityId;
    private final String model;

    public MannequinModelC2SPacket(int entityId, String model) {
        this.entityId = entityId;
        this.model = model;
    }

    public MannequinModelC2SPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(32767));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(model, 32767);
    }

    public static void handle(MannequinModelC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null || !sender.getAbilities().mayBuild) return;
        Entity entity = sender.level().getEntity(msg.entityId);
        if (entity instanceof Mannequin mannequin && sender.distanceToSqr(mannequin) < 144.0D
                && MannequinSkinData.isValidModel(msg.model)) {
            mannequin.setCustomModel(msg.model);
        }
    }
}
