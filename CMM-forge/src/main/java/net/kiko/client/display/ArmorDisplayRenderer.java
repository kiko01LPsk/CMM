package net.kiko.client.display;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.block.ArmorDisplayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

/** Shows the armor on an invisible armor stand figure that turns slowly. */
public class ArmorDisplayRenderer implements BlockEntityRenderer<ArmorDisplayBlockEntity> {
    private ArmorStand stand;

    public ArmorDisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArmorDisplayBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        if (be.getLevel() == null) return;
        boolean any = false;
        for (ItemStack s : be.getItems()) if (!s.isEmpty()) { any = true; break; }
        if (!any) return;
        if (stand == null || stand.level() != be.getLevel()) {
            stand = new ArmorStand(EntityType.ARMOR_STAND, be.getLevel());
            stand.setInvisible(true);
            stand.setNoBasePlate(true);
            stand.setShowArms(true);
        }
        stand.setItemSlot(EquipmentSlot.HEAD, be.get(ArmorDisplayBlockEntity.HEAD));
        stand.setItemSlot(EquipmentSlot.CHEST, be.get(ArmorDisplayBlockEntity.CHEST));
        stand.setItemSlot(EquipmentSlot.LEGS, be.get(ArmorDisplayBlockEntity.LEGS));
        stand.setItemSlot(EquipmentSlot.FEET, be.get(ArmorDisplayBlockEntity.FEET));
        stand.setItemSlot(EquipmentSlot.MAINHAND, be.get(ArmorDisplayBlockEntity.HAND));
        stand.setPos(be.getBlockPos().getX() + 0.5, be.getBlockPos().getY(), be.getBlockPos().getZ() + 0.5);
        float yaw = (be.getLevel().getGameTime() + partialTick) * 0.6F;
        int lit = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.13, 0.5);
        poseStack.scale(0.9F, 0.9F, 0.9F);
        dispatcher.setRenderShadow(false);
        dispatcher.render(stand, 0.0, 0.0, 0.0, yaw, partialTick, poseStack, buffer, lit);
        dispatcher.setRenderShadow(true);
        poseStack.popPose();
    }
}
