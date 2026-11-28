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
    /** Pause: the sound is held and carries on from the same spot. */
    volatile boolean suspended;
    /** Seconds of the video where this sound stream starts (0 unless ffmpeg was told to jump in). */
    volatile double baseSeconds;
    /** Sound is read and thrown away until this many bytes of the stream have gone by (a jump ahead). */
    volatile long targetBytes;
    private long bytesRead;
    private boolean loggedPlay;
    volatile boolean flush;
    /** True once the first sound is queued and ready to play (used when the sound is reloaded to line up with the picture). */
    volatile boolean primed;
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
                if (flush) {
                    flush = false;
                    // start over with a brand new source: the surest way to drop everything that was queued
                    AL10.alSourceStop(source);
                    AL10.alDeleteSources(source);
                    source = AL10.alGenSources();
                    AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
                    AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 0.0F);
                    free.clear();
                    for (int b : buffers) free.add(b);
                    KikoMod.LOGGER.info("Monitor sound: jumped, waiting for the picture (target byte {})", targetBytes);
                }
                if (bytesRead < targetBytes && !eof) {
                    int n = in.readNBytes(chunk, 0, (int) Math.min(CHUNK, targetBytes - bytesRead));
                    if (n <= 0) eof = true; else bytesRead += n;
                    continue;
                }
                if (suspended) {
                    if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) AL10.alSourcePause(source);
                    try { Thread.sleep(20L); } catch (InterruptedException e) { break; }
                    continue;
                }
                int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                while (processed-- > 0) free.add(AL10.alSourceUnqueueBuffers(source));

                while (!free.isEmpty() && !eof && !stop) {
                    int n = in.readNBytes(chunk, 0, CHUNK);
                    bytesRead += n;
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
                if (queued > 0) primed = true;
                if (go && state != AL10.AL_PLAYING && queued > 0) { AL10.alSourcePlay(source); if (!loggedPlay) { loggedPlay = true; KikoMod.LOGGER.info("Monitor sound: playing"); } }
                if (!go) loggedPlay = false;
                if (!go && state == AL10.AL_PLAYING) AL10.alSourcePause(source);
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
