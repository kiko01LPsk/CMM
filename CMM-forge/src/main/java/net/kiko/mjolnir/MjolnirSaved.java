package net.kiko.mjolnir;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * World data for Mjolnir:
 *  - who has been made worthy with /mjolnir worthy (kiko01LPsk is always worthy)
 *  - where Mjolnir is currently standing in the ground (so it can be called from far away)
 */
public class MjolnirSaved extends SavedData {

    /** Always worthy, whatever the command says. */
    public static final String OWNER_NAME = "kiko01LPsk";

    private static final String NAME = "kiko_mjolnir";

    private final Set<UUID> worthy = new HashSet<>();
    private final Set<GlobalPos> placed = new HashSet<>();

    public static MjolnirSaved get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(MjolnirSaved::load, MjolnirSaved::new, NAME);
    }

    public static boolean isOwner(Player player) {
        return OWNER_NAME.equalsIgnoreCase(player.getGameProfile().getName());
    }

    /** True if this player may lift Mjolnir. Server-side this also checks the command list. */
    public static boolean isWorthy(Player player) {
        if (isOwner(player)) return true;
        if (player instanceof ServerPlayer sp) {
            return get(sp.server).worthy.contains(sp.getUUID());
        }
        return false;
    }

    public boolean setWorthy(UUID id, boolean value) {
        boolean changed = value ? worthy.add(id) : worthy.remove(id);
        if (changed) setDirty();
        return changed;
    }

    public void addPlaced(GlobalPos pos) {
        if (placed.add(pos)) setDirty();
    }

    public void removePlaced(GlobalPos pos) {
        if (placed.remove(pos)) setDirty();
    }

    public Set<GlobalPos> placed() {
        return placed;
    }

    // ── saving ────────────────────────────────────────────────────────────────

    public static MjolnirSaved load(CompoundTag tag) {
        MjolnirSaved data = new MjolnirSaved();
        for (Tag t : tag.getList("Worthy", Tag.TAG_STRING)) {
            try {
                data.worthy.add(UUID.fromString(t.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (Tag t : tag.getList("Placed", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(c.getString("Dim")));
            data.placed.add(GlobalPos.of(dim, new BlockPos(c.getInt("X"), c.getInt("Y"), c.getInt("Z"))));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag w = new ListTag();
        for (UUID id : worthy) w.add(StringTag.valueOf(id.toString()));
        tag.put("Worthy", w);
        ListTag p = new ListTag();
        for (GlobalPos g : placed) {
            CompoundTag c = new CompoundTag();
            c.putString("Dim", g.dimension().location().toString());
            c.putInt("X", g.pos().getX());
            c.putInt("Y", g.pos().getY());
            c.putInt("Z", g.pos().getZ());
            p.add(c);
        }
        tag.put("Placed", p);
        return tag;
    }
}
