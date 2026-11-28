package net.kiko.client.morph;

import com.mojang.blaze3d.platform.NativeImage;
import net.kiko.network.ModNetwork;
import net.kiko.network.MorphSkinC2SPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Pick a PNG from the "kiko_skins" folder in the game directory and use it as the skin of your morph. */
public class MorphSkinScreen extends Screen {
    private static final int PER_PAGE = 6;

    private final File dir;
    private List<File> files = new ArrayList<>();
    private int page = 0;
    private Component status = Component.empty();

    public MorphSkinScreen() {
        super(Component.translatable("screen.kiko.morph_skin"));
        this.dir = new File(Minecraft.getInstance().gameDirectory, "kiko_skins");
    }

    @Override
    protected void init() {
        dir.mkdirs();
        File[] list = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        files = list == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(list));
        files.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
        int cx = width / 2;
        int y = 50;
        int from = page * PER_PAGE;
        for (int i = from; i < Math.min(files.size(), from + PER_PAGE); i++) {
            File f = files.get(i);
            addRenderableWidget(Button.builder(Component.literal(f.getName()), b -> choose(f))
                    .bounds(cx - 100, y, 200, 20).build());
            y += 24;
        }
        int bottom = height - 56;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); })
                .bounds(cx - 100, bottom, 20, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); })
                .bounds(cx + 80, bottom, 20, 20).build()).active = from + PER_PAGE < files.size();
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.morph_skin.open_folder"),
                b -> Util.getPlatform().openFile(dir)).bounds(cx - 76, bottom, 152, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.kiko.morph_skin.reset"), b -> {
            ModNetwork.CHANNEL.sendToServer(new MorphSkinC2SPacket(new byte[0]));
            onClose();
        }).bounds(cx - 100, bottom + 24, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(cx + 2, bottom + 24, 98, 20).build());
    }

    private void choose(File file) {
        try {
            byte[] raw = Files.readAllBytes(file.toPath());
            byte[] png;
            try (NativeImage img = NativeImage.read(new ByteArrayInputStream(raw))) {
                png = img.asByteArray(); // clean PNG
            }
            if (png.length > MorphSkinC2SPacket.MAX_BYTES) {
                status = Component.translatable("screen.kiko.morph_skin.too_big");
                return;
            }
            ModNetwork.CHANNEL.sendToServer(new MorphSkinC2SPacket(png));
            onClose();
        } catch (IOException | RuntimeException e) {
            status = Component.translatable("screen.kiko.morph_skin.bad_file");
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        if (files.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("screen.kiko.morph_skin.empty"), width / 2, 70, 0xAAAAAA);
        }
        g.drawCenteredString(font, status, width / 2, height - 70, 0xFF5555);
        super.render(g, mouseX, mouseY, partialTick);
    }
}
