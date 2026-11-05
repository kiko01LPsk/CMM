package net.kiko.client.guitar;

import net.kiko.item.GuitarItem;
import net.kiko.item.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The guitar music: loops, follows its player, stops when the guitar is switched off or leaves the main hand. */
public class GuitarSound extends AbstractTickableSoundInstance {

    private final Player player;

    public GuitarSound(Player player) {
        super(ModSounds.GUITAR_SONG.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.9F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    /** True only while the player holds a switched-on guitar in the main hand. */
    public static boolean shouldPlay(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof GuitarItem && GuitarItem.isPlaying(stack);
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
