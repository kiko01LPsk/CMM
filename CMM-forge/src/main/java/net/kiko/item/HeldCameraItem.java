package net.kiko.item;

import net.kiko.client.tripod.HeldCameraRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** The studio camera without its tripod. Held like the small camera (raised in the bow pose while right-click is held). */
public class HeldCameraItem extends RifleItem {

    public HeldCameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (entity.isUsingItem() && entity.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                return null;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new HeldCameraRenderer();
                return renderer;
            }
        });
    }
}
