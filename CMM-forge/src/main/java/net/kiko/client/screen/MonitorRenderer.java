package net.kiko.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kiko.screen.MonitorBlock;
import net.kiko.screen.MonitorBlockEntity;
import net.kiko.screen.ScreenGroup;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Draws this monitor's piece of the screen's video on top of its black front face. */
public class MonitorRenderer implements BlockEntityRenderer<MonitorBlockEntity> {

    /** 1/16 block: width of the frame that is drawn where no other monitor is attached. */
    private static final float BEZEL = 1.0F;
    /** Front face of the panel, in blocks from the block centre (model: z = 13/16 when facing north). */
    private static final float FACE_Z = 13.0F / 16.0F - 0.5F;
    private static final float MAX_SOUND_DISTANCE = 40.0F;

    public MonitorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(MonitorBlockEntity be) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(MonitorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay) {
        if (!be.isPlaying() || be.getUrl().isEmpty() || be.getLevel() == null) return;
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof MonitorBlock)) return;
        ScreenGroup group = be.group();
        if (group == null) return;

        VideoPlayer player = VideoManager.acquire(be.getLevel(), group, be);
        if (player == null) return;
        BlockPos pos = be.getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        boolean controller = pos.equals(group.controller);
        if (controller) {
            player.poll();
            updateSound(player, group, mc);
        }

        Direction facing = state.getValue(MonitorBlock.FACING);
        int c = group.columnOf(pos);
        int r = group.rowOf(pos);
        float left = state.getValue(MonitorBlock.LEFT) ? 0.0F : BEZEL;
        float right = state.getValue(MonitorBlock.RIGHT) ? 0.0F : BEZEL;
        float top = state.getValue(MonitorBlock.TOP) ? 0.0F : BEZEL;
        float bottom = state.getValue(MonitorBlock.BOTTOM) ? 0.0F : BEZEL;
        // pixels (16 per block) inside this block, seen from the front
        float xa = left, xb = 16.0F - right, ya = top, yb = 16.0F - bottom;
        float totalW = group.cols * 16.0F - 2.0F * BEZEL;
        float totalH = group.rows * 16.0F - 2.0F * BEZEL;
        float u0 = Mth.clamp((c * 16.0F + xa - BEZEL) / totalW, 0.0F, 1.0F);
        float u1 = Mth.clamp((c * 16.0F + xb - BEZEL) / totalW, 0.0F, 1.0F);
        float v0 = Mth.clamp((r * 16.0F + ya - BEZEL) / totalH, 0.0F, 1.0F);
        float v1 = Mth.clamp((r * 16.0F + yb - BEZEL) / totalH, 0.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        Matrix4f m = poseStack.last().pose();
        // facing north: the viewer's left is +x
        float xl = 0.5F - xa / 16.0F, xr = 0.5F - xb / 16.0F;
        float yt = 0.5F - ya / 16.0F, yb2 = 0.5F - yb / 16.0F;
        float z = FACE_Z - 0.002F;
        int full = LightTexture.FULL_BRIGHT;
        VertexConsumer vc = buffer.getBuffer(RenderType.text(player.location));
        vc.vertex(m, xl, yt, z).color(255, 255, 255, 255).uv(u0, v0).uv2(full).endVertex();
        vc.vertex(m, xl, yb2, z).color(255, 255, 255, 255).uv(u0, v1).uv2(full).endVertex();
        vc.vertex(m, xr, yb2, z).color(255, 255, 255, 255).uv(u1, v1).uv2(full).endVertex();
        vc.vertex(m, xr, yt, z).color(255, 255, 255, 255).uv(u1, v0).uv2(full).endVertex();
        poseStack.popPose();
    }

    /** Sound comes from the middle of the screen: quieter with distance, panned by where it is relative to the camera. */
    private static void updateSound(VideoPlayer player, ScreenGroup group, Minecraft mc) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();
        Direction facing = group.facing;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (BlockPos p : group.members) {
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX() + 1);
            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY() + 1);
            minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ() + 1);
        }
        double cx = (minX + maxX) / 2 + facing.getStepX() * 0.5;
        double cy = (minY + maxY) / 2;
        double cz = (minZ + maxZ) / 2 + facing.getStepZ() * 0.5;
        double dx = cx - cam.x, dy = cy - cam.y, dz = cz - cam.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        player.distanceToPlayer = dist;

        float ref = 3.0F + Math.max(group.cols, group.rows) * 1.5F;
        float falloff = dist <= ref ? 1.0F : Mth.clamp(1.0F - (float) (dist - ref) / (MAX_SOUND_DISTANCE - ref), 0.0F, 1.0F);
        float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.RECORDS);
        float gain = player.volume / 100.0F * master * falloff;

        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        float rx = (float) -(dx * left.x + dy * left.y + dz * left.z);
        float ry = (float) (dx * up.x + dy * up.y + dz * up.z);
        float rz = (float) -(dx * look.x + dy * look.y + dz * look.z);
        // keep the direction but bring a faraway source close, since the loudness is already handled by 'gain'
        float len = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (len > 4.0F) { rx *= 4.0F / len; ry *= 4.0F / len; rz *= 4.0F / len; }
        player.setSound(gain, rx, ry, rz);
    }
}
