package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Keeps the spawn egg whose creature is on show in a creature display case. */
public class CreatureDisplayBlockEntity extends BlockEntity {
    private ItemStack egg = ItemStack.EMPTY;

    public CreatureDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CREATURE_DISPLAY_ENTITY.get(), pos, state);
    }

    public ItemStack getEgg() {
        return egg;
    }

    public void setEgg(ItemStack stack) {
        this.egg = stack;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!egg.isEmpty()) tag.put("Egg", egg.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        egg = tag.contains("Egg") ? ItemStack.of(tag.getCompound("Egg")) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        tag.putBoolean("Synced", true); // an empty tag would be ignored by the client, so a removed item would stay visible
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() - 0.5, worldPosition.getY(), worldPosition.getZ() - 0.5,
                worldPosition.getX() + 2.5, worldPosition.getY() + 3.5, worldPosition.getZ() + 2.5);
    }
}
