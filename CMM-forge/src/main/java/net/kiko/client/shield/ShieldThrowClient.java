package net.kiko.client.shield;

import net.kiko.KikoMod;
import net.kiko.item.ModItems;
import net.kiko.network.ModNetwork;
import net.kiko.network.ShieldThrowC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sneak + left click with the shield in the main hand throws it (instead of attacking / breaking). */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShieldThrowClient {

    private ShieldThrowClient() {}

    @SubscribeEvent
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || player.isSpectator()) return;
        if (!player.isShiftKeyDown() || !player.getMainHandItem().is(ModItems.CAPTAIN_SHIELD.get())) return;
        ModNetwork.CHANNEL.sendToServer(new ShieldThrowC2SPacket());
        event.setSwingHand(false);
        event.setCanceled(true);
    }
}
