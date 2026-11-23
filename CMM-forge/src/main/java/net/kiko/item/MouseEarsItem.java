package net.kiko.item;

import net.kiko.client.hat.MouseEarsModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** A hat with two round black ears on top, worn on the head. */
public class MouseEarsItem extends ArmorItem {
    private static final String TEXTURE = "kiko:textures/models/armor/mouse_ears.png";

    public MouseEarsItem(Properties properties) {
        super(ModArmorMaterials.MOUSE_EARS, Type.HELMET, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return TEXTURE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HumanoidModel<?> model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (model == null) {
                    model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(MouseEarsModel.LAYER));
                }
                return model;
            }
        });
    }
}
