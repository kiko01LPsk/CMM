package net.kiko.item;

import net.kiko.walkman.WalkmanMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Walkman. Wearable in the chestplate slot (it is drawn on the belt, see WalkmanLayer).
 * Right-click opens its menu: one cassette slot and the play / stop / forward / skip buttons.
 * Sneak + right-click puts it on like any other armor.
 * State kept on the stack: "Playing" (boolean), "Position" (ticks into the cassette), "Track" (song number), "Items" (the cassette).
 */
public class WalkmanItem extends ArmorItem {

    public static final String PLAYING = "Playing";
    public static final String POSITION = "Position";
    public static final String TRACK = "Track";
    /** Game time when it last started playing; with "Position" it tells where the song is right now. */
    public static final String SINCE = "Since";

    /** Where the song is now, in ticks. */
    public static long position(ItemStack stack, long gameTime) {
        if (!stack.hasTag()) return 0L;
        long pos = stack.getTag().getInt(POSITION);
        if (stack.getTag().getBoolean(PLAYING)) pos += Math.max(0L, gameTime - stack.getTag().getLong(SINCE));
        return pos;
    }

    /** The cassette that sits inside, or EMPTY. */
    public static ItemStack cassette(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("Items")) return ItemStack.EMPTY;
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(stack.getTag(), items);
        return items.get(0);
    }

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
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand); // put it on
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, inv, p) -> new WalkmanMenu(id, inv, hand), Component.translatable("screen.kiko.walkman")),
                    buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** It is a gadget, not armor: it does not wear out. */
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
