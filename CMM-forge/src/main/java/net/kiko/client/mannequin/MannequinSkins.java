package net.kiko.client.mannequin;

import com.mojang.blaze3d.platform.NativeImage;
import net.kiko.KikoMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Turns custom skins (base64 PNG text) into textures, once per distinct skin. Render thread only. */
public final class MannequinSkins {

    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    private MannequinSkins() {}

    /** The texture for this skin, or the fallback when there is no (valid) skin. */
    public static ResourceLocation get(String base64, ResourceLocation fallback) {
        return get(base64, fallback, true);
    }

    /** playerLayout: true for the player model (old 64x32 skins are converted, the body is made opaque); false for mob textures. */
    public static ResourceLocation get(String base64, ResourceLocation fallback, boolean playerLayout) {
        if (base64 == null || base64.isEmpty()) return fallback;
        String key = (playerLayout ? "p" : "m") + base64;
        ResourceLocation cached = CACHE.get(key);
        if (cached == null) {
            cached = load(base64, playerLayout);
            CACHE.put(key, cached != null ? cached : fallback);
            if (cached == null) cached = fallback;
        }
        return cached;
    }

    private static ResourceLocation load(String base64, boolean playerLayout) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            if (image.getWidth() < 16 || image.getHeight() < 16 || image.getWidth() > 256 || image.getHeight() > 256) {
                image.close();
                return null;
            }
            NativeImage skin = image;
            if (playerLayout) {
                if (image.getWidth() != 64 || (image.getHeight() != 64 && image.getHeight() != 32)) {
                    image.close();
                    return null;
                }
                skin = image.getHeight() == 32 ? convertLegacy(image) : image;
                if (skin != image) image.close();
                opaque(skin, 0, 0, 32, 16);
                opaque(skin, 0, 16, 64, 32);
                opaque(skin, 16, 48, 48, 64);
            }
            ResourceLocation id = new ResourceLocation(KikoMod.MOD_ID,
                    "mannequin_skin/" + (playerLayout ? "p" : "m") + UUID.nameUUIDFromBytes(data));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(skin));
            return id;
        } catch (Exception e) {
            KikoMod.LOGGER.warn("Could not load a mannequin skin", e);
            return null;
        }
    }

    /** Old 64x32 skins have no left arm and leg: mirror the right ones, like the game does for players. */
    private static NativeImage convertLegacy(NativeImage old) {
        NativeImage out = new NativeImage(64, 64, true);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                out.setPixelRGBA(x, y, old.getPixelRGBA(x, y));
            }
        }
        mirror(out, 4, 16, 16, 32, 4, 4);
        mirror(out, 8, 16, 16, 32, 4, 4);
        mirror(out, 0, 20, 24, 32, 4, 12);
        mirror(out, 4, 20, 16, 32, 4, 12);
        mirror(out, 8, 20, 8, 32, 4, 12);
        mirror(out, 12, 20, 16, 32, 4, 12);
        mirror(out, 44, 16, -8, 32, 4, 4);
        mirror(out, 48, 16, -8, 32, 4, 4);
        mirror(out, 40, 20, 0, 32, 4, 12);
        mirror(out, 44, 20, -8, 32, 4, 12);
        mirror(out, 48, 20, -16, 32, 4, 12);
        mirror(out, 52, 20, -8, 32, 4, 12);
        return out;
    }

    /** Copies a w x h rectangle at (x, y) to (x + dx, y + dy), flipped left to right. */
    private static void mirror(NativeImage image, int x, int y, int dx, int dy, int w, int h) {
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                image.setPixelRGBA(x + dx + (w - 1 - i), y + dy + j, image.getPixelRGBA(x + i, y + j));
            }
        }
    }

    private static void opaque(NativeImage image, int x1, int y1, int x2, int y2) {
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                image.setPixelRGBA(x, y, image.getPixelRGBA(x, y) | 0xFF000000);
            }
        }
    }
}
