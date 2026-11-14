package net.kiko.client.wheelchair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kiko.wheelchair.WheelchairEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Draws the wheelchair from its converted model data; the four wheels spin with the entity's wheel angles. */
public class WheelchairRenderer extends EntityRenderer<WheelchairEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("kiko", "textures/entity/wheelchair.png");
    private static final ResourceLocation MODEL = new ResourceLocation("kiko", "wheelchair/wheelchair_model.json");

    private static final class Part {
        final String name;
        final float[] pivot;
        final List<float[]> quads = new ArrayList<>();
        Part(String name, float[] pivot) { this.name = name; this.pivot = pivot; }
    }

    private List<Part> parts;

    public WheelchairRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    private void load() {
        parts = new ArrayList<>();
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(MODEL)) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            JsonObject pivots = root.getAsJsonObject("pivots");
            JsonObject data = root.getAsJsonObject("parts");
            for (String name : data.keySet()) {
                JsonArray p = pivots.getAsJsonArray(name);
                Part part = new Part(name, new float[]{p.get(0).getAsFloat(), p.get(1).getAsFloat(), p.get(2).getAsFloat()});
                for (var quad : data.getAsJsonArray(name)) {
                    JsonArray a = quad.getAsJsonArray();
                    float[] q = new float[a.size()];
                    for (int i = 0; i < q.length; i++) q[i] = a.get(i).getAsFloat();
                    part.quads.add(q);
                }
                parts.add(part);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(WheelchairEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        if (parts == null) load();
        poseStack.pushPose();
        // the model faces -Z, the entity's forward is +Z at yaw 0
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (Part part : parts) {
            float angle = switch (part.name) {
                case "wheel_l" -> Mth.lerp(partialTick, entity.wheelLO, entity.wheelL);
                case "wheel_r" -> Mth.lerp(partialTick, entity.wheelRO, entity.wheelR);
                case "caster_l" -> Mth.lerp(partialTick, entity.casterLO, entity.casterL);
                case "caster_r" -> Mth.lerp(partialTick, entity.casterRO, entity.casterR);
                default -> 0.0F;
            };
            poseStack.pushPose();
            if (angle != 0.0F) {
                poseStack.translate(part.pivot[0] / 16.0F, part.pivot[1] / 16.0F, part.pivot[2] / 16.0F);
                poseStack.mulPose(Axis.XP.rotation(angle));
                poseStack.translate(-part.pivot[0] / 16.0F, -part.pivot[1] / 16.0F, -part.pivot[2] / 16.0F);
            }
            PoseStack.Pose pose = poseStack.last();
            for (float[] q : part.quads) {
                for (int v = 0; v < 4; v++) {
                    int o = 3 + v * 5;
                    consumer.vertex(pose.pose(), q[o] / 16.0F, q[o + 1] / 16.0F, q[o + 2] / 16.0F)
                            .color(255, 255, 255, 255)
                            .uv(q[o + 3], q[o + 4])
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(light)
                            .normal(pose.normal(), q[0], q[1], q[2])
                            .endVertex();
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WheelchairEntity entity) {
        return TEXTURE;
    }
}
