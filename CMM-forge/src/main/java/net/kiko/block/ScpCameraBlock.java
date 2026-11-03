package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A fence gate that opens and closes silently. It keeps everything else of the vanilla gate:
 * facing, the open/closed state (which the model uses to look different), the in-wall variant,
 * redstone control and the gate collision. Only the opening/closing sound (and the vibration
 * event that goes with it) are left out.
 */
public class ScpCameraBlock extends FenceGateBlock {

    public ScpCameraBlock(BlockBehaviour.Properties properties, WoodType woodType) {
        super(properties, woodType);
    }

    /** Same as FenceGateBlock#use, without playing a sound. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(OPEN)) {
            state = state.setValue(OPEN, false);
            level.setBlock(pos, state, 10);
        } else {
            Direction direction = player.getDirection();
            if (state.getValue(FACING) == direction.getOpposite()) {
                state = state.setValue(FACING, direction);
            }
            state = state.setValue(OPEN, true);
            level.setBlock(pos, state, 10);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Same as FenceGateBlock#neighborChanged (redstone), without playing a sound. */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, powered), 2);
            }
        }
    }
}
