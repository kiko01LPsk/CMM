package net.kiko.item;

import net.kiko.KikoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Custom sounds. Files: assets/kiko/sounds/<name>.ogg, declared in assets/kiko/sounds.json. */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, KikoMod.MOD_ID);

    public static final RegistryObject<SoundEvent> RUBBER_CHICKEN_SQUEAK = register("rubber_chicken_squeak");
    public static final RegistryObject<SoundEvent> SONIC_USE = register("sonic_screwdriver_use");
    public static final RegistryObject<SoundEvent> SONIC_SWITCH = register("sonic_screwdriver_switch");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(KikoMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
