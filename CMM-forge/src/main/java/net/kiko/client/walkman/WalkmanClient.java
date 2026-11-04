package net.kiko.client.walkman;

import net.kiko.KikoMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/** Starts the walkman music for every player who has a switched-on walkman. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WalkmanClient {

    private static final Map<Integer, WalkmanSound> SOUNDS = new HashMap<>();

    private WalkmanClient() {}

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
            WalkmanSound sound = SOUNDS.get(p.getId());
            if ((sound == null || sound.isStopped()) && WalkmanSound.shouldPlay(p)) {
                sound = new WalkmanSound(p);
                SOUNDS.put(p.getId(), sound);
                mc.getSoundManager().play(sound);
            }
        }
    }
}
