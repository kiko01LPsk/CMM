package net.kiko.client.mannequin;

import net.kiko.mannequin.Mannequin;
import net.minecraft.client.Minecraft;

/** Client-only entry point, called from Mannequin through DistExecutor. */
public final class MannequinClient {

    private MannequinClient() {}

    public static void open(Mannequin mannequin) {
        Minecraft.getInstance().setScreen(new MannequinScreen(mannequin));
    }
}
