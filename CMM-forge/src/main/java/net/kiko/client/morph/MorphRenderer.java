package net.kiko.client.morph;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kiko.client.PoseBindings;
import net.kiko.morph.ClientMorphStore;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Draws morphed players as the mob they turned into (everyone who can see the player sees it). */
public final class MorphRenderer {

    /** Unbound by default; set it in Controls. */
    public static final KeyMapping RESET = new KeyMapping("key.kiko.morph_reset",
            InputConstants.UNKNOWN.getValue(), PoseBindings.CATEGORY);

    private static final class Dummy {
        String id;
        LivingEntity entity;
    }

    private static final Map<UUID, Dummy> DUMMIES = new HashMap<>();

    private MorphRenderer() {}

    private static Map<ResourceLocation, net.minecraft.client.renderer.texture.AbstractTexture> textureMapCache;
    private static boolean textureMapFailed;

    /** The texture manager's own path -> texture map (found by type, so it works with any field naming). */
    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, net.minecraft.client.renderer.texture.AbstractTexture> textureMap() {
        if (textureMapCache != null || textureMapFailed) return textureMapCache;
        try {
            Object tm = Minecraft.getInstance().getTextureManager();
            for (java.lang.reflect.Field f : tm.getClass().getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                Map<?, ?> m = (Map<?, ?>) f.get(tm);
                if (m != null && !m.isEmpty() && m.values().iterator().next() instanceof net.minecraft.client.renderer.texture.AbstractTexture) {
                    textureMapCache = (Map<ResourceLocation, net.minecraft.client.renderer.texture.AbstractTexture>) m;
                    return textureMapCache;
                }
            }
        } catch (Throwable t) {
            // fall through
        }
        textureMapFailed = true;
        return null;
    }

    public static void clear() {
        DUMMIES.clear();
        ClientMorphStore.clear();
        ClientMorphSkins.clear();
    }

    private static Dummy dummyFor(AbstractClientPlayer player, String id) {
        Dummy d = DUMMIES.get(player.getUUID());
        if (d != null && d.id.equals(id) && d.entity != null && d.entity.level() == player.level()) return d;
        d = new Dummy();
        d.id = id;
        try {
            String rest = id;
            java.util.UUID mobUuid = null;
            int at = rest.indexOf('@');
            if (at >= 0) {
                try { mobUuid = java.util.UUID.fromString(rest.substring(at + 1)); } catch (IllegalArgumentException ignored) { }
                rest = rest.substring(0, at);
            }
            boolean baby = rest.endsWith("#baby");
            String plain = baby ? rest.substring(0, rest.length() - 5) : rest;
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(plain));
            Entity e = type == null ? null : type.create(player.level());
            if (e instanceof LivingEntity living) {
                d.entity = living;
                if (mobUuid != null) living.setUUID(mobUuid); // resource packs pick skin variants from the UUID
                if (baby && living instanceof net.minecraft.world.entity.Mob mob) mob.setBaby(true);
            }
        } catch (Throwable t) {
            d.entity = null;
        }
        DUMMIES.put(player.getUUID(), d);
        return d;
    }

    /** Once per client tick: walk animation of the morphs follows the player's movement. */
    public static void tick(Minecraft mc) {
        if (mc.level == null) return;
        Iterator<Map.Entry<UUID, Dummy>> it = DUMMIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Dummy> entry = it.next();
            Dummy d = entry.getValue();
            net.minecraft.world.entity.player.Player p = mc.level.getPlayerByUUID(entry.getKey());
            if (d.entity == null || p == null) continue;
            double dx = p.getX() - p.xo;
            double dz = p.getZ() - p.zo;
            float speed = Math.min((float) Math.sqrt(dx * dx + dz * dz) * 4.0F, 1.0F);
            d.entity.walkAnimation.update(speed, 0.4F);
            d.entity.tickCount = p.tickCount;
        }
    }

    /** Returns true when it drew the mob, so the normal player rendering is skipped. */
    public static boolean render(AbstractClientPlayer player, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight) {
        String id = ClientMorphStore.get(player.getUUID());
        if (id.isEmpty()) {
            DUMMIES.remove(player.getUUID());
            return false;
        }
        Dummy d = dummyFor(player, id);
        LivingEntity m = d.entity;
        if (m == null) return false;

        m.setPos(player.getX(), player.getY(), player.getZ());
        m.xo = player.xo; m.yo = player.yo; m.zo = player.zo;
        m.xOld = player.xOld; m.yOld = player.yOld; m.zOld = player.zOld;
        m.yBodyRot = player.yBodyRot; m.yBodyRotO = player.yBodyRotO;
        m.yHeadRot = player.yHeadRot; m.yHeadRotO = player.yHeadRotO;
        m.setYRot(player.getYRot()); m.yRotO = player.yRotO;
        m.setXRot(player.getXRot()); m.xRotO = player.xRotO;
        m.setPose(player.getPose());
        m.setOnGround(player.onGround());
        m.swinging = player.swinging;
        m.swingTime = player.swingTime;
        m.attackAnim = player.attackAnim;
        m.oAttackAnim = player.oAttackAnim;
        m.hurtTime = player.hurtTime;
        m.hurtDuration = player.hurtDuration;
        m.deathTime = player.deathTime;
        m.setInvisible(player.isInvisible());
        m.setCustomNameVisible(false); // no nametag while morphed
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            m.setItemSlot(slot, player.getItemBySlot(slot));
        }

        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        @SuppressWarnings({"unchecked", "rawtypes"})
        net.minecraft.client.renderer.entity.EntityRenderer<LivingEntity> renderer =
                (net.minecraft.client.renderer.entity.EntityRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(m);
        net.minecraft.client.renderer.texture.DynamicTexture custom = ClientMorphSkins.get(player.getUUID());
        Map<ResourceLocation, net.minecraft.client.renderer.texture.AbstractTexture> textures = custom == null ? null : textureMap();
        if (textures == null) {
            renderer.render(m, yaw, partialTick, poseStack, buffer, packedLight);
            return true;
        }
        // Custom skin: put it in place of the mob's normal texture just while this one mob is drawn (drawn straight away, not batched).
        ResourceLocation loc = renderer.getTextureLocation(m);
        net.minecraft.client.renderer.texture.AbstractTexture original = textures.get(loc);
        textures.put(loc, custom);
        try {
            MultiBufferSource.BufferSource own = MultiBufferSource.immediate(new com.mojang.blaze3d.vertex.BufferBuilder(2048));
            renderer.render(m, yaw, partialTick, poseStack, own, packedLight);
            own.endBatch();
        } finally {
            if (original != null) textures.put(loc, original); else textures.remove(loc);
        }
        return true;
    }
}
