package net.kiko.block;

import net.kiko.KikoMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
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

    // ── Doors and trapdoor (behave like their wooden vanilla versions) ────────
    // Doors use DoubleHighBlockItem so both halves are placed properly.
    public static final RegistryObject<Block> SCP_DOOR_1 = block("scp_door_1",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_DOOR), BlockSetType.BIRCH),
            b -> new DoubleHighBlockItem(b, new Item.Properties()));

    public static final RegistryObject<Block> SCP_DOOR_2 = block("scp_door_2",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_DOOR), BlockSetType.DARK_OAK),
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

    // ── Hat: placeable prop that can also be worn on the head ─────────────────
    public static final RegistryObject<Block> SAWTOOTH_HAT = block("sawtooth_hat",
            () -> new SawtoothHatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
                    .strength(0.8F).sound(SoundType.WOOL).noOcclusion()),
            b -> new net.kiko.item.HatBlockItem(b, new Item.Properties()));

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
