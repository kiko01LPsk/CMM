package net.kiko.mannequin;

import java.util.Base64;

/** Checks custom skins (base64 PNG text) on the server and on the client. */
public final class MannequinSkinData {

    /** The skin is stored in an entity data string, which can hold about 32000 characters. */
    public static final int MAX_LENGTH = 30000;

    private MannequinSkinData() {}

    /** Player-shaped skin check (used by the imitation mode). */
    public static boolean isValid(String base64) {
        return isValid(base64, Mannequin.MODEL_PLAYER);
    }

    /** Whether a texture of this size fits the model: players 64x64 or 64x32, golem 128x128, villager 64x64, creeper 64x32. */
    public static boolean sizeFits(int width, int height, int model) {
        return switch (model) {
            case Mannequin.MODEL_GOLEM -> width == 128 && height == 128;
            case Mannequin.MODEL_VILLAGER, Mannequin.MODEL_ZOMBIE, Mannequin.MODEL_HUSK, Mannequin.MODEL_DROWNED,
                 Mannequin.MODEL_PILLAGER, Mannequin.MODEL_VINDICATOR, Mannequin.MODEL_PIGLIN -> width == 64 && height == 64;
            case Mannequin.MODEL_CREEPER, Mannequin.MODEL_SKELETON, Mannequin.MODEL_WITHER_SKELETON,
                 Mannequin.MODEL_ENDERMAN, Mannequin.MODEL_PIG, Mannequin.MODEL_COW -> width == 64 && height == 32;
            case Mannequin.MODEL_WITCH -> width == 64 && height == 128;
            case Mannequin.MODEL_CUSTOM -> width >= 16 && height >= 16 && width <= 256 && height <= 256;
            default -> width == 64 && (height == 64 || height == 32);
        };
    }

    /** "" (no model) is valid; otherwise valid base64 text that is not too long. */
    public static boolean isValidModel(String data) {
        if (data == null) return false;
        if (data.isEmpty()) return true;
        if (data.length() > MAX_LENGTH) return false;
        try {
            return Base64.getDecoder().decode(data).length > 10;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** "" (no skin) is valid. Otherwise it must be a PNG whose size fits the model and that is not too big. */
    public static boolean isValid(String base64, int model) {
        if (base64 == null) return false;
        if (base64.isEmpty()) return true;
        if (base64.length() > MAX_LENGTH) return false;
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            if (data.length < 33) return false;
            // PNG signature, then the IHDR chunk with width and height.
            int[] signature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            for (int i = 0; i < signature.length; i++) {
                if ((data[i] & 0xFF) != signature[i]) return false;
            }
            int width = readInt(data, 16);
            int height = readInt(data, 20);
            return sizeFits(width, height, model);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int readInt(byte[] d, int i) {
        return ((d[i] & 0xFF) << 24) | ((d[i + 1] & 0xFF) << 16) | ((d[i + 2] & 0xFF) << 8) | (d[i + 3] & 0xFF);
    }
}
