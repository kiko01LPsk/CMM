package net.kiko.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The items on show in an armor display case: helmet, chestplate, leggings, boots and one held item. */
public class ArmorDisplayBlockEntity extends BlockEntity {
    public static final int HEAD = 0, CHEST = 1, LEGS = 2, FEET = 3, HAND = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);

    public ArmorDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ARMOR_DISPLAY_ENTITY.get(), pos, state);
    }

    public ItemStack get(int slot) {
        return items.get(slot);
    }

    public void set(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
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

    /** The figure reaches into the upper block, so the render box must be two blocks high. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() - 0.5, worldPosition.getY(), worldPosition.getZ() - 0.5,
                worldPosition.getX() + 1.5, worldPosition.getY() + 2.5, worldPosition.getZ() + 1.5);
    }
}
