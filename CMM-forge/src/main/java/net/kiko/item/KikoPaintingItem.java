package net.kiko.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * A painting item that always places one specific painting variant instead of a random one.
 * It works the same way as /give minecraft:painting{EntityTag:{variant:"..."}}: the variant is
 * stored in the stack's EntityTag, which vanilla applies to the painting when it is placed
 * (and uses for the title/author/size tooltip).
 */
public class KikoPaintingItem extends HangingEntityItem {

    private final ResourceLocation variantId;

    public KikoPaintingItem(ResourceLocation variantId, Item.Properties properties) {
        super(EntityType.PAINTING, properties);
        this.variantId = variantId;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTagElement("EntityTag").putString("variant", variantId.toString());
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Also covers stacks obtained without the tag (for example /give kiko:painting_tv).
        context.getItemInHand().getOrCreateTagElement("EntityTag").putString("variant", variantId.toString());
        return super.useOn(context);
    }
}
