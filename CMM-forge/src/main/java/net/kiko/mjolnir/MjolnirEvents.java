package net.kiko.mjolnir;

import com.mojang.brigadier.CommandDispatcher;
import net.kiko.KikoMod;
import net.kiko.item.MjolnirItem;
import net.kiko.item.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MjolnirEvents {

    private MjolnirEvents() {}

    /** The unworthy can not pick it up in any way. */
    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        if (event.getItem().getItem().is(ModItems.MJOLNIR.get()) && !MjolnirSaved.isWorthy(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** No fall damage during and just after a Mjolnir flight. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && MjolnirFlight.recentlyFlew(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (!MjolnirFlight.isFlying(player)) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof MjolnirItem) || !MjolnirItem.isFlying(held)
                || player.isSpectator() || !player.isAlive() || player.isInWater()) {
            MjolnirFlight.stop(player);
        } else {
            player.fallDistance = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MjolnirFlight.forget(player);
        }
    }

    // ── /mjolnir worthy <player>  and  /mjolnir unworthy <player> ──────────────
    // Only a worthy player can use it.

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("mjolnir")
                .requires(src -> src.getEntity() instanceof ServerPlayer p && MjolnirSaved.isWorthy(p))
                .then(Commands.literal("worthy")
                        .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            MjolnirSaved data = MjolnirSaved.get(ctx.getSource().getServer());
                            boolean changed = data.setWorthy(target.getUUID(), true);
                            String name = target.getGameProfile().getName();
                            if (changed) {
                                ctx.getSource().sendSuccess(() -> Component.literal(name + " is now worthy."), true);
                                target.displayClientMessage(Component.literal("You have been found worthy."), false);
                                target.level().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                                        SoundSource.PLAYERS, 0.6F, 1.2F);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal(name + " is already worthy."), false);
                            }
                            return 1;
                        })))
                .then(Commands.literal("unworthy")
                        .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            String name = target.getGameProfile().getName();
                            if (MjolnirSaved.isOwner(target)) {
                                ctx.getSource().sendFailure(Component.literal(name + " can never be unworthy."));
                                return 0;
                            }
                            MjolnirSaved data = MjolnirSaved.get(ctx.getSource().getServer());
                            if (data.setWorthy(target.getUUID(), false)) {
                                ctx.getSource().sendSuccess(() -> Component.literal(name + " is no longer worthy."), true);
                                target.displayClientMessage(Component.literal("You are no longer worthy."), false);
                            } else {
                                ctx.getSource().sendFailure(Component.literal(name + " was not worthy."));
                            }
                            return 1;
                        }))));
    }
}
