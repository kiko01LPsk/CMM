package net.kiko.item;

import net.kiko.KikoMod;
import net.kiko.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** White mode: a player holding the sonic screwdriver lights up the air around their head. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SonicLightEvents {

    private SonicLightEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 4 != 0) return;
        if (!SonicScrewdriverItem.isGlowing(player.getMainHandItem())
                && !SonicScrewdriverItem.isGlowing(player.getOffhandItem())) return;

        BlockPos pos = BlockPos.containing(player.getEyePosition());
        if (player.serverLevel().getBlockState(pos).isAir()) {
            player.serverLevel().setBlock(pos, ModBlocks.SONIC_LIGHT.get().defaultBlockState(), 3);
        }
    }
}
