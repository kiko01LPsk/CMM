package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds the one item shown inside a display case. */
public class DisplayCaseBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;

    public DisplayCaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DISPLAY_CASE_ENTITY.get(), pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack stack) {
        this.item = stack;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!item.isEmpty()) tag.put("Item", item.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        item = tag.contains("Item") ? ItemStack.of(tag.getCompound("Item")) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Tall cases show their item in the upper block, so the render box reaches up. */
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1);
    }
}
