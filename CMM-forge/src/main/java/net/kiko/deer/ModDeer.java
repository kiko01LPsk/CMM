package net.kiko.deer;

import net.kiko.KikoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Deer banner pattern (textures: textures/entity/banner/deer.png and shield/deer.png). */
public final class ModDeer {

    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS =
            DeferredRegister.create(Registries.BANNER_PATTERN, KikoMod.MOD_ID);

    public static final RegistryObject<BannerPattern> DEER_PATTERN =
            BANNER_PATTERNS.register("deer", () -> new BannerPattern("kdr"));

    private ModDeer() {}

    public static void register(IEventBus bus) {
        BANNER_PATTERNS.register(bus);
    }
}
