package net.kiko.screen;

import net.kiko.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** What one monitor is showing. Every monitor of a screen holds the same values. */
public class MonitorBlockEntity extends BlockEntity {

    private String url = "";
    private boolean playing;
    private boolean paused;
    /** True from a Play / Resume / jump until the first screen has its picture ready: the clock does not run meanwhile. */
    private boolean waiting;
    /** Counts Play / Resume / jump commands so clients know when to reload their stream. */
    private int seq;
    /** Seconds into the video where it was paused (only meaningful while paused). */
    private double pausedAt;
    private boolean loop = true;
    private int volume = 50;
    /** Game time when playback (re)started: clients use it to join in at the right second. */
    private long startTick;

    // client side: remembered group (recomputed now and then)
    private ScreenGroup cachedGroup;
    private long cachedGroupTime = Long.MIN_VALUE;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MONITOR_ENTITY.get(), pos, state);
    }

    public String getUrl() { return url; }
    public boolean isPlaying() { return playing; }
    public boolean isPaused() { return paused; }
    public boolean isWaiting() { return waiting; }
    public int getSeq() { return seq; }
    public double getPausedAt() { return pausedAt; }
    public boolean isLoop() { return loop; }
    public int getVolume() { return volume; }
    public long getStartTick() { return startTick; }

    /** Where in the video we are, in seconds. */
    public double positionSeconds(long now) {
        return (paused || waiting) ? pausedAt : Math.max(0.0D, (now - startTick) / 20.0D);
    }

    public void set(String url, boolean playing, boolean paused, boolean waiting, double pausedAt, boolean loop, int volume, long startTick, int seq) {
        this.url = url;
        this.playing = playing;
        this.paused = paused;
        this.waiting = waiting;
        this.seq = seq;
        this.pausedAt = pausedAt;
        this.loop = loop;
        this.volume = Math.max(0, Math.min(100, volume));
        this.startTick = startTick;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void copyFrom(MonitorBlockEntity other) {
        set(other.url, other.playing, other.paused, other.waiting, other.pausedAt, other.loop, other.volume, other.startTick, other.seq);
    }

    /** Client: the screen this monitor belongs to (cached for a second). */
    public ScreenGroup group() {
        if (level == null) return null;
        long now = level.getGameTime();
        if (cachedGroup == null || now - cachedGroupTime > 20L || now < cachedGroupTime) {
            cachedGroup = ScreenGroup.find(level, worldPosition);
            cachedGroupTime = now;
        }
        return cachedGroup;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Url", url);
        tag.putBoolean("Playing", playing);
        tag.putBoolean("Paused", paused);
        tag.putBoolean("Waiting", waiting);
        tag.putInt("Seq", seq);
        tag.putDouble("PausedAt", pausedAt);
        tag.putBoolean("Loop", loop);
        tag.putInt("Volume", volume);
        tag.putLong("Start", startTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        url = tag.getString("Url");
        playing = tag.getBoolean("Playing");
        paused = tag.getBoolean("Paused");
        waiting = tag.getBoolean("Waiting");
        seq = tag.getInt("Seq");
        pausedAt = tag.getDouble("PausedAt");
        loop = !tag.contains("Loop") || tag.getBoolean("Loop");
        volume = tag.contains("Volume") ? tag.getInt("Volume") : 50;
        startTick = tag.getLong("Start");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }
}
