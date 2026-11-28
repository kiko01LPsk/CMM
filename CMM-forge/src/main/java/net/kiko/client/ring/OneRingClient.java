package net.kiko.client.ring;

import net.kiko.KikoMod;
import net.kiko.ring.OneRingItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The body of a player wearing the ring is not drawn (the invisibility effect does that, so armor and held items still show),
 * and neither are the name tag and, in first person, an empty arm.
 */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, value = Dist.CLIENT)
public final class OneRingClient {
    private OneRingClient() {}

    @SubscribeEvent
    public static void onNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player p && OneRingItem.isWearing(p)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    /** Your own empty arm in first person disappears too; a held item is still shown. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && OneRingItem.isWearing(player) && event.getItemStack().isEmpty()) event.setCanceled(true);
    }
}
