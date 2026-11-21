package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Decorative "dust" that lays and connects like redstone wire (flat on the ground, up walls, one block up/down steps),
 * but never carries or gives any signal. Used for the Blood and Glowing blood items.
 */
public class BloodDustBlock extends Block {

    public static final EnumProperty<RedstoneSide> NORTH = RedStoneWireBlock.NORTH;
    public static final EnumProperty<RedstoneSide> EAST = RedStoneWireBlock.EAST;
    public static final EnumProperty<RedstoneSide> SOUTH = RedStoneWireBlock.SOUTH;
    public static final EnumProperty<RedstoneSide> WEST = RedStoneWireBlock.WEST;

    private static final VoxelShape DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
    private static final VoxelShape[] ARM = {
            Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 3.0D),   // north
            Block.box(13.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), // east
            Block.box(3.0D, 0.0D, 13.0D, 13.0D, 1.0D, 16.0D), // south
            Block.box(0.0D, 0.0D, 3.0D, 3.0D, 1.0D, 13.0D)    // west
    };
    private static final VoxelShape[] WALL = {
            Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D),
            Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D),
            Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D)
    };
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public BloodDustBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE)
                .setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    private static EnumProperty<RedstoneSide> prop(Direction d) {
        return switch (d) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> WEST;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = DOT;
        for (int i = 0; i < 4; i++) {
            RedstoneSide side = state.getValue(prop(SIDES[i]));
            if (side != RedstoneSide.NONE) shape = Shapes.or(shape, ARM[i]);
            if (side == RedstoneSide.UP) shape = Shapes.or(shape, WALL[i]);
        }
        return shape;
    }

    // ── connections ───────────────────────────────────────────────────────

    private static boolean isBlood(BlockState state) {
        return state.getBlock() instanceof BloodDustBlock;
    }

    private RedstoneSide connectingSide(BlockGetter level, BlockPos pos, Direction dir) {
        BlockPos sidePos = pos.relative(dir);
        BlockState sideState = level.getBlockState(sidePos);
        BlockPos abovePos = pos.above();
        boolean aboveFree = !level.getBlockState(abovePos).isRedstoneConductor(level, abovePos);
        if (aboveFree && isBlood(level.getBlockState(sidePos.above()))
                && sideState.isRedstoneConductor(level, sidePos)) {
            return sideState.isFaceSturdy(level, sidePos, dir.getOpposite()) ? RedstoneSide.UP : RedstoneSide.SIDE;
        }
        if (isBlood(sideState)) return RedstoneSide.SIDE;
        if (!sideState.isRedstoneConductor(level, sidePos) && isBlood(level.getBlockState(sidePos.below()))) {
            return RedstoneSide.SIDE;
        }
        return RedstoneSide.NONE;
    }

    private BlockState connectionState(BlockGetter level, BlockState state, BlockPos pos) {
        RedstoneSide[] found = new RedstoneSide[4];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            found[i] = connectingSide(level, pos, SIDES[i]);
            if (found[i] != RedstoneSide.NONE) count++;
        }
        if (count == 0) {
            for (int i = 0; i < 4; i++) found[i] = RedstoneSide.SIDE;
        } else if (count == 1) {
            for (int i = 0; i < 4; i++) {
                if (found[i] != RedstoneSide.NONE && found[(i + 2) % 4] == RedstoneSide.NONE) {
                    found[(i + 2) % 4] = RedstoneSide.SIDE;
                    break;
                }
            }
        }
        for (int i = 0; i < 4; i++) state = state.setValue(prop(SIDES[i]), found[i]);
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectionState(context.getLevel(), defaultBlockState(), context.getClickedPos());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP) || belowState.is(Blocks.HOPPER);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
            return;
        }
        BlockState updated = connectionState(level, state, pos);
        if (updated != state) level.setBlock(pos, updated, 2);
    }

    /** Wires one step up or down next to us have to re-check their connections too. */
    private void updateDiagonal(Level level, BlockPos pos) {
        for (Direction dir : SIDES) {
            BlockPos side = pos.relative(dir);
            BlockPos target = level.getBlockState(side).isRedstoneConductor(level, side) ? side.above() : side.below();
            if (isBlood(level.getBlockState(target))) level.neighborChanged(target, this, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !oldState.is(state.getBlock())) updateDiagonal(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!level.isClientSide && !state.is(newState.getBlock())) updateDiagonal(level, pos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }
}
