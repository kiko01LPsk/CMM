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

    /** Unbound by default: opens the menu of the walkman you wear or carry. */
    public static final net.minecraft.client.KeyMapping OPEN = new net.minecraft.client.KeyMapping("key.kiko.walkman_open",
            com.mojang.blaze3d.platform.InputConstants.UNKNOWN.getValue(), net.kiko.client.PoseBindings.CATEGORY);

    /** How far (in blocks) other players can hear your walkman when its speaker is on. */
    public static final double SPEAKER_RANGE = 16.0D;
    private static final int MAX_OTHERS = 4;

    private static final class Entry {
        Session session;
        String key = "";
    }

    private static final java.util.Map<java.util.UUID, Entry> ENTRIES = new java.util.HashMap<>();

    private WalkmanPlayer() {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player me = mc.player;
        if (me == null || mc.level == null) { stop(); return; }
        while (OPEN.consumeClick()) {
            if (mc.screen == null) net.kiko.network.ModNetwork.CHANNEL.sendToServer(
                    new net.kiko.network.MjolnirActionC2SPacket(net.kiko.network.MjolnirActionC2SPacket.OPEN_WALKMAN));
        }

        float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.RECORDS);
        java.util.Set<java.util.UUID> active = new java.util.HashSet<>();

        // your own walkman: in your headphones
        update(mc, me, me, true, master, active);

        // other people's walkmans with the speaker on: the closest few
        java.util.List<Player> others = new java.util.ArrayList<>();
        for (Player p : mc.level.players()) {
            if (p == me || p.distanceTo(me) > SPEAKER_RANGE) continue;
            ItemStack w = find(p);
            if (!w.isEmpty() && WalkmanItem.isSpeaker(w)) others.add(p);
        }
        others.sort(java.util.Comparator.comparingDouble(p -> p.distanceToSqr(me)));
        for (int i = 0; i < Math.min(MAX_OTHERS, others.size()); i++) update(mc, me, others.get(i), false, master, active);

        var it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (!active.contains(e.getKey())) {
                if (e.getValue().session != null) e.getValue().session.cancel();
                it.remove();
            }
        }
    }

    /** Starts, restarts or keeps the sound of one player's walkman. */
    private static void update(Minecraft mc, Player me, Player owner, boolean own, float master, java.util.Set<java.util.UUID> active) {
        ItemStack walkman = find(owner);
        if (walkman.isEmpty() || !WalkmanItem.isPlaying(walkman)) return;
        ItemStack cassette = WalkmanItem.cassette(walkman);
        if (!(cassette.getItem() instanceof CassetteItem item) || item.tracks().isEmpty()) return;
        long pos = WalkmanItem.position(walkman, mc.level.getGameTime());
        if (pos >= item.totalTicks()) return;
        int trackIndex = item.trackAt(pos);
        CassetteItem.Track track = item.tracks().get(trackIndex);
        long inTrack = pos - item.startOf(trackIndex);

        active.add(owner.getUUID());
        Entry entry = ENTRIES.computeIfAbsent(owner.getUUID(), k -> new Entry());
        // the sound only restarts when something other than the passing of time changed
        // (a song that ends simply hands over to the next one: the key contains the song number)
        String key = trackIndex + "|" + walkman.getTag().getInt(WalkmanItem.POSITION) + "|" + walkman.getTag().getLong(WalkmanItem.SINCE)
                + "|" + track.file();
        if (entry.session == null || !key.equals(entry.key)) {
            if (entry.session != null) entry.session.cancel();
            entry.key = key;
            entry.session = new Session(track.file(), inTrack / 20.0D);
            entry.session.start();
        }
        Session session = entry.session;
        if (own) {
            session.gain = master * WalkmanVolume.get();
            session.relX = session.relY = session.relZ = 0.0F;
        } else {
            // a small speaker: quieter with distance, and coming from where the wearer is
            var camera = mc.gameRenderer.getMainCamera();
            net.minecraft.world.phys.Vec3 d = owner.getEyePosition().subtract(camera.getPosition());
            double dist = d.length();
            float falloff = (float) Math.max(0.0D, 1.0D - dist / SPEAKER_RANGE);
            session.gain = master * WalkmanVolume.get() * falloff * falloff * 0.8F;
            var left = camera.getLeftVector();
            var up = camera.getUpVector();
            var look = camera.getLookVector();
            float rx = (float) -(d.x * left.x() + d.y * left.y() + d.z * left.z());
            float ry = (float) (d.x * up.x() + d.y * up.y() + d.z * up.z());
            float rz = (float) -(d.x * look.x() + d.y * look.y() + d.z * look.z());
            float len = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
            if (len > 3.0F) { rx *= 3.0F / len; ry *= 3.0F / len; rz *= 3.0F / len; }
            session.relX = rx; session.relY = ry; session.relZ = rz;
        }
    }

    /** The walkman in a hand or worn on the chest. */
    private static ItemStack find(Player player) {
        if (player.getMainHandItem().getItem() instanceof WalkmanItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof WalkmanItem) return player.getOffhandItem();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof WalkmanItem ? chest : ItemStack.EMPTY;
    }

    public static void stop() {
        for (Entry e : ENTRIES.values()) if (e.session != null) e.session.cancel();
        ENTRIES.clear();
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
        volatile float relX, relY, relZ;
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
                    AL10.alSource3f(source, AL10.AL_POSITION, relX, relY, relZ);
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
