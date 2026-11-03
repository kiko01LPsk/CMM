package net.kiko.mannequin;

import net.kiko.KikoMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MannequinEvents {

    private MannequinEvents() {}

    /** Same attributes as a vanilla armor stand. */
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MANNEQUIN.get(), LivingEntity.createLivingAttributes().build());
    }
}
