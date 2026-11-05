package net.kiko.client.sunglasses;

import com.mojang.blaze3d.platform.InputConstants;
import net.kiko.KikoMod;
import net.kiko.client.PoseBindings;
import net.kiko.item.SonicSunglassesItem;
import net.kiko.network.ModNetwork;
import net.kiko.network.SonicSunglassesC2SPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SonicSunglassesClient {

    /** Use the current mode; sneak + this key changes the mode. */
    public static final KeyMapping USE = new KeyMapping("key.kiko.sonic_sunglasses",
            InputConstants.UNKNOWN.getValue(), PoseBindings.CATEGORY);

    private SonicSunglassesClient() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (USE.consumeClick()) {
            if (mc.screen != null) continue;
            if (!(mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SonicSunglassesItem)) continue;
            byte action = mc.player.isShiftKeyDown() ? SonicSunglassesItem.CYCLE : SonicSunglassesItem.USE;
            ModNetwork.CHANNEL.sendToServer(new SonicSunglassesC2SPacket(action));
        }
    }
}
