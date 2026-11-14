package net.kiko.network;

import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.ScreenGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: something the monitor settings window asked a screen to do. */
public class MonitorSetC2SPacket {

    public static final int PLAY = 0;     // start the link from the beginning
    public static final int STOP = 1;
    public static final int PAUSE = 2;
    public static final int RESUME = 3;
    public static final int SEEK = 4;     // jump by 'delta' seconds
    public static final int SETTINGS = 5; // only loop / volume

    private final BlockPos pos;
    private final String url;
    private final int action;
    private final int delta;
    private final boolean loop;
    private final int volume;

    public MonitorSetC2SPacket(BlockPos pos, String url, int action, int delta, boolean loop, int volume) {
        this.pos = pos;
        this.url = url;
        this.action = action;
        this.delta = delta;
        this.loop = loop;
        this.volume = volume;
    }

    public MonitorSetC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(1024), buf.readVarInt(), buf.readInt(), buf.readBoolean(), buf.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(url, 1024);
        buf.writeVarInt(action);
        buf.writeInt(delta);
        buf.writeBoolean(loop);
        buf.writeVarInt(volume);
    }

    public static boolean validUrl(String url) {
        return url.regionMatches(true, 0, "http://", 0, 7) || url.regionMatches(true, 0, "https://", 0, 8)
                || url.regionMatches(true, 0, "file:", 0, 5);
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
        if (msg.action == PLAY && !validUrl(url)) return;

        ScreenGroup group = ScreenGroup.find(level, msg.pos);
        if (group == null) return;
        long now = level.getGameTime();
        int delta = Math.max(-3600, Math.min(3600, msg.delta));
        for (BlockPos member : group.members) {
            if (!(level.getBlockEntity(member) instanceof MonitorBlockEntity be)) continue;
            String u = be.getUrl();
            boolean playing = be.isPlaying();
            boolean paused = be.isPaused();
            boolean waiting = be.isWaiting();
            double pausedAt = be.getPausedAt();
            long start = be.getStartTick();
            int seq = be.getSeq();
            switch (msg.action) {
                case PLAY -> { u = url; playing = true; paused = false; waiting = true; pausedAt = 0.0D; seq++; }
                case STOP -> { playing = false; paused = false; waiting = false; }
                case PAUSE -> {
                    if (playing && !paused) {
                        if (!waiting) pausedAt = Math.max(0.0D, (now - start) / 20.0D);
                        paused = true;
                        waiting = false;
                    }
                }
                case RESUME -> {
                    if (playing && paused) { paused = false; waiting = true; seq++; }
                }
                case SEEK -> {
                    if (playing) {
                        pausedAt = Math.max(0.0D, be.positionSeconds(now) + delta);
                        if (!paused) waiting = true;
                        seq++;
                    }
                }
                default -> { }
            }
            be.set(u, playing && !u.isEmpty(), paused, waiting && playing, pausedAt, msg.loop, msg.volume, start, seq);
        }
    }
}
