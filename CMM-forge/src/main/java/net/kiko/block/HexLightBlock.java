package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Hexagonal panel light on any surface; neighbours on the same surface join edge to edge into a honeycomb mesh. */
public class HexLightBlock extends Block {
    public static final DirectionProperty ATTACH = DirectionProperty.create("attach");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** Every second column is shifted half a block, so the hexagons interlock like a honeycomb. */
    public static final BooleanProperty ODD = BooleanProperty.create("odd");

    private static final VoxelShape S_UP = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape S_DOWN = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape S_NORTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape S_SOUTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape S_WEST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape S_EAST = Block.box(14, 0, 0, 16, 16, 16);

    public HexLightBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ATTACH, Direction.UP).setValue(LIT, true).setValue(ODD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ATTACH, LIT, ODD);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter g, BlockPos p, CollisionContext c) {
        return switch (st.getValue(ATTACH)) {
            case UP -> S_UP; case DOWN -> S_DOWN; case NORTH -> S_NORTH;
            case SOUTH -> S_SOUTH; case WEST -> S_WEST; default -> S_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState st, BlockGetter g, BlockPos p, CollisionContext c) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader lv, BlockPos pos) {
        Direction a = st.getValue(ATTACH);
        BlockPos sp = pos.relative(a);
        return lv.getBlockState(sp).isFaceSturdy(lv, sp, a.getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState st = defaultBlockState().setValue(ATTACH, ctx.getClickedFace().getOpposite());
        if (!st.canSurvive(ctx.getLevel(), ctx.getClickedPos())) return null;
        BlockPos p = ctx.getClickedPos();
        Direction.Axis ax = st.getValue(ATTACH).getAxis();
        int col = ax == Direction.Axis.X ? p.getZ() : p.getX();
        return st.setValue(ODD, (col & 1) != 0);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState n, LevelAccessor lv, BlockPos pos, BlockPos npos) {
        if (d == st.getValue(ATTACH) && !st.canSurvive(lv, pos)) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return st;
    }

    @Override
    public InteractionResult use(BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
        BlockState ns = st.cycle(LIT);
        lv.setBlock(pos, ns, 3);
        lv.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, ns.getValue(LIT) ? 0.6F : 0.5F);
        return InteractionResult.sidedSuccess(lv.isClientSide);
    }
}
