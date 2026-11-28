package net.kiko.network;

import net.kiko.KikoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KikoMod.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(PoseC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PoseC2SPacket::new)
                .encoder(PoseC2SPacket::toBytes)
                .consumerMainThread(PoseC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(PoseS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PoseS2CPacket::new)
                .encoder(PoseS2CPacket::toBytes)
                .consumerMainThread(PoseS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(MannequinEditC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MannequinEditC2SPacket::new)
                .encoder(MannequinEditC2SPacket::toBytes)
                .consumerMainThread(MannequinEditC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(MannequinSkinC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MannequinSkinC2SPacket::new)
                .encoder(MannequinSkinC2SPacket::toBytes)
                .consumerMainThread(MannequinSkinC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(MannequinModeC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MannequinModeC2SPacket::new)
                .encoder(MannequinModeC2SPacket::toBytes)
                .consumerMainThread(MannequinModeC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(MjolnirActionC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MjolnirActionC2SPacket::new)
                .encoder(MjolnirActionC2SPacket::toBytes)
                .consumerMainThread(MjolnirActionC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(ShieldThrowC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ShieldThrowC2SPacket::new)
                .encoder(ShieldThrowC2SPacket::toBytes)
                .consumerMainThread(ShieldThrowC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SonicSunglassesC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(SonicSunglassesC2SPacket::new)
                .encoder(SonicSunglassesC2SPacket::toBytes)
                .consumerMainThread(SonicSunglassesC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(GuitarBackS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GuitarBackS2CPacket::new)
                .encoder(GuitarBackS2CPacket::toBytes)
                .consumerMainThread(GuitarBackS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(MannequinModelC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MannequinModelC2SPacket::new)
                .encoder(MannequinModelC2SPacket::toBytes)
                .consumerMainThread(MannequinModelC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(MannequinModeS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MannequinModeS2CPacket::new)
                .encoder(MannequinModeS2CPacket::toBytes)
                .consumerMainThread(MannequinModeS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(MonitorSetC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonitorSetC2SPacket::new)
                .encoder(MonitorSetC2SPacket::toBytes)
                .consumerMainThread(MonitorSetC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(MonitorReadyC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonitorReadyC2SPacket::new)
                .encoder(MonitorReadyC2SPacket::toBytes)
                .consumerMainThread(MonitorReadyC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(MorphS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MorphS2CPacket::new)
                .encoder(MorphS2CPacket::toBytes)
                .consumerMainThread(MorphS2CPacket::handle)
                .add();
        CHANNEL.messageBuilder(MorphSkinS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MorphSkinS2CPacket::new)
                .encoder(MorphSkinS2CPacket::toBytes)
                .consumerMainThread(MorphSkinS2CPacket::handle)
                .add();
        CHANNEL.messageBuilder(MorphSkinC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MorphSkinC2SPacket::new)
                .encoder(MorphSkinC2SPacket::toBytes)
                .consumerMainThread(MorphSkinC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(MorphResetC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(MorphResetC2SPacket::new)
                .encoder(MorphResetC2SPacket::toBytes)
                .consumerMainThread(MorphResetC2SPacket::handle)
                .add();
    }
}
