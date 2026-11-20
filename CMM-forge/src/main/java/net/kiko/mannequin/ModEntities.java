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

    public static final RegistryObject<EntityType<net.kiko.mjolnir.ThrownMjolnir>> THROWN_MJOLNIR = ENTITIES.register("thrown_mjolnir",
            () -> EntityType.Builder.<net.kiko.mjolnir.ThrownMjolnir>of(net.kiko.mjolnir.ThrownMjolnir::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(16)
                    .updateInterval(2)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "thrown_mjolnir").toString()));

    public static final RegistryObject<EntityType<net.kiko.keyboard.KeyboardEntity>> KEYBOARD = ENTITIES.register("keyboard",
            () -> EntityType.Builder.<net.kiko.keyboard.KeyboardEntity>of(net.kiko.keyboard.KeyboardEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "keyboard").toString()));

    public static final RegistryObject<EntityType<net.kiko.shield.ThrownShield>> THROWN_SHIELD = ENTITIES.register("thrown_shield",
            () -> EntityType.Builder.<net.kiko.shield.ThrownShield>of(net.kiko.shield.ThrownShield::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(16)
                    .updateInterval(2)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "thrown_shield").toString()));

    public static final RegistryObject<EntityType<net.kiko.chair.ThrownChair>> THROWN_CHAIR = ENTITIES.register("thrown_chair",
            () -> EntityType.Builder.<net.kiko.chair.ThrownChair>of(net.kiko.chair.ThrownChair::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "thrown_chair").toString()));

    public static final RegistryObject<EntityType<net.kiko.tripod.TripodCameraEntity>> TRIPOD_CAMERA = ENTITIES.register("tripod_camera",
            () -> EntityType.Builder.<net.kiko.tripod.TripodCameraEntity>of(net.kiko.tripod.TripodCameraEntity::new, MobCategory.MISC)
                    .sized(0.9F, 1.5F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "tripod_camera").toString()));

    public static final RegistryObject<EntityType<net.kiko.wheelchair.WheelchairEntity>> WHEELCHAIR = ENTITIES.register("wheelchair",
            () -> EntityType.Builder.<net.kiko.wheelchair.WheelchairEntity>of(net.kiko.wheelchair.WheelchairEntity::new, MobCategory.MISC)
                    .sized(0.9F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "wheelchair").toString()));

    public static final RegistryObject<EntityType<net.kiko.cardboard.CardboardBoxEntity>> CARDBOARD_BOX = ENTITIES.register("cardboard_box_car",
            () -> EntityType.Builder.<net.kiko.cardboard.CardboardBoxEntity>of(net.kiko.cardboard.CardboardBoxEntity::new, MobCategory.MISC)
                    .sized(0.85F, 0.85F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(new ResourceLocation(KikoMod.MOD_ID, "cardboard_box_car").toString()));

    private ModEntities() {}

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
