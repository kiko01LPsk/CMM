package net.kiko.client;

import net.kiko.KikoMod;
import net.kiko.client.mannequin.MannequinImitation;
import net.kiko.client.mannequin.MannequinRenderer;
import net.kiko.client.mjolnir.MjolnirBlockRenderer;
import net.kiko.client.walkman.WalkmanLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.kiko.client.mjolnir.MjolnirClient;
import net.kiko.client.mjolnir.ThrownMjolnirRenderer;
import net.kiko.block.ModBlocks;
import net.kiko.item.MjolnirItem;
import net.kiko.item.ModItems;
import net.kiko.item.SonicScrewdriverItem;
import net.kiko.mannequin.ModEntities;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        for (PoseBindings.Binding binding : PoseBindings.all()) {
            event.register(binding.key());
        }
        event.register(MannequinImitation.TOGGLE);
        event.register(MjolnirClient.CALL);
        event.register(MjolnirClient.LIGHTNING);
        event.register(net.kiko.client.sunglasses.SonicSunglassesClient.USE);
        event.register(net.kiko.client.morph.MorphRenderer.RESET);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(net.minecraft.world.entity.EntityType.PAINTING, TransparentPaintingRenderer::new);
        event.registerEntityRenderer(ModEntities.MANNEQUIN.get(), MannequinRenderer::new);
        event.registerEntityRenderer(ModEntities.DEER.get(), net.kiko.client.deer.DeerRenderer::new);
        event.registerEntityRenderer(ModEntities.KEYBOARD.get(), KeyboardRenderer::new);
        event.registerEntityRenderer(ModEntities.TRIPOD_CAMERA.get(), net.kiko.client.tripod.TripodCameraRenderer::new);
        event.registerEntityRenderer(ModEntities.WHEELCHAIR.get(), net.kiko.client.wheelchair.WheelchairRenderer::new);
        event.registerEntityRenderer(ModEntities.CARDBOARD_BOX.get(), net.kiko.client.cardboard.CardboardBoxRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_SHIELD.get(), net.kiko.client.shield.ThrownShieldRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_CHAIR.get(), net.kiko.client.chair.ThrownChairRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_MJOLNIR.get(), ThrownMjolnirRenderer::new);
        event.registerEntityRenderer(ModEntities.KIKO_TRIDENT.get(), net.kiko.client.trident.ThrownKikoTridentRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.ARMOR_DISPLAY_ENTITY.get(), net.kiko.client.display.ArmorDisplayRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.CREATURE_DISPLAY_ENTITY.get(), net.kiko.client.display.CreatureDisplayRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.DISPLAY_CASE_ENTITY.get(), net.kiko.client.display.DisplayCaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.MJOLNIR_BLOCK_ENTITY.get(), MjolnirBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.MONITOR_ENTITY.get(), net.kiko.client.screen.MonitorRenderer::new);
    }

    /** Blood dust textures are white and get their colour here (dark red / bright red). */
    @SubscribeEvent
    public static void onBlockColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> 0x8A0A0A, ModBlocks.BLOOD_DUST.get());
        event.register((state, level, pos, tint) -> 0xFF2A2A, ModBlocks.GLOWING_BLOOD_DUST.get());
    }

    @SubscribeEvent
    public static void onItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> 0x8A0A0A, ModBlocks.BLOOD_DUST.get().asItem());
        event.register((stack, tint) -> 0xFF2A2A, ModBlocks.GLOWING_BLOOD_DUST.get().asItem());
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        net.kiko.client.mannequin.MannequinLayers.register(event);
        event.registerLayerDefinition(net.kiko.client.deer.DeerModel.LAYER, net.kiko.client.deer.DeerModel::createLayer);
        event.registerLayerDefinition(net.kiko.client.drone.DroneModel.LAYER, net.kiko.client.drone.DroneModel::createLayer);
    }

    /** Adds the worn-walkman layer to both player skins (normal and slim arms). */
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new WalkmanLayer(renderer));
                renderer.addLayer(new net.kiko.client.guitar.GuitarBackLayer(renderer));
                renderer.addLayer(new net.kiko.client.shield.ShieldBackLayer(renderer));
                renderer.addLayer(new SunglassesLensLayer<>(renderer, event.getEntityModels()));
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.kiko.screen.video.VideoTools.init(net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath());
            ItemProperties.register(ModItems.CLOCK.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "time"),
                    ClientModEvents::clockTime);
            // Picks the colour model of the sonic screwdriver (see models/item/sonic_screwdriver.json).
            ItemProperties.register(ModItems.SONIC_SCREWDRIVER.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "mode"),
                    (stack, level, entity, seed) -> (float) SonicScrewdriverItem.getMode(stack).ordinal());
            // Mjolnir: 12 spin frames while spinning in the hand, one pose while flying (see models/item/mjolnir.json).
            ItemProperties.register(ModItems.MJOLNIR.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "spin"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || !MjolnirItem.isSpinning(stack) || MjolnirItem.isFlying(stack)) return 0.0F;
                        int frame = (int) ((net.minecraft.Util.getMillis() / 32L) % 12L);
                        return (frame + 1) / 12.0F;
                    });
            ItemProperties.register(ModItems.MJOLNIR.get(),
                    new ResourceLocation(KikoMod.MOD_ID, "flying"),
                    (stack, level, entity, seed) -> entity != null && MjolnirItem.isFlying(stack) ? 1.0F : 0.0F);
            // Tridents and spears: separate model while the throw is being charged (flipped to point forward).
            ItemProperties.register(ModItems.TRIDENT_DEFAULT.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.TRIDENT_IRON.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.TRIDENT_GOLD.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.TRIDENT_NETHERITE.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPEAR_DEFAULT.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPEAR_IRON.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPEAR_GOLD.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPEAR_NETHERITE.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.MJOLNIR.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.PLASTIC_CHAIR.get(), new ResourceLocation(KikoMod.MOD_ID, "throwing"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            // Captain shield: blocking pose, same as the vanilla shield.
            ItemProperties.register(ModItems.CAPTAIN_SHIELD.get(),
                    new ResourceLocation("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    /**
     * Value 0..1 that picks the clock frame (see models/item/clock.json).
     * It reads like a normal 12-hour clock face: the hand points at 12 at noon and midnight,
     * at 6 at sunrise (6:00) and sunset (18:00), moving one numeral per in-game hour.
     * In dimensions without a day/night cycle (Nether, End) the hand just spins.
     */
    private static float clockTime(ItemStack stack, ClientLevel clientLevel, LivingEntity entity, int seed) {
        Level level = clientLevel != null ? clientLevel : (entity != null ? entity.level() : null);
        if (level == null) return 0.0F;

        if (!level.dimensionType().natural()) {
            return (level.getGameTime() % 240L) / 240.0F;
        }
        // dayTime 0 = 6:00, so 6000 ticks (6 hours) after dayTime 0 the hand has to be at 12.
        return ((level.getDayTime() + 6000L) % 12000L) / 12000.0F;
    }
}
