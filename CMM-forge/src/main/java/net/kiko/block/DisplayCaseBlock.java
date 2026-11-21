package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A glass display case. Right-click with an item to put one in, right-click with an empty hand to take it out.
 * The item is shown turning slowly inside the glass.
 */
public class DisplayCaseBlock extends BaseEntityBlock {

    private final net.minecraft.world.phys.shapes.VoxelShape shape;
    private final double itemY;
    private final float itemScale;

    public DisplayCaseBlock(Properties properties) {
        this(properties, net.minecraft.world.phys.shapes.Shapes.block(), 0.72, 0.65F);
    }

    public DisplayCaseBlock(Properties properties, net.minecraft.world.phys.shapes.VoxelShape shape,
                            double itemY, float itemScale) {
        super(properties);
        this.shape = shape;
        this.itemY = itemY;
        this.itemScale = itemScale;
    }

    public double getItemY() {
        return itemY;
    }

    public float getItemScale() {
        return itemScale;
    }

    /** Whether this display accepts the item (the wall book display only takes books). */
    public boolean accepts(ItemStack stack) {
        return true;
    }

    /** The block that holds the item: for two-block-high cases that is the lower half. */
    protected BlockPos holderPos(BlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                                               BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayCaseBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(holderPos(state, pos)) instanceof DisplayCaseBlockEntity be)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        ItemStack inside = be.getItem();
        if (inside.isEmpty()) {
            if (held.isEmpty() || !accepts(held)) return InteractionResult.PASS;
            if (!level.isClientSide) {
                be.setItem(held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (!player.getInventory().add(inside.copy())) {
                player.drop(inside.copy(), false);
            }
            be.setItem(ItemStack.EMPTY);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DisplayCaseBlockEntity be && !be.getItem().isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getItem());
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
