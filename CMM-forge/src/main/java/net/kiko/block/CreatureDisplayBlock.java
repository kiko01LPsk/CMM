package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A big glass case for creatures: 2 blocks wide, 2 deep and 3 high (one block of base and two of glass).
 * Right-click with a spawn egg to put that creature on show (nothing is spawned), with an empty hand to take
 * the egg back. The case grows from the block you place it on towards +X, +Z and up.
 */
public class CreatureDisplayBlock extends BaseEntityBlock {
    public static final IntegerProperty DX = IntegerProperty.create("dx", 0, 1);
    public static final IntegerProperty DZ = IntegerProperty.create("dz", 0, 1);
    public static final IntegerProperty LY = IntegerProperty.create("ly", 0, 2);

    public CreatureDisplayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DX, 0).setValue(DZ, 0).setValue(LY, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(DX, DZ, LY);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Glass panes inside the case would show as lines, so faces between two parts of the case are not drawn. */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    private static boolean isMaster(BlockState s) {
        return s.getValue(DX) == 0 && s.getValue(DZ) == 0 && s.getValue(LY) == 0;
    }

    private static BlockPos masterOf(BlockState s, BlockPos pos) {
        return pos.offset(-s.getValue(DX), -s.getValue(LY), -s.getValue(DZ));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new CreatureDisplayBlockEntity(pos, state) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos base = context.getClickedPos();
        Level level = context.getLevel();
        if (base.getY() + 2 >= level.getMaxBuildHeight()) return null;
        for (int dx = 0; dx < 2; dx++)
            for (int dz = 0; dz < 2; dz++)
                for (int ly = 0; ly < 3; ly++)
                    if (!level.getBlockState(base.offset(dx, ly, dz)).canBeReplaced(context)) return null;
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        for (int dx = 0; dx < 2; dx++)
            for (int dz = 0; dz < 2; dz++)
                for (int ly = 0; ly < 3; ly++) {
                    if (dx == 0 && dz == 0 && ly == 0) continue;
                    level.setBlock(pos.offset(dx, ly, dz),
                            defaultBlockState().setValue(DX, dx).setValue(DZ, dz).setValue(LY, ly), 3);
                }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        int dx = state.getValue(DX) + direction.getStepX();
        int dz = state.getValue(DZ) + direction.getStepZ();
        int ly = state.getValue(LY) + direction.getStepY();
        if (dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1 && ly >= 0 && ly <= 2) {
            if (!neighbor.is(this) || neighbor.getValue(DX) != dx || neighbor.getValue(DZ) != dz || neighbor.getValue(LY) != ly) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return state;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos master = masterOf(state, pos);
            for (int dx = 0; dx < 2; dx++)
                for (int dz = 0; dz < 2; dz++)
                    for (int ly = 0; ly < 3; ly++) {
                        BlockPos p = master.offset(dx, ly, dz);
                        BlockState s = level.getBlockState(p);
                        if (!s.is(this) || p.equals(pos)) continue;
                        if (isMaster(s) && !player.isCreative()) {
                            dropResources(s, level, p, level.getBlockEntity(p), player, player.getMainHandItem());
                        }
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                    }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && isMaster(state)
                && level.getBlockEntity(pos) instanceof CreatureDisplayBlockEntity be && !be.getEgg().isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getEgg());
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos master = masterOf(state, pos);
        if (!(level.getBlockEntity(master) instanceof CreatureDisplayBlockEntity be)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof SpawnEggItem) {
            if (!level.isClientSide) {
                ItemStack old = be.getEgg();
                be.setEgg(held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
                level.playSound(null, master, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && !be.getEgg().isEmpty()) {
            if (!level.isClientSide) {
                ItemStack taken = be.getEgg().copy();
                be.setEgg(ItemStack.EMPTY);
                if (!player.getInventory().add(taken)) player.drop(taken, false);
                level.playSound(null, master, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
