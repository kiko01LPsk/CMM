package net.kiko.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Guitar. Right-click switches the music on or off. The music is played on the clients
 * (see GuitarClient) only while the player holds a switched-on guitar in the main hand.
 */
public class GuitarItem extends Item {

    private static final String PLAYING = "Playing";

    public GuitarItem(Properties properties) {
        super(properties);
    }

    public static boolean isPlaying(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PLAYING);
    }

    public static void setPlaying(ItemStack stack, boolean value) {
        if (value) {
            stack.getOrCreateTag().putBoolean(PLAYING, true);
        } else if (stack.hasTag()) {
            stack.getTag().remove(PLAYING);
            if (stack.getTag().isEmpty()) stack.setTag(null);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            setPlaying(stack, !isPlaying(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
