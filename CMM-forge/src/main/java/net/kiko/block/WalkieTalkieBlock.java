package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decorative walkie talkie. Stands on the floor (facing the player) or hangs on the side of a block.
 * FACING is the side it is attached to when WALL is true, and the side its front faces when it stands.
 */
public class WalkieTalkieBlock extends HorizontalDirectionalBlock {

    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    private static final VoxelShape[] STANDING = new VoxelShape[4];
    private static final VoxelShape[] ON_WALL = new VoxelShape[4];

    static {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            int turns = switch (d) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
            STANDING[d.get2DDataValue()] = turn(4, 0, 7, 12, 14, 9, turns);
            ON_WALL[d.get2DDataValue()] = turn(4, 0, 0, 12, 14, 2, turns);
        }
    }

    private static VoxelShape turn(double x1, double y1, double z1, double x2, double y2, double z2, int turns) {
        for (int i = 0; i < turns; i++) {
            double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
            x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
        }
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public WalkieTalkieBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WALL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        BlockState state;
        if (clicked.getAxis().isHorizontal()) {
            state = defaultBlockState().setValue(WALL, true).setValue(FACING, clicked.getOpposite());
        } else {
            state = defaultBlockState().setValue(WALL, false)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(WALL)) {
            Direction side = state.getValue(FACING);
            BlockPos support = pos.relative(side);
            return level.getBlockState(support).isFaceSturdy(level, support, side.getOpposite());
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? state
                : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int i = state.getValue(FACING).get2DDataValue();
        return state.getValue(WALL) ? ON_WALL[i] : STANDING[i];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
