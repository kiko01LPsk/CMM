package net.kiko.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModArmorMaterials implements ArmorMaterial {

    // name, durabilityMultiplier, protection[boots/legs/chest/head], enchantability, equipSound, toughness, knockback
    GLASSES("kiko_glasses",       5,  new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f),
    SUNGLASSES("kiko_sunglasses", 5,  new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f),
    REGGAE_HAT("kiko_reggae_hat", 5,  new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f),
    VEST("kiko_vest",             8,  new int[]{1, 3, 5, 1},  8, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f),
    ZAKAR("kiko_zakar",          20,  new int[]{2, 5, 7, 3},  8, SoundEvents.ARMOR_EQUIP_IRON,    1.0f, 0.0f);

    private static final int[] BASE_DURABILITY = {13, 15, 16, 11}; // boots, legs, chest, head

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protection;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protection, int enchantability,
                      SoundEvent equipSound, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = protection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return protection[type.getSlot().getIndex()];
    }

    @Override public int getEnchantmentValue()        { return enchantability; }
    @Override public SoundEvent getEquipSound()       { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }

    /**
     * Vanilla/Forge resolve the worn texture as
     * assets/minecraft/textures/models/armor/<name>_layer_1.png when the name has no namespace,
     * which matches where the original textures already live.
     */
    @Override public String getName()                 { return name; }

    @Override public float getToughness()             { return toughness; }
    @Override public float getKnockbackResistance()   { return knockbackResistance; }
}
