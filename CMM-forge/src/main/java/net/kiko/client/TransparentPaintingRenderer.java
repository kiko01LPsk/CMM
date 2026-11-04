package net.kiko.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.world.entity.decoration.Painting;

/**
 * The normal painting renderer, except that see-through pixels of a painting texture stay see-through
 * (the game draws paintings as solid, which turns transparent pixels black).
 */
public class TransparentPaintingRenderer extends PaintingRenderer {

    public TransparentPaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Painting painting, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        var atlas = getTextureLocation(painting);
        RenderType solid = RenderType.entitySolid(atlas);
        RenderType cutout = RenderType.entityCutout(atlas);
        MultiBufferSource wrapped = new MultiBufferSource() {
            @Override
            public VertexConsumer getBuffer(RenderType type) {
                return buffer.getBuffer(type.equals(solid) ? cutout : type);
            }
        };
        super.render(painting, yaw, partialTick, poseStack, wrapped, packedLight);
    }
}
