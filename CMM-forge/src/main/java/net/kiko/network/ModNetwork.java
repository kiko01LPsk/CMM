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

        CHANNEL.messageBuilder(MannequinModeS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MannequinModeS2CPacket::new)
                .encoder(MannequinModeS2CPacket::toBytes)
                .consumerMainThread(MannequinModeS2CPacket::handle)
                .add();
    }
}
