package net.kiko.trident;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The Godspear: a spear that always comes back like a Loyalty trident, never breaks and hits far harder
 * than any normal trident (melee and thrown).
 */
public class GodspearItem extends KikoTridentItem {
    /** Damage of a thrown Godspear (a vanilla trident does 8). */
    public static final float THROWN_DAMAGE = 30.0F;
    /** How strongly it flies back to the thrower (Loyalty III is 3). */
    public static final int LOYALTY = 5;

    private final Multimap<Attribute, AttributeModifier> attributes;

    public GodspearItem(Properties properties) {
        super(properties);
        this.attributes = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 17.0D, AttributeModifier.Operation.ADDITION))
                .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }
}
