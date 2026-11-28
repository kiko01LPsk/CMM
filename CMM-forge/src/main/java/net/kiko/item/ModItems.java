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
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.MUSKET));

    public static final RegistryObject<Item> PISTOL = ITEMS.register("pistol",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1), GunStats.PISTOL));

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
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.RIFLE));

    public static final RegistryObject<Item> WILDWEST_SHOTGUN = ITEMS.register("wildwest_shotgun",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SHOTGUN));

    /** Working revolver. */
    public static final RegistryObject<Item> REVOLVER = ITEMS.register("revolver",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1), GunStats.REVOLVER));

    public static final RegistryObject<Item> FN_P90 = ITEMS.register("fn_p90",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SMG));

    public static final RegistryObject<Item> BATON = ITEMS.register("baton",
            () -> new MeleeItem(new Item.Properties().stacksTo(1), 3.0, -2.0));

    public static final RegistryObject<Item> KEYCARD = ITEMS.register("keycard",
            () -> new Item(new Item.Properties()));

    /** Prop: hold right-click to raise it in the bow pose. Fires nothing. */
    public static final RegistryObject<Item> RIFLE = ITEMS.register("rifle",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.RIFLE));

    public static final RegistryObject<Item> SAWTOOTH_PHONE = ITEMS.register("sawtooth_phone",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /** Prop camera: hold right-click to raise it in the bow pose (same as the Rifle). */
    public static final RegistryObject<Item> CAMERA = ITEMS.register("camera",
            () -> new RifleItem(new Item.Properties().stacksTo(1)));

    /** Places a mannequin (white mannequin built on the armor stand). */
    public static final RegistryObject<Item> MANNEQUIN = ITEMS.register("mannequin",
            () -> new MannequinItem(new Item.Properties().stacksTo(16)));

    /** A keyboard that is an entity: points in any direction (right click turns it, sneak + right click tilts it). */
    public static final RegistryObject<Item> KEYBOARD = ITEMS.register("keyboard",
            () -> new KeyboardItem(new Item.Properties()));

    public static final RegistryObject<Item> DOLLAR_ONE = ITEMS.register("dollar_one", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_TWO = ITEMS.register("dollar_two", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_FIVE = ITEMS.register("dollar_five", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_TEN = ITEMS.register("dollar_ten", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_TWENTY = ITEMS.register("dollar_twenty", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_FIFTY = ITEMS.register("dollar_fifty", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_HUNDRED = ITEMS.register("dollar_hundred", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> DOLLAR_K = ITEMS.register("dollar_k", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> CENT_ONE = ITEMS.register("cent_one", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> CENT_FIVE = ITEMS.register("cent_five", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> CENT_TEN = ITEMS.register("cent_ten", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> CENT_QUARTER = ITEMS.register("cent_quarter", () -> new Item(new Item.Properties().stacksTo(50)));
    public static final RegistryObject<Item> PESO = ITEMS.register("peso", () -> new Item(new Item.Properties().stacksTo(50)));

    /** The studio camera without its tripod, held in the hand like the small camera. */
    public static final RegistryObject<Item> STUDIO_CAMERA = ITEMS.register("studio_camera",
            () -> new HeldCameraItem(new Item.Properties().stacksTo(1)));

    /** A studio camera on a tripod, placed as an entity that can be turned. */
    public static final RegistryObject<Item> TRIPOD_CAMERA = ITEMS.register("tripod_camera",
            () -> new TripodCameraItem(new Item.Properties().stacksTo(16)));

    /** A wheelchair you can sit in and drive (W/S, A/D, sneak to get out). */
    public static final RegistryObject<Item> WHEELCHAIR = ITEMS.register("wheelchair",
            () -> new WheelchairItem(new Item.Properties().stacksTo(1)));

    /** Captain America's shield: blocks like a vanilla shield; sneak + left click throws it and it comes back (model by Gamerkm, Cubik Studio). */
    public static final RegistryObject<Item> CAPTAIN_SHIELD = ITEMS.register("captain_shield",
            () -> new CaptainShieldItem(new Item.Properties().durability(336)));

    /** A plastic chair (model from Yuushya) that only kiko01LPsk can throw; it does no damage and breaks on impact. */
    public static final RegistryObject<Item> PLASTIC_CHAIR = ITEMS.register("plastic_chair",
            () -> new net.kiko.chair.PlasticChairItem(new Item.Properties().stacksTo(16)));

    // Film reels (3D models, texture + model from the Film_reel resource pack)
    public static final RegistryObject<Item> FILM_REEL = ITEMS.register("film_reel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ZEME_NIKOHO = ITEMS.register("zeme_nikoho",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TRIDENT_DEFAULT = ITEMS.register("trident_default",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> TRIDENT_IRON = ITEMS.register("trident_iron",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> TRIDENT_GOLD = ITEMS.register("trident_gold",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> TRIDENT_NETHERITE = ITEMS.register("trident_netherite",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> SPEAR_DEFAULT = ITEMS.register("spear_default",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> SPEAR_IRON = ITEMS.register("spear_iron",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> SPEAR_GOLD = ITEMS.register("spear_gold",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> SPEAR_NETHERITE = ITEMS.register("spear_netherite",
            () -> new net.kiko.trident.KikoTridentItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> GODSPEAR = ITEMS.register("godspear",
            () -> new net.kiko.trident.GodspearItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()));

    // ── Deer (ported from the Deer Mod by Arisuwu Mei) ────────────────────────
    public static final RegistryObject<Item> VENISON = ITEMS.register("venison",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build())));

    public static final RegistryObject<Item> COOKED_VENISON = ITEMS.register("cooked_venison",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationMod(0.8F).build())));

    public static final RegistryObject<Item> ANTLERS = ITEMS.register("antlers",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WHITE_ENERGY_DRINK = ITEMS.register("white_energy_drink",
            () -> new EnergyDrinkItem(new Item.Properties().stacksTo(16).food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(1).saturationMod(0.1F).alwaysEat()
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 1200, 1), 1.0F)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 1200, 0), 1.0F)
                    .build())));
    public static final RegistryObject<Item> DEER_CRACKERS = ITEMS.register("deer_crackers",
            () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationMod(0.2F).build())));

    public static final RegistryObject<Item> DEER_CRACKERS_ON_A_STICK = ITEMS.register("deer_crackers_on_a_stick",
            () -> new net.kiko.deer.DeerCrackersOnAStickItem(new Item.Properties().durability(100),
                    net.kiko.mannequin.ModEntities.DEER::get, 4));

    public static final RegistryObject<Item> DEER_BANNER_PATTERN = ITEMS.register("deer_banner_pattern",
            () -> new net.minecraft.world.item.BannerPatternItem(
                    net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BANNER_PATTERN,
                            new net.minecraft.resources.ResourceLocation(KikoMod.MOD_ID, "pattern_item/deer")),
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DEER_SPAWN_EGG = ITEMS.register("deer_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(net.kiko.mannequin.ModEntities.DEER,
                    0xB87339, 0xFAE6C8, new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_LUVDEER = ITEMS.register("painting_luvdeer",
            () -> new KikoPaintingItem(ModPaintings.LUVDEER.getId(), new Item.Properties()));

    // ── Paintings (each places one specific painting, see ModPaintings) ───────

    public static final RegistryObject<Item> PAINTING_TV = ITEMS.register("painting_tv",
            () -> new KikoPaintingItem(ModPaintings.TV.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_FOAM_SMALL = ITEMS.register("painting_foam_small",
            () -> new KikoPaintingItem(ModPaintings.FOAM_SMALL.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_FOAM = ITEMS.register("painting_foam",
            () -> new KikoPaintingItem(ModPaintings.FOAM.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_KAPPER = ITEMS.register("painting_kapper",
            () -> new KikoPaintingItem(ModPaintings.KAPPER.getId(), new Item.Properties()));

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

    public static final RegistryObject<Item> PAINTING_MODEL_POSTER_1 = ITEMS.register("painting_model_poster_1",
            () -> new KikoPaintingItem(ModPaintings.MODEL_POSTER_1.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MODEL_POSTER_2 = ITEMS.register("painting_model_poster_2",
            () -> new KikoPaintingItem(ModPaintings.MODEL_POSTER_2.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MODEL_POSTER_3 = ITEMS.register("painting_model_poster_3",
            () -> new KikoPaintingItem(ModPaintings.MODEL_POSTER_3.getId(), new Item.Properties()));

    public static final RegistryObject<Item> PAINTING_MODEL_POSTER_4 = ITEMS.register("painting_model_poster_4",
            () -> new KikoPaintingItem(ModPaintings.MODEL_POSTER_4.getId(), new Item.Properties()));

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

    public static final RegistryObject<Item> LOKI_CROWN = ITEMS.register("loki_crown",
            () -> new CrownItem(new Item.Properties().stacksTo(1)));

    // Goes on the head: the player turns into a flying drone (see DroneEvents / DroneRenderEvents).
    public static final RegistryObject<Item> DRONE = ITEMS.register("drone",
            () -> new DroneItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SONIC_SUNGLASSES = ITEMS.register("sonic_sunglasses",
            () -> new SonicSunglassesItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUNGLASSES2 = ITEMS.register("sunglasses2",
            () -> new ArmorItem(ModArmorMaterials.SUNGLASSES2, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> FUNKY_GLASSES_LEATHER = ITEMS.register("funky_glasses_leather",
            () -> new ArmorItem(ModArmorMaterials.FUNKY_LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> FUNKY_GLASSES_IRON = ITEMS.register("funky_glasses_iron",
            () -> new ArmorItem(ModArmorMaterials.FUNKY_IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> FUNKY_GLASSES_GOLD = ITEMS.register("funky_glasses_gold",
            () -> new ArmorItem(ModArmorMaterials.FUNKY_GOLD, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> FUNKY_GLASSES_NETHERITE = ITEMS.register("funky_glasses_netherite",
            () -> new ArmorItem(ModArmorMaterials.FUNKY_NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> ANGEL_WINGS = ITEMS.register("angel_wings",
            () -> new AngelWingsItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryObject<Item> COMPASS = ITEMS.register("compass",
            () -> new net.minecraft.world.item.CompassItem(new Item.Properties()));
    public static final RegistryObject<Item> HEART_OF_THE_SEA = ITEMS.register("heart_of_the_sea",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> NETHERITE_SWORD = ITEMS.register("netherite_sword",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.NETHERITE, 3, -2.4F,
                    new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> PIPE_WRENCH = ITEMS.register("pipe_wrench",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.STONE, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> SLEDGEHAMMER = ITEMS.register("sledgehammer",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> FIRE_AXE = ITEMS.register("fire_axe",
            () -> new net.minecraft.world.item.AxeItem(net.minecraft.world.item.Tiers.GOLD, 6.0F, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> CIRCULAR_SAW_AXE = ITEMS.register("circular_saw_axe",
            () -> new net.minecraft.world.item.AxeItem(net.minecraft.world.item.Tiers.IRON, 5.0F, -3.1F, new Item.Properties()));

    public static final RegistryObject<Item> PHONE = ITEMS.register("phone",
            () -> new PhoneItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MOUSE_EARS = ITEMS.register("mouse_ears",
            () -> new MouseEarsItem(new Item.Properties()));
    public static final RegistryObject<Item> MONOCLE = ITEMS.register("monocle",
            () -> new ArmorItem(ModArmorMaterials.MONOCLE, ArmorItem.Type.HELMET, new Item.Properties()));

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
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SHOTGUN));

    public static final RegistryObject<Item> M16 = ITEMS.register("m16",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.ASSAULT));

    public static final RegistryObject<Item> RPG_7 = ITEMS.register("rpg_7",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.ROCKET));

    public static final RegistryObject<Item> AKS_74U = ITEMS.register("aks_74u",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.ASSAULT));

    public static final RegistryObject<Item> UMP = ITEMS.register("ump",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SMG));

    public static final RegistryObject<Item> SNIPER_RIFLE = ITEMS.register("sniper_rifle",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SNIPER));

    public static final RegistryObject<Item> GLOCK_17 = ITEMS.register("glock_17",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1), GunStats.PISTOL));

    public static final RegistryObject<Item> AK_47 = ITEMS.register("ak_47",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.ASSAULT));

    public static final RegistryObject<Item> DESERT_EAGLE = ITEMS.register("desert_eagle",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1), GunStats.MAGNUM));

    /** Prop: hold right-click to aim it one-handed. Model from the Terramity mod. */
    public static final RegistryObject<Item> REQUIEM = ITEMS.register("requiem",
            () -> new OneHandGunItem(new Item.Properties().stacksTo(1), GunStats.MAGNUM));

    /** Right-click a mob to morph into it; the Reset morph key changes you back. */
    public static final RegistryObject<Item> MORPH_WAND = ITEMS.register("morph_wand",
            () -> new MorphWandItem(new Item.Properties().stacksTo(1)));

    /** An open cardboard box you can sit in and drive like a car (W/S, A/D, sneak to get out). */
    public static final RegistryObject<Item> CARDBOARD_BOX = ITEMS.register("cardboard_box_car",
            () -> new CardboardBoxItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PKP_PECHENEG = ITEMS.register("pkp_pecheneg",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.LMG));

    public static final RegistryObject<Item> MP5 = ITEMS.register("mp5",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.SMG));

    public static final RegistryObject<Item> FN_FAL = ITEMS.register("fn_fal",
            () -> new RifleItem(new Item.Properties().stacksTo(1), GunStats.ASSAULT));


    /** See MjolnirItem: only the worthy can lift it. */
    public static final RegistryObject<Item> MJOLNIR = ITEMS.register("mjolnir",
            () -> new MjolnirItem(new Item.Properties().stacksTo(1).fireResistant()));

    /** Wearable in the chestplate slot (drawn on the belt); right-click plays music. */
    public static final RegistryObject<Item> WALKMAN = ITEMS.register("walkman",
            () -> new WalkmanItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CASSETTE_MAN_WHO_SOLD_THE_WORLD = ITEMS.register("cassette_man_who_sold_the_world",
            () -> new CassetteItem(new Item.Properties(), "Midge Ure",
                    java.util.List.of(new CassetteItem.Track("cassettes/man_who_sold_the_world.mp3", "The Man Who Sold the World", 6878))));

    public static final RegistryObject<Item> CASSETTE_AWESOME_MIX_VOL_1 = ITEMS.register("cassette_awesome_mix_vol_1",
            () -> new CassetteItem(new Item.Properties(), "Various artists",
                    java.util.List.of(
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_01.mp3", "Hooked on a Feeling", 3455),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_02.mp3", "Go All the Way", 4024),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_03.mp3", "Spirit in the Sky", 4845),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_04.mp3", "Moonage Daydream", 5624),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_05.mp3", "Fooled Around and Fell in Love", 5512),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_06.mp3", "I'm Not in Love", 7268),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_07.mp3", "I Want You Back", 3578),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_08.mp3", "Come and Get Your Love", 4134),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_09.mp3", "Cherry Bomb", 2759),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_10.mp3", "Escape (The Piña Colada Song)", 5545),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_11.mp3", "O-o-h Child", 3881),
                            new CassetteItem.Track("cassettes/awesome_mix_vol1_12.mp3", "Ain't No Mountain High Enough", 2982))));

    public static final RegistryObject<Item> CASSETTE_AWESOME_MIX_VOL_2 = ITEMS.register("cassette_awesome_mix_vol_2",
            () -> new CassetteItem(new Item.Properties(), "Various artists",
                    java.util.List.of(
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_01.mp3", "Mr. Blue Sky", 6072),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_02.mp3", "Fox on the Run", 4024),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_03.mp3", "Lake Shore Drive", 4707),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_04.mp3", "The Chain", 5347),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_05.mp3", "Bring It on Home to Me", 3254),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_06.mp3", "Southern Nights", 3603),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_07.mp3", "My Sweet Lord", 5629),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_08.mp3", "Brandy (You're a Fine Girl)", 3671),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_09.mp3", "Come a Little Bit Closer", 3337),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_10.mp3", "Wham Bam Shang-A-Lang", 4247),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_11.mp3", "Surrender", 5079),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_12.mp3", "Father and Son", 4383),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_13.mp3", "Flash Light", 5383),
                            new CassetteItem.Track("cassettes/awesome_mix_vol2_14.mp3", "Guardians Inferno", 3940))));

    public static final RegistryObject<Item> CASSETTE_BRAINROT_MIX = ITEMS.register("cassette_brainrot_mix",
            () -> new CassetteItem(new Item.Properties(), "Various artists",
                    java.util.List.of(
                            new CassetteItem.Track("cassettes/brainrot_mix_01.mp3", "Bombardino Crocodilo", 3091),
                            new CassetteItem.Track("cassettes/brainrot_mix_02.mp3", "Brainrot Gigachads Choir", 1750),
                            new CassetteItem.Track("cassettes/brainrot_mix_03.mp3", "Bomboclat Wallahi", 2484),
                            new CassetteItem.Track("cassettes/brainrot_mix_04.mp3", "KFC Pigeon Song", 2806))));

    public static final RegistryObject<Item> CASSETTE_COSMIC_MIX_VOL_1 = ITEMS.register("cassette_cosmic_mix_vol_1",
            () -> new CassetteItem(new Item.Properties(), "Various artists",
                    java.util.List.of(
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_01.mp3", "Hooked on a Feeling", 3462),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_02.mp3", "Rocky Mountain Way", 6322),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_03.mp3", "Don't Stop Me Now", 4202),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_04.mp3", "Drift Away", 4742),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_05.mp3", "So You Are a Star", 4562),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_06.mp3", "The Boys Are Back in Town", 5362),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_07.mp3", "Walk Away", 4282),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_08.mp3", "Funk #49", 4722),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_09.mp3", "Shake Your Groove Thing", 6662),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_10.mp3", "I Will Survive", 9621),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_11.mp3", "Funk Funk", 5742),
                            new CassetteItem.Track("cassettes/cosmic_mix_vol1_12.mp3", "Joy to the World", 4382))));

    public static final RegistryObject<Item> CASSETTE_AWESOME_MIX_VOL_3 = ITEMS.register("cassette_awesome_mix_vol_3",
            () -> new CassetteItem(new Item.Properties(), "Various artists",
                    java.util.List.of(
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_01.mp3", "Creep (Acoustic)", 5177),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_02.mp3", "Crazy on You", 5873),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_03.mp3", "Since You Been Gone", 3946),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_04.mp3", "In the Meantime", 5994),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_05.mp3", "Reasons", 5989),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_06.mp3", "Do You Realize??", 4267),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_07.mp3", "We Care a Lot", 4875),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_08.mp3", "Koinu no Carnival (Minute Waltz)", 3812),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_09.mp3", "I'm Always Chasing Rainbows", 2570),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_10.mp3", "San Francisco", 3461),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_11.mp3", "Poor Girl", 3478),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_12.mp3", "This Is the Day", 5944),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_13.mp3", "No Sleep till Brooklyn", 4940),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_14.mp3", "Dog Days Are Over", 5058),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_15.mp3", "I Will Dare", 3988),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_16.mp3", "Come and Get Your Love (Single Version)", 4122),
                            new CassetteItem.Track("cassettes/awesome_mix_vol3_17.mp3", "Badlands", 4851))));

    public static final RegistryObject<Item> NECKLACE_SATANOVA = ITEMS.register("necklace_satanova",
            () -> new NecklaceItem(new Item.Properties().stacksTo(1)));

    /** Wear it in the chest slot and the player is completely invisible. */
    public static final RegistryObject<Item> ONE_RING = ITEMS.register("one_ring",
            () -> new net.kiko.ring.OneRingItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()));

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
                        output.accept(CASSETTE_MAN_WHO_SOLD_THE_WORLD.get());
                        output.accept(CASSETTE_AWESOME_MIX_VOL_1.get());
                        output.accept(CASSETTE_AWESOME_MIX_VOL_2.get());
                        output.accept(CASSETTE_BRAINROT_MIX.get());
                        output.accept(CASSETTE_COSMIC_MIX_VOL_1.get());
                        output.accept(CASSETTE_AWESOME_MIX_VOL_3.get());
                        output.accept(ONE_RING.get());
                        output.accept(NECKLACE_SATANOVA.get());
                        output.accept(M870.get());
                        output.accept(M16.get());
                        output.accept(RPG_7.get());
                        output.accept(AKS_74U.get());
                        output.accept(UMP.get());
                        output.accept(SNIPER_RIFLE.get());
                        output.accept(GLOCK_17.get());
                        output.accept(AK_47.get());
                        output.accept(DESERT_EAGLE.get());
                        output.accept(REQUIEM.get());
                        output.accept(MORPH_WAND.get());
                        output.accept(PKP_PECHENEG.get());
                        output.accept(MP5.get());
                        output.accept(FN_FAL.get());
                        output.accept(MANNEQUIN.get());
                        output.accept(KEYBOARD.get());
                        output.accept(WHEELCHAIR.get());
                        output.accept(CARDBOARD_BOX.get());
                        output.accept(TRIPOD_CAMERA.get());
                        output.accept(STUDIO_CAMERA.get());
                        output.accept(DOLLAR_ONE.get());
                        output.accept(DOLLAR_TWO.get());
                        output.accept(DOLLAR_FIVE.get());
                        output.accept(DOLLAR_TEN.get());
                        output.accept(DOLLAR_TWENTY.get());
                        output.accept(DOLLAR_FIFTY.get());
                        output.accept(DOLLAR_HUNDRED.get());
                        output.accept(DOLLAR_K.get());
                        output.accept(CENT_ONE.get());
                        output.accept(CENT_FIVE.get());
                        output.accept(CENT_TEN.get());
                        output.accept(CENT_QUARTER.get());
                        output.accept(PESO.get());
                        output.accept(net.kiko.block.ModBlocks.ORANGE_PORTAL.get());
                        output.accept(net.kiko.block.ModBlocks.BLUE_PORTAL.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_CASE_TALL_WOOD.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_CASE_TALL_STONE.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_CASE_TALL_DARK.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_CASE_TALL_TABLE.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_CASE_TALL_COUNTER.get());
                        output.accept(net.kiko.block.ModBlocks.DISPLAY_PLATE.get());
                        output.accept(net.kiko.block.ModBlocks.WALL_BOOK_DISPLAY.get());
                        output.accept(net.kiko.block.ModBlocks.SMALL_MONEY_PILE.get());
                        output.accept(net.kiko.block.ModBlocks.LARGE_MONEY_PILE.get());
                        output.accept(CAPTAIN_SHIELD.get());
                        output.accept(PLASTIC_CHAIR.get());
                        output.accept(FILM_REEL.get());
                        output.accept(ZEME_NIKOHO.get());
                        // Paintings (stacks carry the painting variant, so use the default instance)
                        output.accept(PAINTING_TV.get().getDefaultInstance());
                        output.accept(PAINTING_FOAM.get().getDefaultInstance());
                        output.accept(PAINTING_FOAM_SMALL.get().getDefaultInstance());
                        output.accept(PAINTING_PATA.get().getDefaultInstance());
                        output.accept(PAINTING_LUVDEER.get().getDefaultInstance());
                        output.accept(DEER_SPAWN_EGG.get());
                        output.accept(VENISON.get());
                        output.accept(COOKED_VENISON.get());
                        output.accept(ANTLERS.get());
                        output.accept(DEER_CRACKERS.get());
                        output.accept(WHITE_ENERGY_DRINK.get());
                        output.accept(DEER_CRACKERS_ON_A_STICK.get());
                        output.accept(DEER_BANNER_PATTERN.get());
                        output.accept(PAINTING_KAPPER.get().getDefaultInstance());
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
                        output.accept(PAINTING_MODEL_POSTER_1.get().getDefaultInstance());
                        output.accept(PAINTING_MODEL_POSTER_2.get().getDefaultInstance());
                        output.accept(PAINTING_MODEL_POSTER_3.get().getDefaultInstance());
                        output.accept(PAINTING_MODEL_POSTER_4.get().getDefaultInstance());
                        // Sonic screwdrivers
                        output.accept(SONIC_SCREWDRIVER.get());
                        // Armor
                        output.accept(GLASSES.get());
                        output.accept(GLASSES2.get());
                        output.accept(SUNGLASSES.get());
                        output.accept(SUNGLASSES2.get());
                        output.accept(SONIC_SUNGLASSES.get());
                        output.accept(LOKI_CROWN.get());
                        output.accept(DRONE.get());
                        output.accept(FUNKY_GLASSES_LEATHER.get());
                        output.accept(FUNKY_GLASSES_IRON.get());
                        output.accept(FUNKY_GLASSES_GOLD.get());
                        output.accept(FUNKY_GLASSES_NETHERITE.get());
                        output.accept(TRIDENT_DEFAULT.get());
                        output.accept(TRIDENT_IRON.get());
                        output.accept(TRIDENT_GOLD.get());
                        output.accept(TRIDENT_NETHERITE.get());
                        output.accept(SPEAR_DEFAULT.get());
                        output.accept(SPEAR_IRON.get());
                        output.accept(SPEAR_GOLD.get());
                        output.accept(SPEAR_NETHERITE.get());
                        output.accept(GODSPEAR.get());
                        output.accept(MONOCLE.get());
                        output.accept(MOUSE_EARS.get());
                        output.accept(PHONE.get());
                        output.accept(PIPE_WRENCH.get());
                        output.accept(SLEDGEHAMMER.get());
                        output.accept(FIRE_AXE.get());
                        output.accept(CIRCULAR_SAW_AXE.get());
                        output.accept(COMPASS.get());
                        output.accept(HEART_OF_THE_SEA.get());
                        output.accept(NETHERITE_SWORD.get());
                        output.accept(ANGEL_WINGS.get());
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
