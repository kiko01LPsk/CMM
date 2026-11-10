package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A normal swinging door that only opens for the player named Tomcaik (by hand) or with a sonic screwdriver in
 * redstone mode (see SonicScrewdriverItem). Redstone, mobs and everybody else cannot open it.
 */
public class SecureDoorBlock extends DoorBlock {

    public static final String OWNER = "Tomcaik";

    public SecureDoorBlock(Properties properties, BlockSetType type) {
        super(properties, type);
    }

    /** Opens or closes the door; only the owner and the screwdriver call this. */
    public void unlockToggle(Entity entity, Level level, BlockState state, BlockPos pos) {
        super.setOpen(entity, level, state, pos, !state.getValue(BlockStateProperties.OPEN));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (OWNER.equalsIgnoreCase(player.getGameProfile().getName())) {
            if (!level.isClientSide()) unlockToggle(player, level, state, pos);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        // A sonic screwdriver in the hand is left to the item itself (its redstone mode opens the door).
        if (player.getItemInHand(hand).getItem() instanceof net.kiko.item.SonicScrewdriverItem) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            player.displayClientMessage(Component.translatable("block.kiko.secure_door.locked"), true);
        } else {
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Mobs, redstone and everything else that asks the door to open are ignored. */
    @Override
    public void setOpen(Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        // No reaction to redstone power.
    }
}
