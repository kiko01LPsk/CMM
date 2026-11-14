package net.kiko.client.screen;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

/** A plain RGBA texture that video frames are copied into. */
public final class VideoTexture extends AbstractTexture {

    private final int width;
    private final int height;

    public VideoTexture(int width, int height) {
        this.width = width;
        this.height = height;
        RenderSystem.assertOnRenderThreadOrInit();
        TextureUtil.prepareImage(getId(), width, height);
        setFilter(true, false);
    }

    public void upload(ByteBuffer rgba) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.bindTexture(getId());
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rgba);
    }

    /** Replaces a rectangle of the picture (used for the buffering animation). */
    public void uploadRegion(int x, int y, int w, int h, ByteBuffer rgba) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.bindTexture(getId());
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rgba);
    }

    /** Fills the whole picture with opaque black. */
    public void clearBlack() {
        ByteBuffer b = ByteBuffer.allocateDirect(width * height * 4);
        for (int i = 3; i < b.capacity(); i += 4) b.put(i, (byte) 255);
        b.position(0);
        upload(b);
    }

    @Override
    public void load(ResourceManager manager) {}
}
