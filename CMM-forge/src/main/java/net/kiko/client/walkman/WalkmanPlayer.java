package net.kiko.client.walkman;

import net.kiko.KikoMod;
import net.kiko.item.CassetteItem;
import net.kiko.item.WalkmanItem;
import net.kiko.screen.video.AudioStream;
import net.kiko.screen.video.VideoTools;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;

/**
 * Plays the cassette of the walkman you hold or wear, only for you (it is in your headphones).
 * The walkman item keeps "Playing", "Position", "Since" and "Track"; whenever one of them changes the sound restarts from the right spot.
 */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, value = Dist.CLIENT)
public final class WalkmanPlayer {

    private static Session session;
    private static String sessionKey = "";

    private WalkmanPlayer() {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) { stop(); return; }

        ItemStack walkman = find(player);
        if (walkman.isEmpty() || !WalkmanItem.isPlaying(walkman)) { stop(); return; }
        ItemStack cassette = WalkmanItem.cassette(walkman);
        if (!(cassette.getItem() instanceof CassetteItem item) || item.tracks().isEmpty()) { stop(); return; }

        long pos = WalkmanItem.position(walkman, mc.level.getGameTime());
        if (pos >= item.totalTicks()) { stop(); return; }
        int trackIndex = item.trackAt(pos);
        CassetteItem.Track track = item.tracks().get(trackIndex);
        long inTrack = pos - item.startOf(trackIndex);

        // the sound only restarts when something other than the passing of time changed
        // (a song that ends simply hands over to the next one: the key contains the song number)
        String key = trackIndex + "|" + walkman.getTag().getInt(WalkmanItem.POSITION) + "|" + walkman.getTag().getLong(WalkmanItem.SINCE)
                + "|" + track.file();
        if (session == null || !key.equals(sessionKey)) {
            stop();
            sessionKey = key;
            session = new Session(track.file(), inTrack / 20.0D);
            session.start();
        }
        float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.RECORDS);
        session.gain = master;
    }

    /** The walkman in a hand or worn on the chest. */
    private static ItemStack find(Player player) {
        if (player.getMainHandItem().getItem() instanceof WalkmanItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof WalkmanItem) return player.getOffhandItem();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof WalkmanItem ? chest : ItemStack.EMPTY;
    }

    public static void stop() {
        if (session != null) session.cancel();
        session = null;
        sessionKey = "";
    }

    /** Copies the song out of the mod once so ffmpeg can read it as a normal file. */
    private static Path extract(String file) throws IOException {
        Path target = Minecraft.getInstance().gameDirectory.toPath().resolve("cmm_video").resolve(file);
        ResourceLocation id = new ResourceLocation(KikoMod.MOD_ID, file);
        var resource = Minecraft.getInstance().getResourceManager().getResource(id)
                .orElseThrow(() -> new IOException("missing sound file " + id));
        Files.createDirectories(target.getParent());
        try (InputStream in = resource.open()) {
            byte[] data = in.readAllBytes();
            if (!Files.isRegularFile(target) || Files.size(target) != data.length) Files.write(target, data);
        }
        return target;
    }

    private static final class Session extends Thread {
        private static final int BUFFERS = 6;
        private static final int CHUNK = AudioStream.RATE * 4 / 20;

        private final String file;
        private final double start;
        private volatile boolean stop;
        volatile float gain = 1.0F;
        private volatile AudioStream stream;

        Session(String file, double start) {
            super("CMM walkman sound");
            setDaemon(true);
            this.file = file;
            this.start = start;
        }

        void cancel() {
            stop = true;
            AudioStream s = stream;
            if (s != null) s.close();
        }

        @Override
        public void run() {
            int source = 0;
            int[] buffers = new int[BUFFERS];
            boolean ready = false;
            try {
                String ffmpeg = VideoTools.ffmpeg(msg -> {});
                Path path = extract(file);
                if (stop) return;
                AudioStream audio = new AudioStream(ffmpeg, path.toString(), start, true);
                stream = audio;
                if (stop) { audio.close(); return; }
                InputStream in = audio.pcm();

                source = AL10.alGenSources();
                AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
                AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 0.0F);
                ArrayDeque<Integer> free = new ArrayDeque<>();
                for (int i = 0; i < BUFFERS; i++) { buffers[i] = AL10.alGenBuffers(); free.add(buffers[i]); }
                ready = true;
                byte[] chunk = new byte[CHUNK];
                ByteBuffer direct = BufferUtils.createByteBuffer(CHUNK);
                boolean eof = false;
                while (!stop) {
                    int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                    while (processed-- > 0) free.add(AL10.alSourceUnqueueBuffers(source));
                    while (!free.isEmpty() && !eof && !stop) {
                        int n = in.readNBytes(chunk, 0, CHUNK);
                        n -= n % 4;
                        if (n <= 0) { eof = true; break; }
                        direct.clear();
                        direct.put(chunk, 0, n).flip();
                        int id = free.poll();
                        AL10.alBufferData(id, AL10.AL_FORMAT_STEREO16, direct, AudioStream.RATE);
                        AL10.alSourceQueueBuffers(source, id);
                    }
                    AL10.alSourcef(source, AL10.AL_GAIN, gain);
                    int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
                    int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
                    if (state != AL10.AL_PLAYING && queued > 0) AL10.alSourcePlay(source);
                    if (eof && queued == 0) break;
                    try { Thread.sleep(10L); } catch (InterruptedException e) { break; }
                }
            } catch (IOException | RuntimeException e) {
                if (!stop) KikoMod.LOGGER.warn("Walkman sound stopped: {}", e.toString());
            } finally {
                AudioStream s = stream;
                if (s != null) s.close();
                try {
                    if (ready) {
                        AL10.alSourceStop(source);
                        AL10.alSourcei(source, AL10.AL_BUFFER, 0);
                        AL10.alDeleteSources(source);
                        for (int b : buffers) if (b != 0) AL10.alDeleteBuffers(b);
                    }
                } catch (RuntimeException ignored) {}
            }
        }
    }
}
