package net.kiko.block;

import net.kiko.KikoMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SCP blocks. Each one copies the behaviour of its vanilla counterpart (hardness, sounds,
 * tool requirements, fence connections, iron-bars panes ...) and only changes the look.
 * Every block automatically gets a BlockItem, and all of them are shown in the CMM creative tab.
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KikoMod.MOD_ID);

    /** Items for the blocks above (kept separate from ModItems.ITEMS). */
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, KikoMod.MOD_ID);

    // ── Quartz ────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> SCP_QUARTZ_BLOCK = block("scp_quartz_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.QUARTZ_BLOCK)));

    public static final RegistryObject<Block> SCP_QUARTZ_SLAB = block("scp_quartz_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.QUARTZ_SLAB)));

    public static final RegistryObject<Block> SCP_QUARTZ_STAIRS = block("scp_quartz_stairs",
            () -> new StairBlock(() -> SCP_QUARTZ_BLOCK.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.QUARTZ_STAIRS)));

    // ── Andesite / diorite / granite ──────────────────────────────────────────
    public static final RegistryObject<Block> SCP_ANDESITE = block("scp_andesite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.ANDESITE)));

    public static final RegistryObject<Block> SCP_POLISHED_ANDESITE = block("scp_polished_andesite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.POLISHED_ANDESITE)));

    public static final RegistryObject<Block> SCP_DIORITE = block("scp_diorite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIORITE)));

    public static final RegistryObject<Block> SCP_POLISHED_DIORITE = block("scp_polished_diorite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.POLISHED_DIORITE)));

    public static final RegistryObject<Block> SCP_POLISHED_GRANITE = block("scp_polished_granite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.POLISHED_GRANITE)));

    // ── Nether brick ──────────────────────────────────────────────────────────
    public static final RegistryObject<Block> SCP_NETHER_BRICK = block("scp_nether_brick",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS)));

    public static final RegistryObject<Block> SCP_NETHER_BRICK_SLAB = block("scp_nether_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICK_SLAB)));

    public static final RegistryObject<Block> SCP_NETHER_BRICK_STAIRS = block("scp_nether_brick_stairs",
            () -> new StairBlock(() -> SCP_NETHER_BRICK.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.NETHER_BRICK_STAIRS)));

    public static final RegistryObject<Block> SCP_NETHER_BRICK_FENCE = block("scp_nether_brick_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICK_FENCE)));

    // ── Iron bars (a normal IronBarsBlock, so it connects and behaves like vanilla bars) ──
    public static final RegistryObject<Block> SCP_IRON_BARS = block("scp_iron_bars",
            () -> new IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS)));

    // ── Drawers (opens like a dispenser, but has no redstone function) ────────
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KikoMod.MOD_ID);

    public static final RegistryObject<Block> SCP_DRAWERS = block("scp_drawers",
            () -> new ScpDrawersBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER)));

    public static final RegistryObject<BlockEntityType<ScpDrawersBlockEntity>> SCP_DRAWERS_ENTITY =
            BLOCK_ENTITIES.register("scp_drawers",
                    () -> BlockEntityType.Builder.of(ScpDrawersBlockEntity::new, SCP_DRAWERS.get()).build(null));

    // ── Mjolnir standing in the ground (no item of its own, the Mjolnir item places it) ──
    public static final RegistryObject<Block> SMALL_MONEY_PILE = block("small_money_pile",
            () -> new MoneyPileBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.0F).sound(SoundType.WOOL),
                    Block.box(4, 0, 4, 12, 2.8, 12)));
    public static final RegistryObject<Block> LARGE_MONEY_PILE = block("large_money_pile",
            () -> new MoneyPileBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0F).sound(SoundType.WOOL),
                    Block.box(0.5, 0, 0.5, 15.5, 12, 15.5)));
    public static final RegistryObject<Block> DISPLAY_CASE_TALL_WOOD = block("display_case_tall_wood",
            () -> new DisplayCaseTallBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> DISPLAY_CASE_TALL_STONE = block("display_case_tall_stone",
            () -> new DisplayCaseTallBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> DISPLAY_CASE_TALL_DARK = block("display_case_tall_dark",
            () -> new DisplayCaseTallBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> DISPLAY_CASE_TALL_TABLE = block("display_case_tall_table",
            () -> new DisplayCaseTallBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> DISPLAY_CASE_TALL_COUNTER = block("display_case_tall_counter",
            () -> new DisplayCaseTallBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> DISPLAY_PLATE = block("display_plate",
            () -> new DisplayCaseBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F).sound(SoundType.WOOD),
                    Block.box(1, 0, 1, 15, 2, 15), 0.45, 0.5F));
    public static final RegistryObject<Block> WALL_BOOK_DISPLAY = block("wall_book_display",
            () -> new WallBookDisplayBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.0F).sound(SoundType.WOOD)));
    public static final RegistryObject<BlockEntityType<DisplayCaseBlockEntity>> DISPLAY_CASE_ENTITY =
            BLOCK_ENTITIES.register("display_case",
                    () -> BlockEntityType.Builder.of(DisplayCaseBlockEntity::new,
                            DISPLAY_CASE_TALL_WOOD.get(),
                            DISPLAY_CASE_TALL_STONE.get(),
                            DISPLAY_CASE_TALL_DARK.get(),
                            DISPLAY_CASE_TALL_TABLE.get(),
                            DISPLAY_CASE_TALL_COUNTER.get(),
                            DISPLAY_PLATE.get(),
                            WALL_BOOK_DISPLAY.get()).build(null));

    public static final RegistryObject<Block> MJOLNIR_BLOCK = BLOCKS.register("mjolnir_block", MjolnirBlock::new);

    public static final RegistryObject<BlockEntityType<MjolnirBlockEntity>> MJOLNIR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("mjolnir_block",
                    () -> BlockEntityType.Builder.of(MjolnirBlockEntity::new, MJOLNIR_BLOCK.get()).build(null));

    // ── Atlantida glass (behaves like vanilla glass, see-through, drops itself) ──
    public static final RegistryObject<Block> ATLANTIDA_GLASS = block("atlantida_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));

    public static final RegistryObject<Block> ATLANTIDA_BROKEN_GLASS = block("atlantida_broken_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));


    // ── Chroma blocks: one flat colour, no shading (green screen and the like) ──
    public static final RegistryObject<Block> CHROMA_RED = block("chroma_red",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHROMA_BLUE = block("chroma_blue",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHROMA_GREEN = block("chroma_green",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHROMA_WHITE = block("chroma_white",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHROMA_BLACK = block("chroma_black",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F).sound(SoundType.STONE)));

    // ── Doors and trapdoor (behave like their wooden vanilla versions) ────────
    // Doors use DoubleHighBlockItem so both halves are placed properly.
    public static final RegistryObject<Block> SCP_DOOR_1 = block("scp_door_1",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_DOOR), BlockSetType.BIRCH),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    public static final RegistryObject<Block> SCP_DOOR_2 = block("scp_door_2",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_DOOR), BlockSetType.DARK_OAK),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    // Opens normally, but only for Tomcaik or a sonic screwdriver in redstone mode.
    public static final RegistryObject<Block> SECURE_DOOR = block("secure_door",
            () -> new SecureDoorBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_DOOR), BlockSetType.DARK_OAK),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    // Opens normally, but the opening shows a bright portal wall and the door glows.
    public static final RegistryObject<Block> DIMENSIONAL_DOOR = block("dimensional_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_DOOR)
                    .lightLevel(st -> st.getValue(DoorBlock.OPEN) ? 13 : 0), BlockSetType.DARK_OAK),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    public static final RegistryObject<Block> SCP_TRAPDOOR = block("scp_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR), BlockSetType.OAK));

    // ── Camera: a fence gate (facing, open/closed models) that opens silently ──
    public static final RegistryObject<Block> SCP_CAMERA = block("scp_camera",
            () -> new ScpCameraBlock(BlockBehaviour.Properties.copy(Blocks.SPRUCE_FENCE_GATE), WoodType.SPRUCE));

    // ── Light: an end rod (6 directions, light level 14) without particles ────
    public static final RegistryObject<Block> SCP_LIGHT = block("scp_light",
            () -> new ScpLightBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD)));

    // ── Props: bunk, sink, toilet (decorative, directional, no anvil behaviour or sounds) ──
    public static final RegistryObject<Block> SCP_BUNK = block("scp_bunk",
            () -> new ScpPropBlock(propProperties(), PropShapes.BUNK));

    public static final RegistryObject<Block> SCP_SINK = block("scp_sink",
            () -> new ScpPropBlock(propProperties(), PropShapes.SINK));

    public static final RegistryObject<Block> SCP_TOILET = block("scp_toilet",
            () -> new ScpPropBlock(propProperties(), PropShapes.TOILET));

    private static BlockBehaviour.Properties propProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    // ── Studio light: tripod light, 2 blocks tall; right-click toggles it, sneak + right-click turns it ──
    public static final RegistryObject<Block> STUDIO_LIGHT = block("studio_light",
            () -> new StudioLightBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(1.0F).sound(SoundType.METAL).noOcclusion()
                    .lightLevel(st -> st.getValue(StudioLightBlock.LIT) ? 15 : 0)),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    // ── Box REGNUM: a fletching-table look-alike with the REGNUM logo on one side (no function, like the vanilla one) ──
    public static final RegistryObject<Block> BOX_REGNUM = block("box_regnum",
            () -> new BoxRegnumBlock(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    // ── Cardboard box props (models from the Nexo cardboard box pack) ──
    private static BlockBehaviour.Properties cardboard() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.4F).sound(SoundType.WOOL).noOcclusion();
    }

    public static final RegistryObject<Block> CARDBOARD_BOX_CLOSED = block("cardboard_box_closed",
            () -> new CardboardPropBlock(cardboard(), 0, 0, 0, 16, 16, 16));
    public static final RegistryObject<Block> CARDBOARD_BOX_OPEN = block("cardboard_box_open",
            () -> new CardboardPropBlock(cardboard(), 0, 0, 0, 16, 16, 16));
    public static final RegistryObject<Block> CARDBOARD_BOX_SMALL = block("cardboard_box_small",
            () -> new CardboardPropBlock(cardboard(), 2, 0, 0, 14, 8, 16));
    public static final RegistryObject<Block> CARDBOARD_BOX_STACK_1 = block("cardboard_box_stack_1",
            () -> new CardboardPropBlock(cardboard(), 0, 0, 0, 16, 24, 16));
    /** 3-block-tall clone chamber with 6 stages (model from the Desolation pack). */
    public static final RegistryObject<Block> CLONE_CHAMBER = block("clone_chamber",
            () -> new CloneChamberBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.GLASS).noOcclusion()));

    public static final RegistryObject<Block> CARDBOARD_BOX_STACK_2 = block("cardboard_box_stack_2",
            () -> new CardboardPropBlock(cardboard(), 0, 0, 0, 16, 32, 16));

    // ── Blood dust: lays like redstone wire but is purely decorative (no signal) ──
    public static final RegistryObject<Block> BLOOD_DUST = block("blood_dust",
            () -> new BloodDustBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                    .noCollission().noOcclusion().instabreak().sound(SoundType.WOOL)),
            b -> new net.kiko.item.LoreBlockItem(b, new Item.Properties(), "LostFlash Production"));
    public static final RegistryObject<Block> GLOWING_BLOOD_DUST = block("glowing_blood_dust",
            () -> new BloodDustBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                    .noCollission().noOcclusion().instabreak().sound(SoundType.WOOL).lightLevel(s -> 7)),
            b -> new net.kiko.item.LoreBlockItem(b, new Item.Properties(), "LostFlash Production"));

    // ── Monitor: joins with its neighbours into one big screen that plays a link ──
    public static final RegistryObject<Block> MONITOR = block("monitor",
            () -> new net.kiko.screen.MonitorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(1.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<BlockEntityType<net.kiko.screen.MonitorBlockEntity>> MONITOR_ENTITY =
            BLOCK_ENTITIES.register("monitor",
                    () -> BlockEntityType.Builder.of(net.kiko.screen.MonitorBlockEntity::new, MONITOR.get()).build(null));

    // ── Hexagonal panel light: sticks to any surface, neighbours connect into a mesh ──
    public static final RegistryObject<Block> HEX_LIGHT = block("hex_light",
            () -> new HexLightBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.5F).sound(SoundType.METAL).noOcclusion().noCollission()
                    .lightLevel(st -> st.getValue(HexLightBlock.LIT) ? 13 : 0)));

    // ── Walkie talkie: stands on the floor or hangs on a wall; Carry On cannot move it (forge:immovable) ──
    public static final RegistryObject<Block> WALKIE_TALKIE = block("walkie_talkie",
            () -> new WalkieTalkieBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(0.5F).sound(SoundType.METAL).noOcclusion()));

    // ── Hat: placeable prop that can also be worn on the head ─────────────────
    public static final RegistryObject<Block> SAWTOOTH_HAT = block("sawtooth_hat",
            () -> new SawtoothHatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
                    .strength(0.8F).sound(SoundType.WOOL).noOcclusion()),
            b -> new net.kiko.item.HatBlockItem(b, new Item.Properties()));

    // ── Mysterious flower: a placeable flower (never generated) with its flower-pot version ──
    public static final RegistryObject<Block> MYSTERIOUS_FLOWER = block("mysterious_flower",
            () -> new MysteriousFlowerBlock(BlockBehaviour.Properties.copy(Blocks.POPPY)));

    /** The pot with the flower in it. Has no item of its own. */
    public static final RegistryObject<Block> POTTED_MYSTERIOUS_FLOWER = BLOCKS.register("potted_mysterious_flower",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, MYSTERIOUS_FLOWER,
                    BlockBehaviour.Properties.copy(Blocks.POTTED_POPPY)));

    /** Lets the vanilla flower pot accept the flower. Call from FMLCommonSetupEvent.enqueueWork. */
    public static void registerPottedPlants() {
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(MYSTERIOUS_FLOWER.getId(), POTTED_MYSTERIOUS_FLOWER);
    }

    // ── Sawtooth props (decorative, directional, no sounds) ───────────────────
    public static final RegistryObject<Block> SAWTOOTH_BEARTRAP = block("sawtooth_beartrap",
            () -> new ScpPropBlock(propProperties(), PropShapes.BEARTRAP));

    /** A flat blood puddle: you walk through it. */
    public static final RegistryObject<Block> SAWTOOTH_BLOOD = block("sawtooth_blood",
            () -> new ScpPropBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                    .noCollission().noOcclusion().instabreak().sound(SoundType.WOOL), PropShapes.BLOOD));

    public static final RegistryObject<Block> SAWTOOTH_DEER = block("sawtooth_deer",
            () -> new ScpPropBlock(propProperties(), PropShapes.DEER));

    public static final RegistryObject<Block> SAWTOOTH_CORPSE = block("sawtooth_corpse",
            () -> new ScpPropBlock(propProperties(), PropShapes.CORPSE));

    public static final RegistryObject<Block> SAWTOOTH_SIGN = block("sawtooth_sign",
            () -> new ScpPropBlock(propProperties(), PropShapes.SIGN));

    public static final RegistryObject<Block> SAWTOOTH_TV = block("sawtooth_tv",
            () -> new ScpPropBlock(propProperties(), PropShapes.TV));

    /** Spruce fence gate: same behaviour, silent. */
    public static final RegistryObject<Block> SAWTOOTH_CAMERA = block("sawtooth_camera",
            () -> new ScpCameraBlock(BlockBehaviour.Properties.copy(Blocks.SPRUCE_FENCE_GATE), WoodType.SPRUCE));

    /** Magenta wool. */
    public static final RegistryObject<Block> CARDBOARD_BOX = block("cardboard_box",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MAGENTA_WOOL)));

    // ── Sonic screwdriver light (invisible, no item) ───────────────────────────
    public static final RegistryObject<Block> SONIC_LIGHT = BLOCKS.register("sonic_light",
            () -> new SonicLightBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .noCollission().noOcclusion().replaceable().noLootTable()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .lightLevel(state -> 13)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T extends Block> RegistryObject<T> block(String name, Supplier<T> supplier) {
        return block(name, supplier, b -> new BlockItem(b, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> block(String name, Supplier<T> supplier,
                                                              Function<Block, Item> itemFactory) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        BLOCK_ITEMS.register(name, () -> itemFactory.apply(block.get()));
        return block;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
