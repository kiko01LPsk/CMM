package net.kiko.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Working revolver (experiment). 6-round cylinder stored in the stack's "Ammo" tag.
 * Right-click: fire (hitscan, 64 blocks, 8 damage, 0.5 s between shots).
 * Sneak + right-click: reload from iron nuggets in the inventory (creative: free).
 * Keeps the one-hand horn pose of OneHandGunItem while right-click is held.
 */
public class RevolverItem extends OneHandGunItem {

    public static final int CAPACITY = 6;
    private static final float DAMAGE = 8.0F;
    private static final double RANGE = 64.0D;
    private static final int COOLDOWN = 10;

    public RevolverItem(Properties properties) {
        super(properties);
    }

    public static int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("Ammo");
    }

    private static void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt("Ammo", ammo);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) reload(level, player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        // Raise the hand (horn pose) like the other prop guns.
        player.startUsingItem(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.consume(stack);
        }
        if (!level.isClientSide) {
            int ammo = getAmmo(stack);
            if (ammo <= 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 1.6F);
                player.getCooldowns().addCooldown(this, 6);
            } else {
                if (!player.getAbilities().instabuild) setAmmo(stack, ammo - 1);
                fire((ServerLevel) level, player);
                player.getCooldowns().addCooldown(this, COOLDOWN);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private void reload(Level level, Player player, ItemStack stack) {
        int ammo = getAmmo(stack);
        int missing = CAPACITY - ammo;
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
            player.displayClientMessage(Component.literal("Ammo: " + (ammo + loaded) + "/" + CAPACITY), true);
        } else {
            player.displayClientMessage(Component.literal("No iron nuggets").withStyle(ChatFormatting.RED), true);
        }
    }

    private void fire(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(RANGE));

        HitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, eye, end, box,
                e -> !e.isSpectator() && e.isPickable());

        Vec3 muzzle = eye.add(look.scale(0.8D));
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.15D, muzzle.z, 4, 0.03D, 0.03D, 0.03D, 0.01D);
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y - 0.15D, muzzle.z, 6, 0.04D, 0.04D, 0.04D, 0.02D);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 2.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, 0.7F);

        Vec3 impact = end;
        if (hit != null) {
            Entity target = hit.getEntity();
            impact = hit.getLocation();
            target.hurt(level.damageSources().playerAttack(player), DAMAGE);
            level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 6, 0.1D, 0.1D, 0.1D, 0.1D);
        } else {
            level.sendParticles(ParticleTypes.POOF, impact.x, impact.y, impact.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Ammo: " + getAmmo(stack) + "/" + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak + right-click: reload (iron nuggets)").withStyle(ChatFormatting.DARK_GRAY));
    }
}
