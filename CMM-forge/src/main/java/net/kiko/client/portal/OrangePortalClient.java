package net.kiko.client.portal;

import net.kiko.KikoMod;
import net.kiko.block.ModBlocks;
import net.kiko.block.OrangePortalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Someone standing inside an orange portal is hidden from people looking through the portal from the other side,
 * as if they had stepped into it.
 */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OrangePortalClient {
    private OrangePortalClient() {}

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        BlockPos feet = entity.blockPosition();
        BlockPos head = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        BlockPos found = null;
        BlockState state = null;
        for (BlockPos p : new BlockPos[]{feet, head}) {
            BlockState s = mc.level.getBlockState(p);
            if (s.is(ModBlocks.ORANGE_PORTAL.get()) || s.is(ModBlocks.BLUE_PORTAL.get())) { found = p; state = s; break; }
        }
        if (found == null) return;
        boolean alongX = state.getValue(OrangePortalBlock.FACING).getAxis() == Direction.Axis.X;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        double entityOffset = alongX ? entity.getX() - (found.getX() + 0.5) : entity.getZ() - (found.getZ() + 0.5);
        double camOffset = alongX ? cam.x - (found.getX() + 0.5) : cam.z - (found.getZ() + 0.5);
        // different sides of the pane: the viewer would be looking through the portal at the person
        if (entityOffset * camOffset < 0 && Math.abs(camOffset) > 0.05) {
            event.setCanceled(true);
        }
    }
}
