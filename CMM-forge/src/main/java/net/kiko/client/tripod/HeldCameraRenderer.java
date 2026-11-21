package net.kiko.client.tripod;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * The studio camera without its tripod, drawn as a held item. It reuses the tripod camera's mesh and
 * simply leaves out everything below the camera body.
 */
public class HeldCameraRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("kiko", "textures/entity/tripod_camera.png");
    private static final ResourceLocation MODEL = new ResourceLocation("kiko", "tripod/tripod_camera_model.json");

    /** Triangles that start below this height (in model pixels) belong to the tripod. */
    private static final float BODY_MIN_Y = 17.4F;
    /** Centre of the camera body in the mesh, in pixels. */
    private static final float CX = -0.75F, CY = 20.9F, CZ = -0.8F;
    /** Where the body sits in the item cube (pixels): the same spot as the small camera. */
    private static final float HX = 8.0F, HY = 4.5F, HZ = 12.0F;
    private static final float SCALE = 0.7F;

    private List<float[]> triangles;

    public HeldCameraRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void load() {
        triangles = new ArrayList<>();
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(MODEL)) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            for (var element : root.getAsJsonArray("quads")) {
                JsonArray a = element.getAsJsonArray();
                float[] t = new float[a.size()];
                for (int i = 0; i < t.length; i++) t[i] = a.get(i).getAsFloat();
                float minY = Math.min(t[4], Math.min(t[9], t[14]));
                if (minY >= BODY_MIN_Y) triangles.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int light, int overlay) {
        if (triangles == null) load();
        poseStack.pushPose();
        poseStack.translate(HX / 16.0F, HY / 16.0F, HZ / 16.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        // the mesh looks towards -Z; the small camera looks towards +Z
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-CX / 16.0F, -CY / 16.0F, -CZ / 16.0F);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (float[] t : triangles) {
            for (int v = 0; v < 4; v++) {
                int o = 3 + Math.min(v, 2) * 5;
                consumer.vertex(pose.pose(), t[o] / 16.0F, t[o + 1] / 16.0F, t[o + 2] / 16.0F)
                        .color(255, 255, 255, 255)
                        .uv(t[o + 3], t[o + 4])
                        .overlayCoords(overlay)
                        .uv2(light)
                        .normal(pose.normal(), t[0], t[1], t[2])
                        .endVertex();
            }
        }
        poseStack.popPose();
    }
}
