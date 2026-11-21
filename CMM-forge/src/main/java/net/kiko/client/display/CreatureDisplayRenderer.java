package net.kiko.client.display;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.block.CreatureDisplayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/** Draws the creature of the spawn egg inside the creature display case, standing still and turning slowly. */
public class CreatureDisplayRenderer implements BlockEntityRenderer<CreatureDisplayBlockEntity> {
    private Entity cached;
    private EntityType<?> cachedType;
    private net.minecraft.world.level.Level cachedLevel;

    public CreatureDisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CreatureDisplayBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        ItemStack egg = be.getEgg();
        if (be.getLevel() == null || !(egg.getItem() instanceof SpawnEggItem spawnEgg)) return;
        try {
            EntityType<?> type = spawnEgg.getType(egg.getTag());
            if (type == null) return;
            if (cached == null || cachedType != type || cachedLevel != be.getLevel()) {
                cached = type.create(be.getLevel());
                cachedType = type;
                cachedLevel = be.getLevel();
            }
            if (cached == null) return;
            cached.setPos(be.getBlockPos().getX() + 1.0, be.getBlockPos().getY() + 1.0, be.getBlockPos().getZ() + 1.0);
            float size = Math.max(cached.getBbHeight(), cached.getBbWidth());
            float scale = Math.max(0.3F, Math.min(1.5F, 1.7F / size));
            float yaw = (be.getLevel().getGameTime() + partialTick) * 0.8F;
            int lit = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
            var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            poseStack.pushPose();
            poseStack.translate(1.0, 1.05, 1.0);
            poseStack.scale(scale, scale, scale);
            dispatcher.setRenderShadow(false);
            dispatcher.render(cached, 0.0, 0.0, 0.0, yaw, partialTick, poseStack, buffer, lit);
            dispatcher.setRenderShadow(true);
            poseStack.popPose();
        } catch (Exception e) {
            cached = null; // some creatures cannot be drawn without a real world; skip them
        }
    }
}
