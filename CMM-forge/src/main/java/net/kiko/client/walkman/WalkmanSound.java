package net.kiko.client.walkman;

import net.kiko.item.ModSounds;
import net.kiko.item.WalkmanItem;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The walkman music: loops, follows its player, stops when the walkman is switched off or taken away. */
public class WalkmanSound extends AbstractTickableSoundInstance {

    private final Player player;

    public WalkmanSound(Player player) {
        super(ModSounds.WALKMAN_SONG.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.9F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    /** True if the player holds a switched-on walkman, or wears one. */
    public static boolean shouldPlay(Player player) {
        return isOn(player.getMainHandItem()) || isOn(player.getOffhandItem())
                || isOn(player.getItemBySlot(EquipmentSlot.CHEST));
    }

    private static boolean isOn(ItemStack stack) {
        return stack.getItem() instanceof WalkmanItem && WalkmanItem.isPlaying(stack);
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !shouldPlay(player)) {
            stop();
            return;
        }
        this.x = player.getX();
        this.y = player.getY() + 1.0D;
        this.z = player.getZ();
    }
}
