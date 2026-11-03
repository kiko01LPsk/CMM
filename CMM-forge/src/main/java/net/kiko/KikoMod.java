package net.kiko;

import com.mojang.logging.LogUtils;
import net.kiko.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(KikoMod.MOD_ID)
public class KikoMod {

    public static final String MOD_ID = "kiko";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KikoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);

        LOGGER.info("Kiko mod initialized!");
    }
}
