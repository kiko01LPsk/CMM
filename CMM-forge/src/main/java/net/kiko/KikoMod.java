package net.kiko;

import com.mojang.logging.LogUtils;
import net.kiko.block.ModBlocks;
import net.kiko.item.ModItems;
import net.kiko.item.ModPaintings;
import net.kiko.item.ModSounds;
import net.kiko.network.ModNetwork;
import net.kiko.mannequin.ModEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(KikoMod.MOD_ID)
public class KikoMod {

    public static final String MOD_ID = "kiko";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KikoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        net.kiko.walkman.ModMenus.register(modEventBus);
        ModPaintings.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntities.register(modEventBus);
        net.kiko.deer.ModDeer.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Kiko mod initialized!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        event.enqueueWork(ModBlocks::registerPottedPlants);
    }
}
