package net.kiko.client.cardboard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kiko.cardboard.CardboardBoxEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the open cardboard box: floor, four walls and four flaps folded outwards. */
public class CardboardBoxRenderer extends EntityRenderer<CardboardBoxEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("kiko", "textures/entity/cardboard_box.png");

    private final ModelPart root;

    public CardboardBoxRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.6F;
        this.root = createLayer().bakeRoot();
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // model space: y points down, y = 24 is the ground
        root.addOrReplaceChild("floor", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 23.0F, -8.0F, 16.0F, 1.0F, 16.0F), PartPose.ZERO);
        root.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 13.0F, -8.0F, 16.0F, 10.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 13.0F, 7.0F, 16.0F, 10.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, 13.0F, -7.0F, 1.0F, 10.0F, 14.0F), PartPose.ZERO);
        root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 13.0F, -7.0F, 1.0F, 10.0F, 14.0F), PartPose.ZERO);
        // flaps, hinged on the top edge of each wall
        root.addOrReplaceChild("flap_front", CubeListBuilder.create().texOffs(0, 17).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 13.0F, -8.0F, -0.6F, 0.0F, 0.0F));
        root.addOrReplaceChild("flap_back", CubeListBuilder.create().texOffs(0, 17).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 13.0F, 8.0F, 0.6F, 0.0F, 0.0F));
        root.addOrReplaceChild("flap_left", CubeListBuilder.create().texOffs(0, 17).addBox(0.0F, -1.0F, -8.0F, 7.0F, 1.0F, 16.0F), PartPose.offsetAndRotation(8.0F, 13.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        root.addOrReplaceChild("flap_right", CubeListBuilder.create().texOffs(0, 17).addBox(-7.0F, -1.0F, -8.0F, 7.0F, 1.0F, 16.0F), PartPose.offsetAndRotation(-8.0F, 13.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(CardboardBoxEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        root.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CardboardBoxEntity entity) {
        return TEXTURE;
    }
}
