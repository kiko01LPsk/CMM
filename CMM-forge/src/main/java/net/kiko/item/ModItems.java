package net.kiko.item;

import net.kiko.KikoMod;
import net.kiko.block.ModBlocks;
import net.kiko.mannequin.MannequinItem;
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
            () -> new MeleeItem(new Item.Properties().stacksTo(1), 6.0, -2.4));

    public static final RegistryObject<Item> DYKA = ITEMS.register("dyka",
            () -> new MeleeItem(new Item.Properties().stacksTo(1), 3.0, -1.6));

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

    public static final RegistryObject<Item> INVENT_ZASTAVENI_CASU = ITEMS.register("invent_zastaveni_casu",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> INVENT_PORTAL = ITEMS.register("invent_portal",
            () -> new Item(new Item.Properties()));

    /** Prop: hold right-click to aim it in the bow pose, like the Rifle. */
    public static final RegistryObject<Item> MUSKET = ITEMS.register("musket",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PISTOL = ITEMS.register("pistol",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GUITAR = ITEMS.register("guitar",
            () -> new GuitarItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GAUNTLET = ITEMS.register("gauntlet",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // Held like tools/weapons (models use item/handheld)
    public static final RegistryObject<Item> NAIL_BAT = ITEMS.register("nail_bat",
            () -> new MeleeItem(new Item.Properties().stacksTo(1), 5.0, -2.8));

    public static final RegistryObject<Item> RUBBER_CHICKEN = ITEMS.register("rubber_chicken",
            () -> new RubberChickenItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SAW = ITEMS.register("saw",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /** Shows the in-game time; the hand is chosen by the "kiko:time" model predicate (see ClientModEvents). */
    public static final RegistryObject<Item> CLOCK = ITEMS.register("clock",
            () -> new Item(new Item.Properties()));

    // Items from the SCP pack (3D models, held like tools/weapons)
    public static final RegistryObject<Item> WILDWEST_RIFLE = ITEMS.register("wildwest_rifle",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WILDWEST_SHOTGUN = ITEMS.register("wildwest_shotgun",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    /** One-handed prop: hold right-click to raise it in the horn pose. Fires nothing. */
    public static final RegistryObject<Item> REVOLVER = ITEMS.register("revolver",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FN_P90 = ITEMS.register("fn_p90",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BATON = ITEMS.register("baton",
            () -> new MeleeItem(new Item.Properties().stacksTo(1), 3.0, -2.0));

    public static final RegistryObject<Item> KEYCARD = ITEMS.register("keycard",
            () -> new Item(new Item.Properties()));

    /** Prop: hold right-click to raise it in the bow pose. Fires nothing. */
    public static final RegistryObject<Item> RIFLE = ITEMS.register("rifle",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SAWTOOTH_PHONE = ITEMS.register("sawtooth_phone",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /** Prop camera: hold right-click to raise it in the bow pose (same as the Rifle). */
    public static final RegistryObject<Item> CAMERA = ITEMS.register("camera",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    /** Places a mannequin (white mannequin built on the armor stand). */
    public static final RegistryObject<Item> MANNEQUIN = ITEMS.register("mannequin",
            () -> new MannequinItem(new Item.Properties().stacksTo(16)));

    // Film reels (3D models, texture + model from the Film_reel resource pack)
    public static final RegistryObject<Item> FILM_REEL = ITEMS.register("film_reel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ZEME_NIKOHO = ITEMS.register("zeme_nikoho",
            () -> new Item(new Item.Properties()));

    // ── Paintings (each places one specific painting, see ModPaintings) ───────

    public static final RegistryObject<Item> PAINTING_TV = ITEMS.register("painting_tv",
            () -> new KikoPaintingItem(ModPaintings.TV.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_FOAM = ITEMS.register("painting_foam",
            () -> new KikoPaintingItem(ModPaintings.FOAM.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_PATA = ITEMS.register("painting_pata",
            () -> new KikoPaintingItem(ModPaintings.PATA.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_CAM = ITEMS.register("painting_cam",
            () -> new KikoPaintingItem(ModPaintings.CAM.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_OLD_LOGO = ITEMS.register("painting_old_logo",
            () -> new KikoPaintingItem(ModPaintings.OLD_LOGO.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MAWHENNA_SYMBOL = ITEMS.register("painting_mawhenna_symbol",
            () -> new KikoPaintingItem(ModPaintings.MAWHENNA_SYMBOL.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MAWHENNA_SYMBOL_SMALL = ITEMS.register("painting_mawhenna_symbol_small",
            () -> new KikoPaintingItem(ModPaintings.MAWHENNA_SYMBOL_SMALL.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MAWHENNA_LOGO = ITEMS.register("painting_mawhenna_logo",
            () -> new KikoPaintingItem(ModPaintings.MAWHENNA_LOGO.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_BIGGERD = ITEMS.register("painting_biggerd",
            () -> new KikoPaintingItem(ModPaintings.BIGGERD.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_TOMCAIK = ITEMS.register("painting_tomcaik",
            () -> new KikoPaintingItem(ModPaintings.TOMCAIK.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_DEYD = ITEMS.register("painting_deyd",
            () -> new KikoPaintingItem(ModPaintings.DEYD.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_SHOCKER = ITEMS.register("painting_shocker",
            () -> new KikoPaintingItem(ModPaintings.SHOCKER.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_KIKO = ITEMS.register("painting_kiko",
            () -> new KikoPaintingItem(ModPaintings.KIKO.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_SCORPY = ITEMS.register("painting_scorpy",
            () -> new KikoPaintingItem(ModPaintings.SCORPY.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_TRIXL = ITEMS.register("painting_trixl",
            () -> new KikoPaintingItem(ModPaintings.TRIXL.getId(), new Item.Properties()));

    // ── Sonic Screwdriver (one item, modes shown by colour) ────────────────────────────────────────────────────

    public static final RegistryObject<Item> SONIC_SCREWDRIVER =ITEMS.register("sonic_screwdriver",
            () -> new SonicScrewdriverItem(new Item.Properties().stacksTo(1)));

    // ── Armor ─────────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> GLASSES = ITEMS.register("glasses",
            () -> new ArmorItem(ModArmorMaterials.GLASSES, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> GLASSES2 = ITEMS.register("glasses2",
            () -> new ArmorItem(ModArmorMaterials.GLASSES2, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> SUNGLASSES = ITEMS.register("sunglasses",
            () -> new ArmorItem(ModArmorMaterials.SUNGLASSES, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> SUNGLASSES2 = ITEMS.register("sunglasses2",
            () -> new ArmorItem(ModArmorMaterials.SUNGLASSES2, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> REGGAE_HAT = ITEMS.register("reggae_hat",
            () -> new ArmorItem(ModArmorMaterials.REGGAE_HAT, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> VEST = ITEMS.register("vest",
            () -> new ArmorItem(ModArmorMaterials.VEST, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> ZAKAROVA_HELMA = ITEMS.register("zakarova_helma",
            () -> new ArmorItem(ModArmorMaterials.ZAKAR, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> ZAKARUV_CHESTPLATE = ITEMS.register("zakaruv_chestplate",
            () -> new ArmorItem(ModArmorMaterials.ZAKAR, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    // ── Creative tab ──────────────────────────────────────────────────────────

    // ── Warfare pack guns (props: hold right-click to aim, they fire nothing) ──
    public static final RegistryObject<Item> M870 = ITEMS.register("m870",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> M16 = ITEMS.register("m16",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RPG_7 = ITEMS.register("rpg_7",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AKS_74U = ITEMS.register("aks_74u",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> UMP = ITEMS.register("ump",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SNIPER_RIFLE = ITEMS.register("sniper_rifle",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GLOCK_17 = ITEMS.register("glock_17",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AK_47 = ITEMS.register("ak_47",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DESERT_EAGLE = ITEMS.register("desert_eagle",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PKP_PECHENEG = ITEMS.register("pkp_pecheneg",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MP5 = ITEMS.register("mp5",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FN_FAL = ITEMS.register("fn_fal",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));


    /** See MjolnirItem: only the worthy can lift it. */
    public static final RegistryObject<Item> MJOLNIR = ITEMS.register("mjolnir",
            () -> new MjolnirItem(new Item.Properties().stacksTo(1).fireResistant()));

    /** Wearable in the chestplate slot (drawn on the belt); right-click plays music. */
    public static final RegistryObject<Item> WALKMAN = ITEMS.register("walkman",
            () -> new WalkmanItem(new Item.Properties().stacksTo(1)));

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
                        output.accept(INVENT_ZASTAVENI_CASU.get());
                        output.accept(INVENT_PORTAL.get());
                        output.accept(MUSKET.get());
                        output.accept(PISTOL.get());
                        output.accept(GUITAR.get());
                        output.accept(GAUNTLET.get());
                        output.accept(NAIL_BAT.get());
                        output.accept(RUBBER_CHICKEN.get());
                        output.accept(SAW.get());
                        output.accept(CLOCK.get());
                        output.accept(FN_P90.get());
                        output.accept(REVOLVER.get());
                        output.accept(WILDWEST_RIFLE.get());
                        output.accept(WILDWEST_SHOTGUN.get());
                        output.accept(BATON.get());
                        output.accept(KEYCARD.get());
                        output.accept(RIFLE.get());
                        output.accept(SAWTOOTH_PHONE.get());
                        output.accept(CAMERA.get());
                        output.accept(MJOLNIR.get());
                        output.accept(WALKMAN.get());
                        output.accept(M870.get());
                        output.accept(M16.get());
                        output.accept(RPG_7.get());
                        output.accept(AKS_74U.get());
                        output.accept(UMP.get());
                        output.accept(SNIPER_RIFLE.get());
                        output.accept(GLOCK_17.get());
                        output.accept(AK_47.get());
                        output.accept(DESERT_EAGLE.get());
                        output.accept(PKP_PECHENEG.get());
                        output.accept(MP5.get());
                        output.accept(FN_FAL.get());
                        output.accept(MANNEQUIN.get());
                        output.accept(FILM_REEL.get());
                        output.accept(ZEME_NIKOHO.get());
                        // Paintings (stacks carry the painting variant, so use the default instance)
                        output.accept(PAINTING_TV.get().getDefaultInstance());
                        output.accept(PAINTING_FOAM.get().getDefaultInstance());
                        output.accept(PAINTING_PATA.get().getDefaultInstance());
                        output.accept(PAINTING_CAM.get().getDefaultInstance());
                        output.accept(PAINTING_OLD_LOGO.get().getDefaultInstance());
                        output.accept(PAINTING_MAWHENNA_SYMBOL.get().getDefaultInstance());
                        output.accept(PAINTING_MAWHENNA_SYMBOL_SMALL.get().getDefaultInstance());
                        output.accept(PAINTING_MAWHENNA_LOGO.get().getDefaultInstance());
                        output.accept(PAINTING_BIGGERD.get().getDefaultInstance());
                        output.accept(PAINTING_TOMCAIK.get().getDefaultInstance());
                        output.accept(PAINTING_DEYD.get().getDefaultInstance());
                        output.accept(PAINTING_SHOCKER.get().getDefaultInstance());
                        output.accept(PAINTING_KIKO.get().getDefaultInstance());
                        output.accept(PAINTING_SCORPY.get().getDefaultInstance());
                        output.accept(PAINTING_TRIXL.get().getDefaultInstance());
                        // Sonic screwdrivers
                        output.accept(SONIC_SCREWDRIVER.get());
                        // Armor
                        output.accept(GLASSES.get());
                        output.accept(GLASSES2.get());
                        output.accept(SUNGLASSES.get());
                        output.accept(SUNGLASSES2.get());
                        output.accept(REGGAE_HAT.get());
                        output.accept(VEST.get());
                        output.accept(ZAKAROVA_HELMA.get());
                        output.accept(ZAKARUV_CHESTPLATE.get());
                        // SCP blocks (all block items registered in ModBlocks)
                        ModBlocks.BLOCK_ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
                    })
                    .build());

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static void register(IEventBus eventBus) {
        KikoMod.LOGGER.info("Registering items for " + KikoMod.MOD_ID);
        ITEMS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
    }
}
