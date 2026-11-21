package net.kiko.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** A block item with a grey credit line in its tooltip. */
public class LoreBlockItem extends BlockItem {
    private final String line;

    public LoreBlockItem(Block block, Properties properties, String line) {
        super(block, properties);
        this.line = line;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
