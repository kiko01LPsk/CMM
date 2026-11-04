package net.kiko.block;

import net.kiko.item.ModItems;
import net.kiko.item.ModSounds;
import net.kiko.mjolnir.MjolnirSaved;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mjolnir standing head-down in the ground, a little tilted (drawn by MjolnirBlockRenderer).
 * Nobody can break it or push it. A worthy player lifts it with right-click; everyone else
 * only gets "You are not worthy."
 */
public class MjolnirBlock extends BaseEntityBlock {

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public MjolnirBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .noLootTable());
        registerDefaultState(stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MjolnirBlockEntity(pos, state);
    }

    /** The block itself is invisible, the block entity renderer draws the hammer. */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.MJOLNIR.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** Right-click: the worthy lift it, everybody else fails. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!MjolnirSaved.isWorthy(player)) {
            net.kiko.item.MjolnirItem.notWorthy(player);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide()) {
            level.removeBlock(pos, false);
            giveToPlayer(player);
            level.playSound(null, pos, ModSounds.MJOLNIR_SWING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Creative-mode "break": only the worthy, and the hammer goes to their hand. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (!MjolnirSaved.isWorthy(player)) return false;
        if (!level.isClientSide() && !player.getAbilities().instabuild) giveToPlayer(player);
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    private static void giveToPlayer(Player player) {
        ItemStack stack = new ItemStack(ModItems.MJOLNIR.get());
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** If the ground disappears the hammer falls as an item again (and lands standing). */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
        if (!level.isClientSide() && !canSurvive(state, level, pos)) {
            level.removeBlock(pos, false);
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    new ItemStack(ModItems.MJOLNIR.get()));
            level.addFreshEntity(drop);
        }
    }

    // ── remember where it stands (so it can be called from far away) ───────────

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this) && level.getServer() != null) {
            MjolnirSaved.get(level.getServer()).addPlaced(GlobalPos.of(level.dimension(), pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!level.isClientSide() && !state.is(newState.getBlock()) && level.getServer() != null) {
            MjolnirSaved.get(level.getServer()).removePlaced(GlobalPos.of(level.dimension(), pos));
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    // ── placing ────────────────────────────────────────────────────────────────

    /** Stand the hammer at this position if there is room and solid ground under it. */
    public static boolean tryPlace(Level level, BlockPos pos, float yaw) {
        BlockState here = level.getBlockState(pos);
        boolean free = here.isAir() || (here.canBeReplaced() && here.getFluidState().isEmpty());
        if (!free) return false;
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return false;

        int rotation = Mth.floor((yaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        level.setBlock(pos, ModBlocks.MJOLNIR_BLOCK.get().defaultBlockState().setValue(ROTATION, rotation), 3);
        level.playSound(null, pos, ModSounds.MJOLNIR_DROP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
