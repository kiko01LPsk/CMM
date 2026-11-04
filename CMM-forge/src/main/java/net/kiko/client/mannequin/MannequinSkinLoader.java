package net.kiko.client.mannequin;

import com.mojang.blaze3d.platform.NativeImage;
import net.kiko.mannequin.MannequinSkinData;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/** Reads a skin PNG from the player's computer and turns it into the text form that is sent to the server. */
public final class MannequinSkinLoader {

    /** The message is a lang key. */
    public static final class SkinException extends Exception {
        public SkinException(String key) {
            super(key);
        }
    }

    private MannequinSkinLoader() {}

    public static String load(String pathText, int model) throws SkinException {
        String cleaned = pathText == null ? "" : pathText.trim();
        if (cleaned.length() > 1 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) throw new SkinException("screen.kiko.mannequin.skin.no_file");
        byte[] bytes;
        try {
            Path path = Path.of(cleaned);
            if (!Files.isRegularFile(path)) throw new SkinException("screen.kiko.mannequin.skin.no_file");
            if (Files.size(path) > 2_000_000L) throw new SkinException("screen.kiko.mannequin.skin.too_big");
            bytes = Files.readAllBytes(path);
        } catch (IOException | RuntimeException e) {
            throw new SkinException("screen.kiko.mannequin.skin.no_file");
        }
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes))) {
            if (!MannequinSkinData.sizeFits(image.getWidth(), image.getHeight(), model)) {
                throw new SkinException("screen.kiko.mannequin.skin.invalid_dimensions");
            }
            // Re-encoding makes the PNG as small as the game can make it.
            String text = Base64.getEncoder().encodeToString(image.asByteArray());
            if (text.length() > MannequinSkinData.MAX_LENGTH) {
                throw new SkinException("screen.kiko.mannequin.skin.too_big");
            }
            return text;
        } catch (IOException e) {
            throw new SkinException("screen.kiko.mannequin.skin.not_png");
        }
    }

    /**
     * Opens the system "open file" window (the same one the game uses elsewhere, through LWJGL).
     * Returns the chosen path, or null when cancelled or when the window is not available.
     */
    public static String browse() {
        return browse("Select a PNG texture", "*.png", "PNG images");
    }

    public static String browse(String title, String pattern, String description) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8(pattern));
            filters.flip();
            Class<?> dialogs = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            Method open = dialogs.getMethod("tinyfd_openFileDialog",
                    CharSequence.class, CharSequence.class, PointerBuffer.class, CharSequence.class, boolean.class);
            return (String) open.invoke(null, title, "", filters, description, false);
        } catch (Throwable t) {
            return null;
        }
    }
}
