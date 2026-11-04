package net.kiko.network;

import net.kiko.mannequin.Mannequin;
import net.kiko.mannequin.MannequinSkinData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "give this mannequin this skin" (base64 PNG, or "" to remove the skin). */
public class MannequinSkinC2SPacket {

    private final int entityId;
    private final String skin;

    public MannequinSkinC2SPacket(int entityId, String skin) {
        this.entityId = entityId;
        this.skin = skin;
    }

    public MannequinSkinC2SPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(32767));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(skin, 32767);
    }

    public static void handle(MannequinSkinC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null || !sender.getAbilities().mayBuild) return;
        Entity entity = sender.level().getEntity(msg.entityId);
        if (entity instanceof Mannequin mannequin && sender.distanceToSqr(mannequin) < 144.0D) {
            if (MannequinSkinData.isValid(msg.skin, mannequin.getModelType())) mannequin.setSkin(msg.skin);
        }
    }
}
