package net.kiko.client.mjolnir;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.kiko.KikoMod;
import net.kiko.client.PoseBindings;
import net.kiko.item.MjolnirItem;
import net.kiko.network.MjolnirActionC2SPacket;
import net.kiko.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/**
 * Client side of Mjolnir: the call key, the flight movement (your own client moves you,
 * the server only starts and stops it) and tilting the flying player's body.
 */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MjolnirClient {

    public static final KeyMapping CALL = new KeyMapping("key.kiko.mjolnir_call",
            InputConstants.UNKNOWN.getValue(), PoseBindings.CATEGORY);

    /** Unbound by default: strikes lightning where you look while holding the hammer. */
    public static final KeyMapping LIGHTNING = new KeyMapping("key.kiko.mjolnir_lightning",
            InputConstants.UNKNOWN.getValue(), PoseBindings.CATEGORY);

    /** Blocks per tick while flying. */
    private static final double SPEED = 1.5D;

    private static int flightTicks = 0;
    private static int startCooldown = 0;
    private static final Set<Integer> LEANING = new HashSet<>();
    private static final java.util.Map<Integer, MjolnirSpinSound> SPIN_SOUNDS = new java.util.HashMap<>();

    private static final Set<java.util.UUID> CALLING = new HashSet<>();

    private MjolnirClient() {}

    /** True while a called hammer is still on its way to this player. */
    public static boolean isCalling(Player player) {
        return CALLING.contains(player.getUUID());
    }

    private static void updateCalling(Minecraft mc) {
        CALLING.clear();
        if (mc.level == null) return;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof net.kiko.mjolnir.ThrownMjolnir hammer && hammer.isCalled() && hammer.getOwner() != null) {
                CALLING.add(hammer.getOwner().getUUID());
            }
            // a called Godspear: the owner raises the arm the same way
            if (e instanceof net.kiko.trident.ThrownKikoTrident spear && spear.isGodspear() && spear.isRecalled() && spear.getOwner() != null) {
                CALLING.add(spear.getOwner().getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;

        updateSpinSounds(mc);
        updateCalling(mc);

        if (startCooldown > 0) startCooldown--;
        if (CALL.consumeClick() && mc.screen == null) {
            ModNetwork.CHANNEL.sendToServer(new MjolnirActionC2SPacket(MjolnirActionC2SPacket.CALL));
        }

        if (LIGHTNING.consumeClick() && mc.screen == null) {
            ModNetwork.CHANNEL.sendToServer(new MjolnirActionC2SPacket(MjolnirActionC2SPacket.LIGHTNING));
        }

        ItemStack held = player.getMainHandItem();
        boolean mjolnir = held.getItem() instanceof MjolnirItem;

        if (mjolnir && MjolnirItem.isFlying(held)) {
            flyTick(player);
        } else {
            flightTicks = 0;
            if (mjolnir && MjolnirItem.isSpinning(held) && startCooldown == 0
                    && player.onGround() && mc.options.keyJump.isDown() && mc.screen == null) {
                startCooldown = 20;
                ModNetwork.CHANNEL.sendToServer(new MjolnirActionC2SPacket(MjolnirActionC2SPacket.START_FLIGHT));
            }
        }
    }

    private static void updateSpinSounds(Minecraft mc) {
        if (mc.level == null) return;
        SPIN_SOUNDS.keySet().removeIf(id -> mc.level.getEntity(id) == null);
        for (Player p : mc.level.players()) {
            MjolnirSpinSound sound = SPIN_SOUNDS.get(p.getId());
            if ((sound == null || sound.isStopped()) && MjolnirSpinSound.shouldPlay(p)) {
                sound = new MjolnirSpinSound(p);
                SPIN_SOUNDS.put(p.getId(), sound);
                mc.getSoundManager().play(sound);
            }
        }
    }

    private static void flyTick(LocalPlayer player) {
        flightTicks++;
        // Sneak ends the flight, but not in the first moments (you may still be holding it from toggling the spin).
        boolean end = (flightTicks > 15 && player.isShiftKeyDown())
                || player.isInWater()
                || (flightTicks > 3 && player.horizontalCollision)
                || (flightTicks > 10 && player.onGround());
        if (end) {
            ModNetwork.CHANNEL.sendToServer(new MjolnirActionC2SPacket(MjolnirActionC2SPacket.STOP_FLIGHT));
            startCooldown = 10;
            return;
        }

        double ramp = Math.min(1.0D, flightTicks / 6.0D);
        Vec3 want = player.getLookAngle().scale(SPEED * ramp);
        if (flightTicks < 12) want = want.add(0.0D, 0.35D, 0.0D);
        // Undo the air drag and gravity the next movement tick applies, so the flight speed stays even.
        double drag = flightTicks < 6 ? 0.6D : 0.91D;
        player.setDeltaMovement(want.x / drag, want.y / 0.98D + 0.08D, want.z / drag);
        player.fallDistance = 0.0F;
        player.getAbilities().flying = false;
    }

    // ── lean the body forward while flying ─────────────────────────────────────

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof MjolnirItem) || !MjolnirItem.isFlying(held)) return;

        float partial = event.getPartialTick();
        float pitch = Mth.lerp(partial, player.xRotO, player.getXRot());
        float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        float lean = Mth.clamp(90.0F + pitch, 0.0F, 135.0F);
        float a = 180.0F - bodyYaw;

        var pose = event.getPoseStack();
        pose.pushPose();
        LEANING.add(player.getId());
        pose.translate(0.0D, 0.9D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(a));
        pose.mulPose(Axis.XP.rotationDegrees(-lean));
        pose.mulPose(Axis.YP.rotationDegrees(-a));
        pose.translate(0.0D, -0.9D, 0.0D);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (LEANING.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }
}
