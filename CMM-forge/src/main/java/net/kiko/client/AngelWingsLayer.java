package net.kiko.client;

import net.kiko.KikoMod;
import net.kiko.item.AngelWingsItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** The normal elytra layer, but the Angel wings item gets its own wing texture. */
public class AngelWingsLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KikoMod.MOD_ID, "textures/entity/angel_wings.png");

    public AngelWingsLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent, models);
    }

    /** The vanilla layer only draws for the real elytra, so the wings have to be allowed here. */
    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() instanceof AngelWingsItem || super.shouldRender(stack, entity);
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        if (stack.getItem() instanceof AngelWingsItem) return TEXTURE;
        return super.getElytraTexture(stack, entity);
    }
}
