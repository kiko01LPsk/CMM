package net.kiko.deer;

import net.kiko.KikoMod;
import net.kiko.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Right-clicking the player Bambi2009521 with deer crackers feeds them: hearts, eating sound, the item is used up. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID)
public final class BambiEvents {
    private static final String BAMBI = "Bambi2009521";

    private BambiEvents() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return;
        if (!BAMBI.equalsIgnoreCase(target.getGameProfile().getName())) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.DEER_CRACKERS.get())) return;
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
        if (event.getLevel() instanceof ServerLevel level) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight() + 0.3,
                    target.getZ(), 7, 0.4, 0.3, 0.4, 0.02);
            if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);
        }
    }

    /** Bambi2009521 may not eat venison. */
    @SubscribeEvent
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!BAMBI.equalsIgnoreCase(player.getGameProfile().getName())) return;
        ItemStack stack = event.getItem();
        if (!stack.is(ModItems.VENISON.get()) && !stack.is(ModItems.COOKED_VENISON.get())) return;
        event.setCanceled(true);
        if (!player.level().isClientSide) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Cannibalism will not be tolerated"), false);
        }
    }
}
