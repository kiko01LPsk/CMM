package net.kiko.client.screen;

import net.kiko.KikoMod;
import net.kiko.screen.video.AudioStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

/** Feeds decoded sound to one OpenAL source. Volume and direction are set by the renderer every frame. */
final class VideoAudio extends Thread {

    private static final int BUFFERS = 6;
    private static final int CHUNK = AudioStream.RATE * 4 / 20; // 50 ms of 16-bit stereo

    private final InputStream in;
    private volatile boolean stop;
    /** Sound stays silent until the picture is there, so you never hear a video you cannot see. */
    volatile boolean go;
    volatile float gain;
    volatile float relX, relY, relZ;

    VideoAudio(InputStream in) {
        super("CMM video sound");
        setDaemon(true);
        this.in = in;
    }

    void shutdown() { stop = true; }

    @Override
    public void run() {
        int source = 0;
        int[] buffers = new int[BUFFERS];
        boolean ready = false;
        try {
            source = AL10.alGenSources();
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 0.0F);
            ArrayDeque<Integer> free = new ArrayDeque<>();
            for (int i = 0; i < BUFFERS; i++) {
                buffers[i] = AL10.alGenBuffers();
                free.add(buffers[i]);
            }
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
                if (go && state != AL10.AL_PLAYING && queued > 0) AL10.alSourcePlay(source);
                if (eof && queued == 0) break;
                try { Thread.sleep(10L); } catch (InterruptedException e) { break; }
            }
        } catch (IOException | RuntimeException e) {
            if (!stop) KikoMod.LOGGER.warn("Monitor sound stopped: {}", e.toString());
        } finally {
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
