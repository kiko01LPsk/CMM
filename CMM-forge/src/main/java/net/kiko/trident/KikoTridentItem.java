package net.kiko.trident;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A trident with a different look (the 3D trident and spear models). Same stats, enchantments,
 * throwing, loyalty, riptide and channeling as the vanilla trident; it throws {@link ThrownKikoTrident}
 * so the thrown weapon keeps its own model and the item that comes back.
 */
public class KikoTridentItem extends TridentItem {

    public KikoTridentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        int used = getUseDuration(stack) - timeLeft;
        if (used < 10) return;
        int riptide = EnchantmentHelper.getRiptide(stack);
        if (riptide > 0 && !player.isInWaterOrRain()) return;

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(entity.getUsedItemHand()));
            if (riptide == 0) {
                ThrownKikoTrident thrown = new ThrownKikoTrident(level, player, stack);
                thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                if (player.getAbilities().instabuild) {
                    thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.addFreshEntity(thrown);
                level.playSound(null, thrown, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (riptide > 0) {
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            float x = -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
            float y = -Mth.sin(pitch * 0.017453292F);
            float z = Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
            float len = Mth.sqrt(x * x + y * y + z * z);
            float power = 3.0F * ((1.0F + (float) riptide) / 4.0F);
            player.push(x * power / len, y * power / len, z * power / len);
            player.startAutoSpinAttack(20);
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
            }
            SoundEvent sound = riptide >= 3 ? SoundEvents.TRIDENT_RIPTIDE_3
                    : riptide == 2 ? SoundEvents.TRIDENT_RIPTIDE_2 : SoundEvents.TRIDENT_RIPTIDE_1;
            level.playSound(null, player, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
