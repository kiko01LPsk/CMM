package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Behaves like a vanilla flower (breaks instantly, can go in a flower pot, suspicious stew),
 * but can also be planted on more natural ground than the base game allows:
 * dirt types, farmland, sand, gravel, clay, terracotta, snow, and the common stones.
 * It is never generated in the world.
 */
public class MysteriousFlowerBlock extends FlowerBlock {

    public MysteriousFlowerBlock(Properties properties) {
        super(() -> MobEffects.NIGHT_VISION, 5, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.TERRACOTTA)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || state.is(net.minecraft.world.level.block.Blocks.CLAY)
                || state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.NETHERRACK)
                || state.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK);
    }
}
