package net.kiko.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * A prop. Holding right-click raises it in the vanilla bow pose (other players see it too).
 * It never shoots and needs no ammo.
 */
public class RifleItem extends Item {

    /** Firearm stats; null = a prop that does not shoot. */
    private final GunStats stats;

    public RifleItem(Properties properties) {
        this(properties, null);
    }

    public RifleItem(Properties properties, GunStats stats) {
        super(properties);
        this.stats = stats;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE; // normal first-person view; the raised arm comes from getArmPose below
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (stats != null) GunLogic.onUse(level, player, stack, this, stats);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remaining) {
        if (stats != null && entity instanceof Player player) GunLogic.onUseTick(level, player, stack, this, stats);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        GunLogic.addTooltip(stack, stats, tooltip);
    }

    /** Third-person arm pose while holding right-click; first person stays unchanged. */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (entity.isUsingItem() && entity.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                return null;
            }
        });
    }
}
