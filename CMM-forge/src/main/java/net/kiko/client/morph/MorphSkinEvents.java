package net.kiko.client.morph;

import net.kiko.KikoMod;
import net.kiko.item.MorphWandItem;
import net.kiko.morph.ClientMorphStore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sneak + right-click in the air with the wand while morphed: choose a custom skin. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MorphSkinEvents {
    private MorphSkinEvents() {}

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || !(event.getItemStack().getItem() instanceof MorphWandItem)) return;
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() != mc.player || !mc.player.isShiftKeyDown()) return;
        if (ClientMorphStore.get(mc.player.getUUID()).isEmpty() || mc.screen instanceof MorphSkinScreen) return;
        mc.setScreen(new MorphSkinScreen());
    }
}
