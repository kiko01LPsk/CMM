package net.kiko.client.tripod;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kiko.tripod.TripodCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Draws the tripod camera from its converted model data (triangles: normal, then 3 x position + uv). */
public class TripodCameraRenderer extends EntityRenderer<TripodCameraEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("kiko", "textures/entity/tripod_camera.png");
    private static final ResourceLocation MODEL = new ResourceLocation("kiko", "tripod/tripod_camera_model.json");

    private List<float[]> triangles;

    public TripodCameraRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4F;
    }

    private void load() {
        triangles = new ArrayList<>();
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(MODEL)) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            for (var element : root.getAsJsonArray("quads")) {
                JsonArray a = element.getAsJsonArray();
                float[] t = new float[a.size()];
                for (int i = 0; i < t.length; i++) t[i] = a.get(i).getAsFloat();
                triangles.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(TripodCameraEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        if (triangles == null) load();
        poseStack.pushPose();
        // the model faces -Z, the entity's forward is +Z at yaw 0
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (float[] t : triangles) {
            for (int v = 0; v < 4; v++) {
                int o = 3 + Math.min(v, 2) * 5;
                consumer.vertex(pose.pose(), t[o] / 16.0F, t[o + 1] / 16.0F, t[o + 2] / 16.0F)
                        .color(255, 255, 255, 255)
                        .uv(t[o + 3], t[o + 4])
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(light)
                        .normal(pose.normal(), t[0], t[1], t[2])
                        .endVertex();
            }
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TripodCameraEntity entity) {
        return TEXTURE;
    }
}
