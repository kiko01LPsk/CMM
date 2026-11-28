package net.kiko.client.morph;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Client side: custom skin textures of morphed players. */
public final class ClientMorphSkins {
    private static final Map<UUID, DynamicTexture> SKINS = new HashMap<>();

    private ClientMorphSkins() {}

    public static void set(UUID player, byte[] png) {
        DynamicTexture old = SKINS.remove(player);
        if (old != null) old.close();
        if (png == null || png.length == 0) return;
        try {
            SKINS.put(player, new DynamicTexture(NativeImage.read(new ByteArrayInputStream(png))));
        } catch (Exception ignored) {
            // not a readable PNG: keep the normal skin
        }
    }

    public static DynamicTexture get(UUID player) {
        return SKINS.get(player);
    }

    public static void clear() {
        SKINS.values().forEach(DynamicTexture::close);
        SKINS.clear();
    }
}
