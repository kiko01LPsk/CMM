package net.kiko.walkman;

import net.kiko.item.WalkmanItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.kiko.KikoMod;

/** The walkman menu: one cassette slot (kept inside the walkman item) and the player's inventory. */
public class WalkmanMenu extends AbstractContainerMenu {

    public static final TagKey<Item> CASSETTES = TagKey.create(Registries.ITEM, new ResourceLocation(KikoMod.MOD_ID, "cassettes"));

    // button ids (see clickMenuButton)
    public static final int PLAY_PAUSE = 0, STOP = 1, BACK = 2, FORWARD = 3, NEXT = 4, PREVIOUS = 5;
    /** How far the forward / back buttons jump, in ticks (5 seconds). */
    public static final int SEEK_TICKS = 100;

    private boolean loading;
    private final Player player;
    private final InteractionHand hand;
    private final Container cassette = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            save();
        }
    };

    public WalkmanMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenus.WALKMAN.get(), id);
        this.player = inventory.player;
        this.hand = hand;
        load();

        addSlot(new Slot(cassette, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(CASSETTES);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(guard(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(guard(inventory, col, 8 + col * 18, 142));
        }
    }

    /** The slot that holds this walkman cannot be picked up while the menu is open. */
    private Slot guard(Inventory inventory, int index, int x, int y) {
        boolean holder = hand == InteractionHand.MAIN_HAND ? index == inventory.selected : false;
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player p) {
                return !(holder && getItem().getItem() instanceof WalkmanItem);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return !(holder && getItem().getItem() instanceof WalkmanItem);
            }
        };
    }

    public ItemStack walkman() {
        return player.getItemInHand(hand);
    }

    private void load() {
        ItemStack stack = walkman();
        if (!(stack.getItem() instanceof WalkmanItem) || !stack.hasTag()) return;
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(stack.getTag(), items);
        loading = true;
        cassette.setItem(0, items.get(0));
        loading = false;
    }

    private boolean hadCassette;

    private void save() {
        if (loading) { hadCassette = !cassette.getItem(0).isEmpty(); return; }
        boolean has = !cassette.getItem(0).isEmpty();
        if (has != hadCassette) {
            hadCassette = has;
            if (!player.level().isClientSide) {
                player.level().playSound(null, player.blockPosition(),
                        (has ? net.kiko.item.ModSounds.WALKMAN_INSERT : net.kiko.item.ModSounds.WALKMAN_EJECT).get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
            }
        }
        ItemStack stack = walkman();
        if (!(stack.getItem() instanceof WalkmanItem)) return;
        CompoundTag tag = stack.getOrCreateTag();
        ContainerHelper.saveAllItems(tag, NonNullList.of(ItemStack.EMPTY, cassette.getItem(0)));
        if (cassette.getItem(0).isEmpty()) {
            // no cassette: it stops and starts from the beginning next time
            tag.remove("Items");
            tag.remove(WalkmanItem.PLAYING);
            tag.remove(WalkmanItem.POSITION);
            tag.remove(WalkmanItem.TRACK);
            tag.remove(WalkmanItem.SINCE);
        }
    }

    public boolean hasCassette() {
        return !cassette.getItem(0).isEmpty();
    }

    /** Buttons: play / pause, stop, back, forward, next song, previous song. They only do something with a cassette inside. */
    @Override
    public boolean clickMenuButton(Player p, int id) {
        ItemStack stack = walkman();
        if (!(stack.getItem() instanceof WalkmanItem) || !hasCassette()) return false;
        CompoundTag tag = stack.getOrCreateTag();
        long now = p.level().getGameTime();
        long pos = WalkmanItem.position(stack, now);
        boolean playing = WalkmanItem.isPlaying(stack);
        if (!(cassette.getItem(0).getItem() instanceof net.kiko.item.CassetteItem tape) || tape.tracks().isEmpty()) return false;
        // Position counts ticks from the start of the whole tape; the song is worked out from it
        long total = tape.totalTicks();
        if (pos >= total) pos = 0;
        int track = tape.trackAt(pos);
        switch (id) {
            case PLAY_PAUSE -> playing = !playing;
            case STOP -> { playing = false; pos = 0; }
            case BACK -> pos = Math.max(0, pos - SEEK_TICKS);
            case FORWARD -> pos = Math.min(total - 1, pos + SEEK_TICKS);
            case NEXT -> { pos = tape.startOf((track + 1) % tape.tracks().size()); }
            case PREVIOUS -> {
                // first press goes to the start of this song, a quick second press to the song before
                if (pos - tape.startOf(track) > 60) pos = tape.startOf(track);
                else pos = tape.startOf((track - 1 + tape.tracks().size()) % tape.tracks().size());
            }
            default -> { return false; }
        }
        tag.remove(WalkmanItem.TRACK);
        tag.putInt(WalkmanItem.POSITION, (int) pos);
        tag.putLong(WalkmanItem.SINCE, now);
        WalkmanItem.setPlaying(stack, playing);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            } else if (stack.is(CASSETTES)) {
                if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player p) {
        return walkman().getItem() instanceof WalkmanItem;
    }
}
