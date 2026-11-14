package net.kiko.item;

import net.kiko.mannequin.ModEntities;
import net.kiko.wheelchair.WheelchairEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Places a wheelchair where you click, facing the way you look. */
public class WheelchairItem extends Item {

    public WheelchairItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        Direction face = context.getClickedFace();
        Vec3 at = context.getClickLocation().add(face.getStepX() * 0.05, face.getStepY() * 0.05, face.getStepZ() * 0.05);
        WheelchairEntity chair = ModEntities.WHEELCHAIR.get().create(level);
        if (chair == null) return InteractionResult.FAIL;
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() : 0.0F;
        chair.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        level.addFreshEntity(chair);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
