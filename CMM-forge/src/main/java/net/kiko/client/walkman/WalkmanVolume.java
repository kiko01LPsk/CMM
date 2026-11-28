package net.kiko.client.walkman;

import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

/** The walkman volume you set with the slider (only yours, kept in the game folder). */
public final class WalkmanVolume {
    private static float value = -1.0F;

    private WalkmanVolume() {}

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("cmm_video").resolve("walkman_volume.txt");
    }

    public static float get() {
        if (value < 0.0F) {
            value = 0.8F;
            try {
                Path f = file();
                if (Files.isRegularFile(f)) value = Math.max(0.0F, Math.min(1.0F, Float.parseFloat(Files.readString(f).trim())));
            } catch (Exception ignored) {}
        }
        return value;
    }

    public static void set(float v) {
        value = Math.max(0.0F, Math.min(1.0F, v));
    }

    public static void save() {
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, Float.toString(get()));
        } catch (Exception ignored) {}
    }
}
