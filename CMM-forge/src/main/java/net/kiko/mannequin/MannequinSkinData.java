package net.kiko.mannequin;

import java.util.Base64;

/** Checks custom skins (base64 PNG text) on the server and on the client. */
public final class MannequinSkinData {

    /** The skin is stored in an entity data string, which can hold about 32000 characters. */
    public static final int MAX_LENGTH = 30000;

    private MannequinSkinData() {}

    /** "" (no skin) is valid. Otherwise it must be a PNG of 64x64 or 64x32 pixels that is not too big. */
    public static boolean isValid(String base64) {
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
            return width == 64 && (height == 64 || height == 32);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int readInt(byte[] d, int i) {
        return ((d[i] & 0xFF) << 24) | ((d[i + 1] & 0xFF) << 16) | ((d[i + 2] & 0xFF) << 8) | (d[i + 3] & 0xFF);
    }
}
