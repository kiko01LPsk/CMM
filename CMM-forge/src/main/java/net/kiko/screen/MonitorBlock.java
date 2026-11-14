package net.kiko.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * A thin monitor. Monitors that face the same way and touch each other join into one big screen.
 * LEFT/RIGHT/TOP/BOTTOM say which sides have a neighbour (seen from the front), so the frame is only drawn on the outside.
 */
public class MonitorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    private static final VoxelShape S_NORTH = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape S_SOUTH = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape S_EAST = Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape S_WEST = Block.box(13, 0, 0, 16, 16, 16);

    public MonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(LEFT, false).setValue(RIGHT, false).setValue(TOP, false).setValue(BOTTOM, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEFT, RIGHT, TOP, BOTTOM);
    }

    /** The side that is on the viewer's left when looking at a screen that faces {@code facing}. */
    public static Direction leftOf(Direction facing) { return facing.getClockWise(); }

    /** The side that is on the viewer's right. */
    public static Direction rightOf(Direction facing) { return facing.getCounterClockWise(); }

    private static boolean joins(BlockGetter level, BlockPos pos, Direction facing, Direction side) {
        BlockState other = level.getBlockState(pos.relative(side));
        return other.getBlock() instanceof MonitorBlock && other.getValue(FACING) == facing;
    }

    private BlockState withJoins(BlockState state, BlockGetter level, BlockPos pos) {
        Direction f = state.getValue(FACING);
        return state.setValue(LEFT, joins(level, pos, f, leftOf(f)))
                .setValue(RIGHT, joins(level, pos, f, rightOf(f)))
                .setValue(TOP, joins(level, pos, f, Direction.UP))
                .setValue(BOTTOM, joins(level, pos, f, Direction.DOWN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction facing = face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite();
        return withJoins(defaultBlockState().setValue(FACING, facing), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return withJoins(state, level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> S_SOUTH;
            case EAST -> S_EAST;
            case WEST -> S_WEST;
            default -> S_NORTH;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MonitorBlockEntity(pos, state);
    }

    /** A new monitor next to a playing screen joins in with the same video. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        if (!(level.getBlockEntity(pos) instanceof MonitorBlockEntity mine)) return;
        ScreenGroup group = ScreenGroup.find(level, pos);
        if (group == null) return;
        for (BlockPos member : group.members) {
            if (!member.equals(pos) && level.getBlockEntity(member) instanceof MonitorBlockEntity other && !other.getUrl().isEmpty()) {
                mine.copyFrom(other);
                break;
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.kiko.client.screen.MonitorClient.open(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
