package net.kiko.client.mannequin;

import net.kiko.KikoMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * The mannequin's own model layers: the same shapes as the player, but registered under our own names so that
 * mods that replace the player model (Fresh Moves and similar) do not touch the mannequin.
 */
public final class MannequinLayers {

    public static final ModelLayerLocation WIDE = layer("mannequin");
    public static final ModelLayerLocation SLIM = layer("mannequin_slim");
    public static final ModelLayerLocation INNER_ARMOR = layer("mannequin_inner_armor");
    public static final ModelLayerLocation OUTER_ARMOR = layer("mannequin_outer_armor");

    private MannequinLayers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(new ResourceLocation(KikoMod.MOD_ID, name), "main");
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WIDE, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
        event.registerLayerDefinition(SLIM, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64));
        event.registerLayerDefinition(INNER_ARMOR, () -> LayerDefinition.create(
                HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F), 64, 32));
        event.registerLayerDefinition(OUTER_ARMOR, () -> LayerDefinition.create(
                HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F), 64, 32));
    }
}
