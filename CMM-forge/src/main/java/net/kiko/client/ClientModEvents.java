package net.kiko.client;

import net.kiko.KikoMod;
import net.kiko.client.mannequin.MannequinImitation;
import net.kiko.client.mannequin.MannequinRenderer;
import net.kiko.item.ModItems;
import net.kiko.item.SonicScrewdriverItem;
import net.kiko.mannequin.ModEntities;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        for (PoseBindings.Binding binding : PoseBindings.all()) {
            event.register(binding.key());
        }
        event.register(MannequinImitation.TOGGLE);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MANNEQUIN.get(), MannequinRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.CLOCK.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "time"),
                    ClientModEvents::clockTime);
            // Picks the colour model of the sonic screwdriver (see models/item/sonic_screwdriver.json).
            ItemProperties.register(ModItems.SONIC_SCREWDRIVER.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "mode"),
                    (stack, level, entity, seed) -> (float) SonicScrewdriverItem.getMode(stack).ordinal());
        });
    }

    /**
     * Value 0..1 that picks the clock frame (see models/item/clock.json).
     * It reads like a normal 12-hour clock face: the hand points at 12 at noon and midnight,
     * at 6 at sunrise (6:00) and sunset (18:00), moving one numeral per in-game hour.
     * In dimensions without a day/night cycle (Nether, End) the hand just spins.
     */
    private static float clockTime(ItemStack stack, ClientLevel clientLevel, LivingEntity entity, int seed) {
        Level level = clientLevel != null ? clientLevel : (entity != null ? entity.level() : null);
        if (level == null) return 0.0F;

        if (!level.dimensionType().natural()) {
            return (level.getGameTime() % 240L) / 240.0F;
        }
        // dayTime 0 = 6:00, so 6000 ticks (6 hours) after dayTime 0 the hand has to be at 12.
        return ((level.getDayTime() + 6000L) % 12000L) / 12000.0F;
    }
}
