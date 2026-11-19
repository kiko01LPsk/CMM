package net.kiko.client.cardboard;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/** Reads the driver's WASD keys. Only ever called on the client. */
public final class CardboardBoxClient {

    private CardboardBoxClient() {}

    public static float forward(Entity driver) {
        return driver instanceof LocalPlayer player ? player.input.forwardImpulse : 0.0F;
    }

    public static float left(Entity driver) {
        return driver instanceof LocalPlayer player ? player.input.leftImpulse : 0.0F;
    }
}
