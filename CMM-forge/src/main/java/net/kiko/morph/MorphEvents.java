package net.kiko.morph;

import net.kiko.KikoMod;
import net.kiko.network.ModNetwork;
import net.kiko.network.MorphS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/** Server side of the Morph wand: the chosen mob type lives in the player's persistent data and is sent to the clients. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorphEvents {

    private static final String TAG = "kiko_morph";

    private MorphEvents() {}

    /** Registry id of the mob the player is morphed into, or "" when not morphed. */
    public static String get(ServerPlayer player) {
        return player.getPersistentData().getString(TAG);
    }

    /** A baby target is stored as "id#baby" so the morph is drawn as the baby variant. */
    public static void set(ServerPlayer player, net.minecraft.world.entity.LivingEntity target) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (key == null) return;
        apply(player, key + (target.isBaby() ? "#baby" : ""));
    }

    public static void clear(ServerPlayer player) {
        apply(player, "");
    }

    private static void apply(ServerPlayer player, String id) {
        if (id.isEmpty()) player.getPersistentData().remove(TAG);
        else player.getPersistentData().putString(TAG, id);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new MorphS2CPacket(player.getUUID(), id));
    }

    /**
     * Runs before the mob's own right-click behaviour, so the wand also works on villagers, llamas, horses and the like
     * (otherwise their trade screen / saddle / feeding would take the click first).
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getItemStack().getItem() instanceof net.kiko.item.MorphWandItem)) return;
        if (event.getTarget() instanceof net.minecraft.world.entity.player.Player
                || !(event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target)) return;
        if (event.getEntity() instanceof ServerPlayer sp) {
            set(sp, target);
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.kiko.morphed",
                    target.getType().getDescription()), true);
            sp.level().playSound(null, sp.blockPosition(), net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !get(player).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new MorphS2CPacket(player.getUUID(), get(player)));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer
                && !get(target).isEmpty()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                    new MorphS2CPacket(target.getUUID(), get(target)));
        }
    }
}
