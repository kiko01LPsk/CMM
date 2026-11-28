package net.kiko.client.phone;

import net.kiko.KikoMod;
import net.kiko.client.PoseBindings;
import net.kiko.item.PhoneItem;
import net.kiko.pose.PoseKind;
import net.kiko.pose.PoseState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client side of the phone: opens the screen and raises the arm that holds it. */
@Mod.EventBusSubscriber(modid = KikoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PhoneClient {

    private static InteractionHand hand = InteractionHand.MAIN_HAND;

    private PhoneClient() {}

    public static void open(InteractionHand usedHand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        hand = usedHand;
        mc.setScreen(new PhoneScreen(usedHand));
    }

    /** Raises the arm holding the phone in the given pose (NONE lowers it). */
    public static void setPose(InteractionHand usedHand, PoseKind kind) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        hand = usedHand;
        if (kind == PoseKind.NONE) {
            PoseBindings.setPhonePose(PoseState.NONE);
            return;
        }
        HumanoidArm arm = usedHand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        PoseBindings.setPhonePose(arm == HumanoidArm.RIGHT ? new PoseState(kind, PoseKind.NONE) : new PoseState(PoseKind.NONE, kind));
    }

    /** Putting the phone away (or switching items) lowers the arm again. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (PoseBindings.currentPhonePose().isNone()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getItemInHand(hand).getItem() instanceof PhoneItem)) {
            PoseBindings.setPhonePose(PoseState.NONE);
        }
    }
}
