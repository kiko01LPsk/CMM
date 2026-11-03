package net.kiko.item;

import net.kiko.KikoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, KikoMod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KikoMod.MOD_ID);

    // ── Regular items ─────────────────────────────────────────────────────────
    public static final RegistryObject<Item> PROTILATKA = ITEMS.register("protilatka",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MUFFIN = ITEMS.register("muffin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DIAMANTOVA_SABLE = ITEMS.register("diamantova_sable",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DYKA = ITEMS.register("dyka",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> P_VIRUS = ITEMS.register("p_virus",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PANDORIKA = ITEMS.register("pandorika",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SALAZAROVA_DUSE = ITEMS.register("salazarova_duse",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SYRINGE_FULL = ITEMS.register("syringe_full",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SYRINGE_EMPTY = ITEMS.register("syringe_empty",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> INVENT = ITEMS.register("invent",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MUSKET = ITEMS.register("musket",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PISTOL = ITEMS.register("pistol",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GUITAR = ITEMS.register("guitar",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // ── Sonic Screwdrivers ────────────────────────────────────────────────────

    public static final RegistryObject<Item> SONIC_SCREWDRIVER = ITEMS.register("sonic_screwdriver",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_GREEN = ITEMS.register("sonic_screwdriver_green",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_TEAL = ITEMS.register("sonic_screwdriver_teal",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_MAGENTA = ITEMS.register("sonic_screwdriver_magenta",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_RED = ITEMS.register("sonic_screwdriver_red",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_WHITE = ITEMS.register("sonic_screwdriver_white",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SCREWDRIVER_YELLOW = ITEMS.register("sonic_screwdriver_yellow",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    // ── Armor ─────────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> GLASSES = ITEMS.register("glasses",
            () -> new ArmorItem(ModArmorMaterials.GLASSES, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> SUNGLASSES = ITEMS.register("sunglasses",
            () -> new ArmorItem(ModArmorMaterials.SUNGLASSES, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> REGGAE_HAT = ITEMS.register("reggae_hat",
            () -> new ArmorItem(ModArmorMaterials.REGGAE_HAT, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> VEST = ITEMS.register("vest",
            () -> new ArmorItem(ModArmorMaterials.VEST, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> ZAKAROVA_HELMA = ITEMS.register("zakarova_helma",
            () -> new ArmorItem(ModArmorMaterials.ZAKAR, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> ZAKARUV_CHESTPLATE = ITEMS.register("zakaruv_chestplate",
            () -> new ArmorItem(ModArmorMaterials.ZAKAR, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    // ── Creative tab ──────────────────────────────────────────────────────────

    public static final RegistryObject<CreativeModeTab> KIKO_GROUP = CREATIVE_TABS.register("kiko_group",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.kiko.kiko_group"))
                    .icon(() -> PANDORIKA.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        // Regular items
                        output.accept(MUFFIN.get());
                        output.accept(PROTILATKA.get());
                        output.accept(DIAMANTOVA_SABLE.get());
                        output.accept(DYKA.get());
                        output.accept(P_VIRUS.get());
                        output.accept(PANDORIKA.get());
                        output.accept(SALAZAROVA_DUSE.get());
                        output.accept(SYRINGE_FULL.get());
                        output.accept(SYRINGE_EMPTY.get());
                        output.accept(INVENT.get());
                        output.accept(MUSKET.get());
                        output.accept(PISTOL.get());
                        output.accept(GUITAR.get());
                        // Sonic screwdrivers
                        output.accept(SONIC_SCREWDRIVER.get());
                        output.accept(SONIC_SCREWDRIVER_GREEN.get());
                        output.accept(SONIC_SCREWDRIVER_TEAL.get());
                        output.accept(SONIC_SCREWDRIVER_MAGENTA.get());
                        output.accept(SONIC_SCREWDRIVER_RED.get());
                        output.accept(SONIC_SCREWDRIVER_WHITE.get());
                        output.accept(SONIC_SCREWDRIVER_YELLOW.get());
                        // Armor
                        output.accept(GLASSES.get());
                        output.accept(SUNGLASSES.get());
                        output.accept(REGGAE_HAT.get());
                        output.accept(VEST.get());
                        output.accept(ZAKAROVA_HELMA.get());
                        output.accept(ZAKARUV_CHESTPLATE.get());
                    })
                    .build());

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static void register(IEventBus eventBus) {
        KikoMod.LOGGER.info("Registering items for " + KikoMod.MOD_ID);
        ITEMS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
    }
}
