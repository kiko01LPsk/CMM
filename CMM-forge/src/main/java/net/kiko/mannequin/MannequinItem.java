package net.kiko.mannequin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Places a mannequin the same way an armor stand is placed. */
public class MannequinItem extends Item {

    public MannequinItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) return InteractionResult.FAIL;

        Level level = context.getLevel();
        BlockPos pos = new BlockPlaceContext(context).getClickedPos();
        ItemStack stack = context.getItemInHand();
        Vec3 bottom = Vec3.atBottomCenterOf(pos);
        AABB box = ModEntities.MANNEQUIN.get().getDimensions().makeBoundingBox(bottom.x(), bottom.y(), bottom.z());
        if (!level.noCollision(null, box) || !level.getEntities(null, box).isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel server) {
            Mannequin mannequin = new Mannequin(server, bottom.x(), bottom.y(), bottom.z());
            float yaw = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            mannequin.moveTo(mannequin.getX(), mannequin.getY(), mannequin.getZ(), yaw, 0.0F);
            EntityType.updateCustomEntityTag(server, context.getPlayer(), mannequin, stack.getTag());
            server.addFreshEntityWithPassengers(mannequin);
            level.playSound(null, mannequin.getX(), mannequin.getY(), mannequin.getZ(),
                    SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            mannequin.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.kiko.mannequin.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
