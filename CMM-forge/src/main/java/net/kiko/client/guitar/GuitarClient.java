package net.kiko.client.guitar;

import net.kiko.KikoMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/** Starts the guitar music for every player who has a switched-on guitar. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GuitarClient {

    private static final Map<Integer, GuitarSound> SOUNDS = new HashMap<>();

    private GuitarClient() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            SOUNDS.clear();
            return;
        }
        SOUNDS.keySet().removeIf(id -> mc.level.getEntity(id) == null);
        for (Player p : mc.level.players()) {
            GuitarSound sound = SOUNDS.get(p.getId());
            if ((sound == null || sound.isStopped()) && GuitarSound.shouldPlay(p)) {
                sound = new GuitarSound(p);
                SOUNDS.put(p.getId(), sound);
                mc.getSoundManager().play(sound);
            }
        }
    }
}
