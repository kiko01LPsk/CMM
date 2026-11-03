package net.kiko.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decorative, directional block (no gravity, no GUI, no anvil sounds).
 * It is placed the way a vanilla anvil is: the facing is the clockwise side of the direction the
 * player looks in. The hitbox follows the model and turns with the block.
 */
public class ScpPropBlock extends HorizontalDirectionalBlock {

    /** Hitbox per facing, indexed by Direction.get2DDataValue(). */
    private final VoxelShape[] shapes = new VoxelShape[4];

    public ScpPropBlock(Properties properties, double[][] boxesFacingNorth) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            shapes[direction.get2DDataValue()] = buildShape(boxesFacingNorth, quarterTurns(direction));
        }
    }

    /** Clockwise quarter turns of the blockstate "y" rotation for each facing (north = 0, east = 90 ...). */
    private static int quarterTurns(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static VoxelShape buildShape(double[][] boxes, int turns) {
        VoxelShape result = Shapes.empty();
        for (double[] b : boxes) {
            double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
            for (int i = 0; i < turns; i++) {
                // 90 degrees clockwise around the block centre, seen from above: (x, z) -> (16 - z, x)
                double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
                x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
            }
            result = Shapes.or(result, Block.box(x1, y1, z1, x2, y2, z2));
        }
        return result.optimize();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
