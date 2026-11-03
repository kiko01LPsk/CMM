package net.kiko.mannequin;

import net.kiko.KikoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KikoMod.MOD_ID);

    public static final RegistryObject<EntityType<Mannequin>> MANNEQUIN = ENTITIES.register("mannequin",
            () -> EntityType.Builder.<Mannequin>of(Mannequin::new, MobCategory.MISC)
                    .sized(0.6F, 1.875F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "mannequin").toString()));

    private ModEntities() {}

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
