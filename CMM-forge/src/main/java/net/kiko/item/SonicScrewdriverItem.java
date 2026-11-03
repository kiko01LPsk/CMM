package net.kiko.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class SonicScrewdriverItem extends Item {

    public SonicScrewdriverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // Redstone lamp – toggle lit state
        if (block instanceof RedstoneLampBlock) {
            boolean nowLit = !state.getValue(BlockStateProperties.LIT);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, nowLit));
            level.playSound(null, pos,
                    SoundEvents.LEVER_CLICK,
                    SoundSource.BLOCKS, 0.3f, nowLit ? 1.2f : 0.8f);
            return InteractionResult.SUCCESS;
        }

        // Lever – toggle power
        if (block instanceof LeverBlock) {
            level.setBlockAndUpdate(pos, state.cycle(BlockStateProperties.POWERED));
            boolean nowPowered = level.getBlockState(pos).getValue(BlockStateProperties.POWERED);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK,
                    SoundSource.BLOCKS, 0.3f, nowPowered ? 0.6f : 0.5f);
            level.updateNeighborsAt(pos, block);
            return InteractionResult.SUCCESS;
        }

        // Button – press it (schedules auto-release via block tick)
        if (block instanceof ButtonBlock) {
            if (!state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                level.updateNeighborsAt(pos, block);
                level.scheduleTick(pos, block, 20);
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON,
                        SoundSource.BLOCKS, 0.3f, 0.6f);
            }
            return InteractionResult.SUCCESS;
        }

        // Door – toggle open/close (handles both halves)
        if (block instanceof DoorBlock) {
            boolean nowOpen = !state.getValue(BlockStateProperties.OPEN);
            DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, nowOpen));
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.getBlock() instanceof DoorBlock) {
                level.setBlockAndUpdate(otherPos, otherState.setValue(BlockStateProperties.OPEN, nowOpen));
            }
            level.playSound(null, pos,
                    nowOpen ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // Trapdoor – toggle open/close
        if (block instanceof TrapDoorBlock) {
            boolean nowOpen = !state.getValue(BlockStateProperties.OPEN);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, nowOpen));
            level.playSound(null, pos,
                    nowOpen ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // Fence gate – toggle open/close
        if (block instanceof FenceGateBlock) {
            boolean nowOpen = !state.getValue(BlockStateProperties.OPEN);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, nowOpen));
            level.playSound(null, pos,
                    nowOpen ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
