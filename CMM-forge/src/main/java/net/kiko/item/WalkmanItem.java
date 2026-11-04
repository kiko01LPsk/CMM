package net.kiko.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Walkman. Wearable in the chestplate slot (it is drawn on the belt, see WalkmanLayer).
 * Right-click toggles the music; sneak + right-click puts it on like any other armor.
 * The music itself is played on the clients (see WalkmanClient) while the stack says "Playing".
 */
public class WalkmanItem extends ArmorItem {

    private static final String PLAYING = "Playing";

    public WalkmanItem(Properties properties) {
        super(ModArmorMaterials.WALKMAN, Type.CHESTPLATE, properties);
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
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand); // put it on
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            setPlaying(stack, !isPlaying(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** It is a gadget, not armor: it does not wear out. */
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
