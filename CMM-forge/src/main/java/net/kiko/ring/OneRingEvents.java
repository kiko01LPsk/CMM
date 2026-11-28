package net.kiko.ring;

import net.kiko.KikoMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** While the ring is worn the player has an invisibility effect with no particles and no icon (this also hides them from mobs). */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID)
public final class OneRingEvents {
    private OneRingEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        MobEffectInstance current = player.getEffect(MobEffects.INVISIBILITY);
        if (OneRingItem.isWearing(player)) {
            if (current == null || current.getDuration() < 20) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, false));
            }
        } else if (current != null && current.isAmbient() && !current.isVisible() && !current.showIcon() && current.getDuration() <= 60) {
            // took the ring off: drop our own effect (an invisibility potion is longer and left alone)
            player.removeEffect(MobEffects.INVISIBILITY);
        }
    }
}
