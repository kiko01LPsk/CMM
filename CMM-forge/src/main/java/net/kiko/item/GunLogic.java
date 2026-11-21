package net.kiko.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Shared shooting / reloading for every firearm item (hitscan). */
public final class GunLogic {

    private GunLogic() {}

    public static int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("Ammo");
    }

    private static void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt("Ammo", ammo);
    }

    /** Called on right-click. Returns nothing; the caller raises the arm. */
    public static void onUse(Level level, Player player, ItemStack stack, Item item, GunStats stats) {
        if (level.isClientSide) return;
        if (player.isShiftKeyDown()) {
            reload(level, player, stack, stats);
        } else {
            tryFire((ServerLevel) level, player, stack, item, stats);
        }
    }

    /** Called every tick while right-click is held (automatic guns). */
    public static void onUseTick(Level level, Player player, ItemStack stack, Item item, GunStats stats) {
        if (level.isClientSide || !stats.auto() || player.isShiftKeyDown()) return;
        tryFire((ServerLevel) level, player, stack, item, stats);
    }

    private static void tryFire(ServerLevel level, Player player, ItemStack stack, Item item, GunStats stats) {
        if (player.getCooldowns().isOnCooldown(item)) return;
        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 1.6F);
            player.getCooldowns().addCooldown(item, 8);
            return;
        }
        if (!player.getAbilities().instabuild) setAmmo(stack, ammo - 1);
        fire(level, player, stats);
        player.getCooldowns().addCooldown(item, stats.cooldown());
    }

    private static void reload(Level level, Player player, ItemStack stack, GunStats stats) {
        int ammo = getAmmo(stack);
        int missing = stats.capacity() - ammo;
        if (missing <= 0) return;
        int loaded = 0;
        if (player.getAbilities().instabuild) {
            loaded = missing;
        } else {
            for (ItemStack s : player.getInventory().items) {
                if (loaded >= missing) break;
                if (s.is(Items.IRON_NUGGET)) {
                    int take = Math.min(s.getCount(), missing - loaded);
                    s.shrink(take);
                    loaded += take;
                }
            }
        }
        if (loaded > 0) {
            setAmmo(stack, ammo + loaded);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 1.3F);
            player.displayClientMessage(Component.literal("Ammo: " + (ammo + loaded) + "/" + stats.capacity()), true);
        } else {
            player.displayClientMessage(Component.literal("No iron nuggets").withStyle(ChatFormatting.RED), true);
        }
    }

    private static void fire(ServerLevel level, Player player, GunStats stats) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        Vec3 muzzle = eye.add(look.scale(0.8D));
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.15D, muzzle.z, 4, 0.03D, 0.03D, 0.03D, 0.01D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 2.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, stats.rocket() ? 0.5F : 0.7F);

        for (int i = 0; i < stats.pellets(); i++) {
            Vec3 dir = look;
            if (stats.spread() > 0) {
                dir = look.add(player.getRandom().nextGaussian() * stats.spread(),
                        player.getRandom().nextGaussian() * stats.spread(),
                        player.getRandom().nextGaussian() * stats.spread()).normalize();
            }
            shoot(level, player, eye, dir, stats);
        }
    }

    private static void shoot(ServerLevel level, Player player, Vec3 eye, Vec3 dir, GunStats stats) {
        Vec3 end = eye.add(dir.scale(stats.range()));
        HitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();

        AABB box = player.getBoundingBox().expandTowards(dir.scale(stats.range())).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, eye, end, box,
                e -> !e.isSpectator() && e.isPickable());

        Vec3 impact = end;
        if (hit != null) {
            impact = hit.getLocation();
            Entity target = hit.getEntity();
            if (!stats.rocket()) {
                target.invulnerableTime = 0; // so shotgun pellets and fast guns all count
                target.hurt(level.damageSources().playerAttack(player), stats.damage());
                level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 4, 0.1D, 0.1D, 0.1D, 0.1D);
            }
        } else if (!stats.rocket()) {
            level.sendParticles(ParticleTypes.POOF, impact.x, impact.y, impact.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
        }
        if (stats.rocket()) {
            level.explode(player, impact.x, impact.y, impact.z, 2.5F, Level.ExplosionInteraction.NONE);
        }
    }

    public static void addTooltip(ItemStack stack, GunStats stats, List<Component> tooltip) {
        if (stats == null) return;
        tooltip.add(Component.literal("Ammo: " + getAmmo(stack) + "/" + stats.capacity()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak + right-click: reload (iron nuggets)").withStyle(ChatFormatting.DARK_GRAY));
    }
}
