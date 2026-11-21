package net.kiko.client.deer;

import net.kiko.KikoMod;
import net.kiko.deer.DeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DeerRenderer extends MobRenderer<DeerEntity, DeerModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(KikoMod.MOD_ID, "textures/entity/deer/deer.png");

    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerModel(context.bakeLayer(DeerModel.LAYER)), 0.75F);
    }

    @Override
    public ResourceLocation getTextureLocation(DeerEntity entity) {
        return TEXTURE;
    }
}
