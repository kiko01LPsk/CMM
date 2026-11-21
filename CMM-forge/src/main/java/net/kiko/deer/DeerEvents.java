package net.kiko.deer;

import net.kiko.KikoMod;
import net.kiko.mannequin.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DeerEvents {

    private DeerEvents() {}

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DEER.get(), DeerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.DEER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
