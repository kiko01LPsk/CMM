package net.kiko.client.screen;

import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.ScreenGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keeps one video player per screen that is currently in view and shuts it down a few seconds after nobody
 * looks at it, so only what you can actually see is decoded.
 */
public final class VideoManager {

    private static final int MAX_PLAYERS = 3;
    private static final long KEEP_ALIVE_TICKS = 60L;
    private static final int PIXELS_PER_BLOCK = 320;
    private static final int MAX_WIDTH = 1280;
    private static final long MAX_PIXELS = 1280L * 720L;

    private static final Map<String, VideoPlayer> PLAYERS = new HashMap<>();

    private VideoManager() {}

    private static String key(Level level, BlockPos controller) {
        return level.dimension().location() + "@" + controller.asLong();
    }

    static VideoPlayer acquire(Level level, ScreenGroup group, MonitorBlockEntity be) {
        String key = key(level, group.controller);
        long now = level.getGameTime();
        double w = group.cols * PIXELS_PER_BLOCK;
        double h = group.rows * PIXELS_PER_BLOCK;
        double scale = Math.min(1.0D, Math.min(MAX_WIDTH / w, Math.sqrt((double) MAX_PIXELS / (w * h))));
        int texW = Math.max(64, ((int) (w * scale)) & ~1);
        int texH = Math.max(64, ((int) (h * scale)) & ~1);

        VideoPlayer player = PLAYERS.get(key);
        if (player != null && !player.matches(be.getUrl(), texW, texH)) {
            player.close();
            PLAYERS.remove(key);
            player = null;
        }
        if (player == null) {
            if (PLAYERS.size() >= MAX_PLAYERS) return null;
            player = new VideoPlayer(be.getUrl(), texW, texH, now, level::getGameTime,
                    be.isPaused(), be.isWaiting(), be.getPausedAt(), be.getStartTick(), be.getSeq(), group.controller);
            PLAYERS.put(key, player);
        }
        player.lastTouched = now;
        if (be.getBlockPos().equals(group.controller)) {
            player.loop = be.isLoop();
            player.volume = be.getVolume();
            player.sync(be, group.controller);
        }
        return player;
    }

    /** The "Sync sound" button: reload the sound of the screen this controller runs, lined up with the picture. */
    static void resyncAudio(Level level, BlockPos controller) {
        VideoPlayer p = PLAYERS.get(key(level, controller));
        if (p != null) p.resyncAudio();
    }

    /** Client tick: close players nobody has drawn for a while. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (PLAYERS.isEmpty()) return;
        long now = mc.level == null ? Long.MAX_VALUE / 2 : mc.level.getGameTime();
        Iterator<Map.Entry<String, VideoPlayer>> it = PLAYERS.entrySet().iterator();
        while (it.hasNext()) {
            VideoPlayer p = it.next().getValue();
            if (mc.level == null || now - p.lastTouched > KEEP_ALIVE_TICKS || now < p.lastTouched) {
                p.close();
                it.remove();
            }
        }
    }

    public static void closeAll() {
        for (VideoPlayer p : PLAYERS.values()) p.close();
        PLAYERS.clear();
    }
}
