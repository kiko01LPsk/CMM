package net.kiko.client.hat;

import net.kiko.KikoMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Two round ears standing on top of the head. Only the head part has any shape; the rest of the body is empty. */
public final class MouseEarsModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(KikoMod.MOD_ID, "mouse_ears"), "main");

    private MouseEarsModel() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder ears = CubeListBuilder.create().texOffs(0, 0);
        for (float cx : new float[]{-3.0F, 3.0F}) {
            // a round plate made of two crossing boxes, 5 pixels across and 1 thick, standing on the top of the head
            ears.addBox(cx - 2.5F, -12.0F, -0.5F, 5.0F, 3.0F, 1.0F);
            ears.addBox(cx - 1.5F, -13.0F, -0.5F, 3.0F, 5.0F, 1.0F);
        }
        root.addOrReplaceChild("head", ears, PartPose.ZERO);
        for (String name : new String[]{"hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }
}
