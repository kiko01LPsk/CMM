package net.kiko.network;

import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.ScreenGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "this screen should play this link" (sent by the monitor settings window). */
public class MonitorSetC2SPacket {

    private final BlockPos pos;
    private final String url;
    private final boolean playing;
    private final boolean restart;
    private final boolean loop;
    private final int volume;

    public MonitorSetC2SPacket(BlockPos pos, String url, boolean playing, boolean restart, boolean loop, int volume) {
        this.pos = pos;
        this.url = url;
        this.playing = playing;
        this.restart = restart;
        this.loop = loop;
        this.volume = volume;
    }

    public MonitorSetC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(1024), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(url, 1024);
        buf.writeBoolean(playing);
        buf.writeBoolean(restart);
        buf.writeBoolean(loop);
        buf.writeVarInt(volume);
    }

    public static void handle(MonitorSetC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null || !sender.getAbilities().mayBuild) return;
        Level level = sender.level();
        if (sender.distanceToSqr(msg.pos.getX() + 0.5D, msg.pos.getY() + 0.5D, msg.pos.getZ() + 0.5D) > 256.0D) return;
        if (!level.isLoaded(msg.pos)) return;

        String url = msg.url.trim();
        for (int i = 0; i < url.length(); i++) {
            if (Character.isISOControl(url.charAt(i))) return;
        }
        if (!url.isEmpty() && !(url.regionMatches(true, 0, "http://", 0, 7) || url.regionMatches(true, 0, "https://", 0, 8))) return;

        ScreenGroup group = ScreenGroup.find(level, msg.pos);
        if (group == null) return;
        long now = level.getGameTime();
        for (BlockPos member : group.members) {
            if (level.getBlockEntity(member) instanceof MonitorBlockEntity be) {
                boolean changed = !be.getUrl().equals(url) || !be.isPlaying();
                long start = (msg.restart || (msg.playing && changed)) ? now : be.getStartTick();
                be.set(url, msg.playing && !url.isEmpty(), msg.loop, msg.volume, start);
            }
        }
    }
}
