package net.kiko.client.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kiko.KikoMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** The quadcopter from the Drons n Copters pack (texture 64x128), origin = the middle of the body. */
public class DroneModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(KikoMod.MOD_ID, "drone"), "main");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(KikoMod.MOD_ID, "textures/entity/drone.png");

    private final ModelPart root;
    private final ModelPart[] rotors = new ModelPart[4];

    public DroneModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        for (int i = 0; i < 4; i++) rotors[i] = root.getChild("rotor" + (i + 1));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.1F))
                .texOffs(16, 36).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO);

        float[][] pos = {{5, 1, -5}, {-5, 1, -5}, {5, 1, 5}, {-5, 1, 5}};
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("rotor" + (i + 1), CubeListBuilder.create()
                    .texOffs(14, 36).addBox(-0.5F, -2.25F, -0.5F, 1.0F, 1.0F, 1.0F)
                    .texOffs(6, 33).addBox(-3.5F, -1.75F, -0.5F, 7.0F, 0.0F, 1.0F)
                    .texOffs(11, 37).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.25F)),
                    PartPose.offset(pos[i][0], pos[i][1], pos[i][2]));
        }

        PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-6.5F, 0.0F, -0.5F, 13.0F, 1.0F, 1.0F)
                .texOffs(17, 37).addBox(6.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(17, 37).addBox(6.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(17, 37).addBox(-7.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(17, 37).addBox(-0.5F, 0.0F, -7.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(17, 37).addBox(-0.5F, 0.0F, 6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
                .texOffs(17, 37).addBox(-7.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(17, 37).addBox(-0.5F, 0.0F, 6.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(17, 37).addBox(-0.5F, 0.0F, -7.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(45.0), 0.0F));
        legs.addOrReplaceChild("cross", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-6.5F, 0.0F, -0.5F, 13.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, (float) Math.toRadians(90.0), 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    /** Spins the four rotors. */
    public void spin(float ageInTicks) {
        for (int i = 0; i < 4; i++) {
            rotors[i].yRot = ageInTicks * 2.2F * (i == 0 || i == 3 ? 1.0F : -1.0F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(poseStack, buffer, light, overlay, r, g, b, a);
    }
}
