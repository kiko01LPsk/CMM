package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Storage for the SCP Drawers: 9 slots, shown in the same 3x3 screen as a dispenser.
 * It reuses the dispenser's container code (saving, hoppers, loot tables, custom names),
 * but nothing ever dispenses items because the block has no redstone behaviour.
 */
public class ScpDrawersBlockEntity extends DispenserBlockEntity {

    public ScpDrawersBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SCP_DRAWERS_ENTITY.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.kiko.scp_drawers");
    }
}
