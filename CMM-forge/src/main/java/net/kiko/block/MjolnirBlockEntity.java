package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Only here so the hammer can be drawn by a block entity renderer (tilted, head down). */
public class MjolnirBlockEntity extends BlockEntity {

    public MjolnirBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MJOLNIR_BLOCK_ENTITY.get(), pos, state);
    }

    /** The handle sticks out above the block. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(0.5D).expandTowards(0.0D, 1.0D, 0.0D);
    }
}
