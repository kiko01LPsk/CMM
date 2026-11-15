package net.kiko.item;

import net.kiko.KikoMod;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom paintings. Sizes are given in pixels (16 per block), so 64x32 = 4x2 blocks.
 * Textures: assets/kiko/textures/painting/<id>.png, names: painting.kiko.<id>.title in the lang file.
 * They are made placeable by data/minecraft/tags/painting_variant/placeable.json.
 */
public class ModPaintings {

    public static final DeferredRegister<PaintingVariant> PAINTINGS =
            DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, KikoMod.MOD_ID);

    public static final RegistryObject<PaintingVariant> TV =
            PAINTINGS.register("tv", () -> new PaintingVariant(64, 32));       // 4 x 2 blocks

    public static final RegistryObject<PaintingVariant> FOAM =
            PAINTINGS.register("foam", () -> new PaintingVariant(32, 32));     // 2 x 2 blocks

    public static final RegistryObject<PaintingVariant> FOAM_SMALL =
            PAINTINGS.register("foam_small", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> PATA =
            PAINTINGS.register("pata", () -> new PaintingVariant(16, 16));     // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> KAPPER =
            PAINTINGS.register("kapper", () -> new PaintingVariant(16, 16));   // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> CAM =
            PAINTINGS.register("cam", () -> new PaintingVariant(32, 16));      // 2 x 1 blocks

    public static final RegistryObject<PaintingVariant> OLD_LOGO =
            PAINTINGS.register("old_logo", () -> new PaintingVariant(32, 32)); // 2 x 2 blocks

    public static final RegistryObject<PaintingVariant> MAWHENNA_SYMBOL =
            PAINTINGS.register("mawhenna_symbol", () -> new PaintingVariant(32, 32));       // 2 x 2 blocks

    public static final RegistryObject<PaintingVariant> MAWHENNA_SYMBOL_SMALL =
            PAINTINGS.register("mawhenna_symbol_small", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> MAWHENNA_LOGO =
            PAINTINGS.register("mawhenna_logo", () -> new PaintingVariant(32, 32));         // 2 x 2 blocks

    public static final RegistryObject<PaintingVariant> BIGGERD =
            PAINTINGS.register("biggerd", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> TOMCAIK =
            PAINTINGS.register("tomcaik", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> DEYD =
            PAINTINGS.register("deyd", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> SHOCKER =
            PAINTINGS.register("shocker", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> KIKO =
            PAINTINGS.register("kiko", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> SCORPY =
            PAINTINGS.register("scorpy", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> TRIXL =
            PAINTINGS.register("trixl", () -> new PaintingVariant(16, 16)); // 1 x 1 blocks

    public static final RegistryObject<PaintingVariant> MODEL_POSTER_1 =
            PAINTINGS.register("model_poster_1", () -> new PaintingVariant(16, 32)); // 1 x 2 blocks

    public static final RegistryObject<PaintingVariant> MODEL_POSTER_2 =
            PAINTINGS.register("model_poster_2", () -> new PaintingVariant(16, 32)); // 1 x 2 blocks

    public static final RegistryObject<PaintingVariant> MODEL_POSTER_3 =
            PAINTINGS.register("model_poster_3", () -> new PaintingVariant(16, 32)); // 1 x 2 blocks

    public static final RegistryObject<PaintingVariant> MODEL_POSTER_4 =
            PAINTINGS.register("model_poster_4", () -> new PaintingVariant(16, 32)); // 1 x 2 blocks

    public static void register(IEventBus eventBus) {
        PAINTINGS.register(eventBus);
    }
}
