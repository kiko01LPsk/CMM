package net.kiko.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.kiko.block.MjolnirBlock;
import net.kiko.mjolnir.MjolnirFlight;
import net.kiko.mjolnir.MjolnirSaved;
import net.kiko.mjolnir.ThrownMjolnir;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Mjolnir. Only the worthy can lift it (kiko01LPsk, plus anyone made worthy with /mjolnir worthy).
 *
 *  - Drop it, or sneak + right-click the top of a block: it stands head-down in the ground (MjolnirBlock).
 *  - Hold right-click and release: throw it like a trident; it flies back to your hand.
 *  - Sneak + right-click in the air: start/stop spinning it in your hand.
 *  - While it spins, jump: it pulls you into the air (flight). Sneak to end the flight.
 *  - Key "Call Mjolnir": it flies to you from where it is lying.
 */
public class MjolnirItem extends Item {

    private static final String SPINNING = "Spinning";
    private static final String FLYING = "Flying";

    public MjolnirItem(Properties properties) {
        super(properties);
    }

    // ── state kept on the stack (read by the model predicates and the arm pose) ──

    public static boolean isSpinning(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(SPINNING);
    }

    public static boolean isFlying(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(FLYING);
    }

    public static void setSpinning(ItemStack stack, boolean value) {
        if (value) {
            stack.getOrCreateTag().putBoolean(SPINNING, true);
        } else if (stack.hasTag()) {
            stack.getTag().remove(SPINNING);
            if (stack.getTag().isEmpty()) stack.setTag(null);
        }
    }

    public static void setFlying(ItemStack stack, boolean value) {
        if (value) {
            stack.getOrCreateTag().putBoolean(FLYING, true);
        } else if (stack.hasTag()) {
            stack.getTag().remove(FLYING);
            if (stack.getTag().isEmpty()) stack.setTag(null);
        }
    }

    /** Hammer as it is stored inside a thrown hammer or a placed one: no spin, no flight. */
    public static ItemStack cleaned(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setTag(null);
        return copy;
    }

    public static void notWorthy(Player player) {
        if (player.level().isClientSide()) return;
        player.displayClientMessage(Component.literal("You are not worthy.").withStyle(ChatFormatting.GRAY), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(),
                SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    // ── using it ───────────────────────────────────────────────────────────────

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!MjolnirSaved.isWorthy(player)) {
            notWorthy(player);
            return InteractionResultHolder.fail(stack);
        }
        if (player.isShiftKeyDown()) {
            if (hand == InteractionHand.MAIN_HAND) {
                if (!level.isClientSide()) {
                    boolean spin = !isSpinning(stack);
                    setSpinning(stack, spin);
                    if (!spin) setFlying(stack, false);
                    level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1,
                            SoundSource.PLAYERS, 0.7F, spin ? 1.2F : 0.8F);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.pass(stack);
        }
        if (isFlying(stack)) return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide()) return;
        if (!MjolnirSaved.isWorthy(player)) return;
        if (getUseDuration(stack) - timeLeft < 10) return;

        ThrownMjolnir thrown = new ThrownMjolnir(level, player, stack);
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.6F, 1.0F);
        if (player.getAbilities().instabuild) {
            thrown.setFromCreative(true);
        } else {
            player.getInventory().removeItem(stack);
        }
        level.addFreshEntity(thrown);
        level.playSound(null, thrown, ModSounds.MJOLNIR_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer sp) MjolnirFlight.stop(sp);
    }

    /** The swing sound (server side, everybody nearby hears it). */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide()) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.MJOLNIR_SWING.get(),
                    SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return false;
    }

    /** Sneak + right-click the top of a block: stand it in the ground. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || context.getClickedFace() != net.minecraft.core.Direction.UP) {
            return InteractionResult.PASS;
        }
        if (!MjolnirSaved.isWorthy(player)) {
            notWorthy(player);
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().above();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (MjolnirBlock.tryPlace(level, target, player.getYRot())) {
            if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    // ── never in the hands of the unworthy ─────────────────────────────────────

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;

        if (!MjolnirSaved.isWorthy(player)) {
            // The hammer slips out of an unworthy inventory and lies on the ground.
            ItemEntity drop = new ItemEntity(level, player.getX(), player.getY() + 0.3D, player.getZ(), cleaned(stack));
            drop.setDeltaMovement(0.0D, 0.1D, 0.0D);
            drop.setPickUpDelay(40);
            level.addFreshEntity(drop);
            stack.setCount(0);
            notWorthy(player);
            return;
        }
        if ((isSpinning(stack) || isFlying(stack)) && !(selected && player.getMainHandItem() == stack)) {
            setSpinning(stack, false);
            setFlying(stack, false);
        }
        // A flight flag left over from before a restart or relog.
        if (isFlying(stack) && !MjolnirFlight.isFlying(player)) {
            setFlying(stack, false);
        }
    }

    /** A dropped hammer that lands stands in the ground. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide() && entity.onGround() && entity.tickCount > 8) {
            if (MjolnirBlock.tryPlace(level, entity.blockPosition(), entity.getYRot())) {
                entity.discard();
                return true;
            }
        }
        return false;
    }

    /** A hammer hit: heavy knockback, a thud, and it knocks a raised shield out of use like an axe. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        if (!level.isClientSide) {
            double dx = target.getX() - attacker.getX();
            double dz = target.getZ() - attacker.getZ();
            target.knockback(1.1D, -dx, -dz);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.18D, 0.0D));
            target.hurtMarked = true;
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 1.4F);
            if (target instanceof Player victim && victim.isBlocking()) {
                victim.disableShield(true);
            }
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) return super.getDefaultAttributeModifiers(slot);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 9.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.1D, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Whosoever holds this hammer, if he be worthy...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    // ── arm pose while spinning / flying ───────────────────────────────────────

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (isSpinning(stack) || isFlying(stack)) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                return null;
            }
        });
    }
}
