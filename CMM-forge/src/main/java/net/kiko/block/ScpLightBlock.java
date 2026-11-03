package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An end rod without the particles. Everything else is the vanilla end rod: it can face all
 * six directions, it is placed against the face you click, and it gives off light level 14
 * (both come from EndRodBlock and the copied properties).
 */
public class ScpLightBlock extends EndRodBlock {

    public ScpLightBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** The vanilla end rod spawns "end rod" particles here; this one spawns none. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // intentionally empty
    }
}
