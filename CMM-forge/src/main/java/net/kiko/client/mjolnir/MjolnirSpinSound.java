package net.kiko.client.mjolnir;

import net.kiko.item.MjolnirItem;
import net.kiko.item.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The spin sound: loops, follows the player, and stops when the hammer stops spinning. */
public class MjolnirSpinSound extends AbstractTickableSoundInstance {

    private final Player player;

    public MjolnirSpinSound(Player player) {
        super(ModSounds.MJOLNIR_SPIN.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.8F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    public static boolean shouldPlay(Player player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem() instanceof MjolnirItem && MjolnirItem.isSpinning(held) && !MjolnirItem.isFlying(held);
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !shouldPlay(player)) {
            stop();
            return;
        }
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
