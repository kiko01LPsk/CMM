package net.kiko.mjolnir;

import net.kiko.item.ModItems;
import net.kiko.item.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** "Call Mjolnir": the closest lying or standing hammer flies to the caller. */
public final class MjolnirCall {

    public static final double RANGE = 128.0D;

    private MjolnirCall() {}

    public static void call(ServerPlayer player) {
        if (!MjolnirSaved.isWorthy(player)) return;
        // Already has one.
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ModItems.MJOLNIR.get())) return;
        }
        if (player.getOffhandItem().is(ModItems.MJOLNIR.get())) return;

        ServerLevel level = player.serverLevel();
        Vec3 here = player.position();

        // 1. a dropped hammer
        ItemEntity bestItem = null;
        double bestItemDist = Double.MAX_VALUE;
        AABB box = player.getBoundingBox().inflate(RANGE);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box,
                e -> e.getItem().is(ModItems.MJOLNIR.get()));
        for (ItemEntity e : items) {
            double d = e.distanceToSqr(here);
            if (d < bestItemDist) {
                bestItemDist = d;
                bestItem = e;
            }
        }

        // 2. a hammer standing in the ground
        BlockPos bestBlock = null;
        double bestBlockDist = Double.MAX_VALUE;
        for (GlobalPos g : MjolnirSaved.get(player.server).placed()) {
            if (!g.dimension().equals(level.dimension())) continue;
            double d = g.pos().distToCenterSqr(here);
            if (d < bestBlockDist) {
                bestBlockDist = d;
                bestBlock = g.pos();
            }
        }

        double limit = RANGE * RANGE;
        boolean useItem = bestItem != null && bestItemDist <= limit && bestItemDist <= bestBlockDist;
        boolean useBlock = !useItem && bestBlock != null && bestBlockDist <= limit && level.isLoaded(bestBlock);

        Vec3 from;
        if (useItem) {
            from = bestItem.position();
            bestItem.discard();
        } else if (useBlock) {
            from = Vec3.atCenterOf(bestBlock).add(0.0D, 0.6D, 0.0D);
            level.removeBlock(bestBlock, false);
        } else {
            player.displayClientMessage(Component.literal("Nothing answers your call."), true);
            return;
        }

        ThrownMjolnir hammer = new ThrownMjolnir(level, player, new ItemStack(ModItems.MJOLNIR.get()));
        hammer.setPos(from);
        hammer.startReturn();
        level.addFreshEntity(hammer);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(from);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, from.x, from.y, from.z, ModSounds.MJOLNIR_SWING.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4F, 1.4F);
    }
}
