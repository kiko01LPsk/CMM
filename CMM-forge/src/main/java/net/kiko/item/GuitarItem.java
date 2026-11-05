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
 * Sneak + right-click slings it on the back (see GuitarBackLayer); sneak + right-click again takes it off.
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
        if (player.isShiftKeyDown()) {
            // Sneak + right-click: sling the guitar on the back (or take it off again).
            if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                boolean on = !net.kiko.guitar.GuitarBackEvents.isOnBack(sp);
                net.kiko.guitar.GuitarBackEvents.setOnBack(sp, on);
                if (on) {
                    setPlaying(stack, false);
                    // Free the hand: select the next hotbar slot.
                    int slot = (sp.getInventory().selected + 1) % 9;
                    sp.getInventory().selected = slot;
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(slot));
                }
                sp.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        on ? "item.kiko.guitar.on_back" : "item.kiko.guitar.off_back"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (!level.isClientSide()) {
            setPlaying(stack, !isPlaying(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
