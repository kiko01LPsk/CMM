package net.kiko.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** A phone. Right-click opens its screen, where the hold / call / selfie animations are chosen. Can be dyed. */
public class PhoneItem extends Item implements DyeableLeatherItem {

    public static final int DEFAULT_COLOR = 0x222223;

    public PhoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor(ItemStack stack) {
        var tag = stack.getTagElement("display");
        return tag != null && tag.contains("color", 99) ? tag.getInt("color") : DEFAULT_COLOR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.kiko.client.phone.PhoneClient.open(hand));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
