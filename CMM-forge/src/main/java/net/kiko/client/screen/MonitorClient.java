package net.kiko.client.screen;

import net.kiko.screen.MonitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** Client-only entry point, called from MonitorBlock through DistExecutor. */
public final class MonitorClient {

    private MonitorClient() {}

    public static void open(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getBlockEntity(pos) instanceof MonitorBlockEntity be) {
            mc.setScreen(new MonitorScreen(pos, be));
        }
    }
}
