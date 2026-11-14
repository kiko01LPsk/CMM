package net.kiko.network;

import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.ScreenGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: "my picture for this screen is ready", which starts the screen's clock. */
public class MonitorReadyC2SPacket {

    private final BlockPos pos;

    public MonitorReadyC2SPacket(BlockPos pos) { this.pos = pos; }

    public MonitorReadyC2SPacket(FriendlyByteBuf buf) { this(buf.readBlockPos()); }

    public void toBytes(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }

    public static void handle(MonitorReadyC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) return;
        Level level = sender.level();
        if (sender.distanceToSqr(msg.pos.getX() + 0.5D, msg.pos.getY() + 0.5D, msg.pos.getZ() + 0.5D) > 128.0D * 128.0D) return;
        if (!level.isLoaded(msg.pos)) return;
        ScreenGroup group = ScreenGroup.find(level, msg.pos);
        if (group == null) return;
        long now = level.getGameTime();
        for (BlockPos member : group.members) {
            if (level.getBlockEntity(member) instanceof MonitorBlockEntity be && be.isWaiting() && be.isPlaying()) {
                be.set(be.getUrl(), true, false, false, be.getPausedAt(), be.isLoop(), be.getVolume(),
                        now - (long) (be.getPausedAt() * 20.0D), be.getSeq());
            }
        }
    }
}
