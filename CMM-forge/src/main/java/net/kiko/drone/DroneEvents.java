package net.kiko.drone;

import net.kiko.KikoMod;
import net.kiko.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lets the player fly while the drone is on their head, and takes the flight away again when it is removed. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DroneEvents {

    private static final String FLAG = "KikoDroneFlight";

    private DroneEvents() {}

    public static boolean isWearing(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.DRONE.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        CompoundTag data = player.getPersistentData();
        if (isWearing(player)) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            data.putBoolean(FLAG, true);
        } else if (data.getBoolean(FLAG)) {
            data.putBoolean(FLAG, false);
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
