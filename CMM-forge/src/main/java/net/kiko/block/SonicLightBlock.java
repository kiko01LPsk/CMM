package net.kiko.block;

import net.kiko.item.SonicScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An invisible block that only gives light. It is placed next to a player who holds the sonic screwdriver in
 * white mode and removes itself a moment after nobody holds one near it.
 */
public class SonicLightBlock extends Block {

    public SonicLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 8);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean held = !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(3.0D),
                p -> SonicScrewdriverItem.isGlowing(p.getMainHandItem()) || SonicScrewdriverItem.isGlowing(p.getOffhandItem())
        ).isEmpty();
        if (held) {
            level.scheduleTick(pos, this, 8);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
