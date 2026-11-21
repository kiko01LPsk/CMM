package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A glass case, two blocks high, that shows a full set of armor (and one held item) on an invisible figure.
 * Right-click with armor to put it on, with another item to put it in the hand, with an empty hand to take
 * a piece off (the height you click picks helmet, chestplate, leggings or boots).
 */
public class ArmorDisplayBlock extends DisplayCaseTallBlock {

    public ArmorDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ArmorDisplayBlockEntity(pos, state) : null;
    }

    private static int slotFor(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        return switch (slot) {
            case HEAD -> ArmorDisplayBlockEntity.HEAD;
            case CHEST -> ArmorDisplayBlockEntity.CHEST;
            case LEGS -> ArmorDisplayBlockEntity.LEGS;
            case FEET -> ArmorDisplayBlockEntity.FEET;
            default -> ArmorDisplayBlockEntity.HAND;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos base = holderPos(state, pos);
        if (!(level.getBlockEntity(base) instanceof ArmorDisplayBlockEntity be)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            if (!level.isClientSide) {
                int slot = slotFor(held);
                ItemStack old = be.get(slot);
                be.set(slot, held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
                level.playSound(null, base, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        // empty hand: the click height (over the two blocks) picks the piece
        double height = (hit.getLocation().y - base.getY()) / 2.0;
        int slot = height > 0.75 ? ArmorDisplayBlockEntity.HEAD
                : height > 0.42 ? ArmorDisplayBlockEntity.CHEST
                : height > 0.14 ? ArmorDisplayBlockEntity.LEGS : ArmorDisplayBlockEntity.FEET;
        if (be.get(slot).isEmpty()) {
            // nothing there: fall back to the held item, then to whatever piece is left
            if (!be.get(ArmorDisplayBlockEntity.HAND).isEmpty()) slot = ArmorDisplayBlockEntity.HAND;
            else {
                slot = -1;
                for (int i = 0; i < 4; i++) if (!be.get(i).isEmpty()) { slot = i; break; }
            }
        }
        if (slot < 0) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack taken = be.get(slot).copy();
            be.set(slot, ItemStack.EMPTY);
            if (!player.getInventory().add(taken)) player.drop(taken, false);
            level.playSound(null, base, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ArmorDisplayBlockEntity be) {
            for (ItemStack stack : be.getItems()) {
                if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.onRemove(state, level, pos, newState, false);
    }
}
