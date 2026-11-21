package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Clone chamber (Desolation pack model). The model is 3 blocks tall, so the block is a 3-high
 * column like a door: LOWER and UPPER are invisible, MIDDLE draws the whole model (its
 * coordinates reach 1 block below and 2 above it).
 *
 * STAGE 0 = empty, 1-4 = clone growing, 5 = finished. Right-click advances the stage,
 * sneak + right-click goes back.
 */
public class CloneChamberBlock extends HorizontalDirectionalBlock {

    public enum Part implements StringRepresentable {
        LOWER("lower"), MIDDLE("middle"), UPPER("upper");

        private final String name;

        Part(String name) { this.name = name; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 5);
    public static final int MAX_STAGE = 5;

    public CloneChamberBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(PART, Part.LOWER).setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    // ── Placing / removing the 3 parts ──────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() > level.getMaxBuildHeight() - 3
                || !level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(PART, Part.MIDDLE), 3);
        level.setBlock(pos.above(2), state.setValue(PART, Part.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Part part = state.getValue(PART);
        boolean needsBelow = part != Part.LOWER;
        boolean needsAbove = part != Part.UPPER;
        if (needsBelow && direction == Direction.DOWN && !isPart(neighbor, part == Part.UPPER ? Part.MIDDLE : Part.LOWER)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (needsAbove && direction == Direction.UP && !isPart(neighbor, part == Part.LOWER ? Part.MIDDLE : Part.UPPER)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    private boolean isPart(BlockState other, Part part) {
        return other.is(this) && other.getValue(PART) == part;
    }

    /** Only the lower part drops the item, so breaking another part removes the lower one without a drop. */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos lowerPos = lowerPos(pos, state);
            BlockState lower = level.getBlockState(lowerPos);
            if (lower.is(this) && state.getValue(PART) != Part.LOWER) {
                if (!player.isCreative()) {
                    Block.dropResources(lower, level, lowerPos, null, player, player.getMainHandItem());
                }
                level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, lowerPos, Block.getId(lower));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private static BlockPos lowerPos(BlockPos pos, BlockState state) {
        return switch (state.getValue(PART)) {
            case LOWER -> pos;
            case MIDDLE -> pos.below();
            case UPPER -> pos.below(2);
        };
    }

    // ── Stages ──────────────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        int stage = state.getValue(STAGE);
        int next = player.isShiftKeyDown() ? (stage + MAX_STAGE) % (MAX_STAGE + 1) : (stage + 1) % (MAX_STAGE + 1);
        if (!level.isClientSide) {
            BlockPos base = lowerPos(pos, state);
            for (int i = 0; i < 3; i++) {
                BlockPos p = base.above(i);
                BlockState s = level.getBlockState(p);
                if (s.is(this)) {
                    level.setBlock(p, s.setValue(STAGE, next), 3);
                }
            }
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6F, 0.8F + next * 0.08F);
            player.displayClientMessage(Component.literal("Clone chamber: stage " + next + "/" + MAX_STAGE), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
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
