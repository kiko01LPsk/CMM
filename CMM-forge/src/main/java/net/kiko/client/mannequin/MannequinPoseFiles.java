package net.kiko.client.mannequin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kiko.mannequin.MannequinSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Saves and loads poses as small JSON files in the game folder under cmm_poses (they can be shared or edited by hand). */
public final class MannequinPoseFiles {

    private static final Gson GSON = new Gson();

    private MannequinPoseFiles() {}

    public static Path folder() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("cmm_poses");
    }

    /** Names (without .json) of the saved poses, sorted. */
    public static List<String> list() {
        List<String> names = new ArrayList<>();
        Path dir = folder();
        if (!Files.isDirectory(dir)) return names;
        try (Stream<Path> files = Files.list(dir)) {
            files.map(f -> f.getFileName().toString())
                    .filter(n -> n.toLowerCase(java.util.Locale.ROOT).endsWith(".json"))
                    .forEach(n -> names.add(n.substring(0, n.length() - 5)));
        } catch (IOException ignored) {
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /** Only letters, digits, space, dash and underscore are kept. */
    public static String cleanName(String raw) {
        String s = raw == null ? "" : raw.trim().replaceAll("[^A-Za-z0-9 _-]", "");
        return s.length() > 40 ? s.substring(0, 40) : s;
    }

    public static void save(String name, MannequinSettings s) throws IOException {
        String clean = cleanName(name);
        if (clean.isEmpty()) throw new IOException("name");
        JsonObject o = new JsonObject();
        o.addProperty("format", "cmm_pose");
        JsonArray pose = new JsonArray();
        for (float f : s.pose) pose.add(Math.round(f * 100.0F) / 100.0F);
        o.add("pose", pose);
        o.addProperty("tiltX", s.tiltX);
        o.addProperty("tiltZ", s.tiltZ);
        o.addProperty("crouching", s.crouching);
        o.addProperty("scale", s.scale);
        Files.createDirectories(folder());
        try (Writer w = Files.newBufferedWriter(folder().resolve(clean + ".json"), StandardCharsets.UTF_8)) {
            new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(o, w);
        }
    }

    public static void loadNamed(String name, MannequinSettings into) throws IOException {
        loadPath(folder().resolve(cleanName(name) + ".json"), into);
    }

    /** Reads a pose file from any path and applies it to the settings. Throws IOException when it is not a pose file. */
    public static void loadPath(Path path, MannequinSettings into) throws IOException {
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject o = GSON.fromJson(r, JsonObject.class);
            if (o == null || !o.has("pose") || !o.get("pose").isJsonArray()) throw new IOException("format");
            JsonArray a = o.getAsJsonArray("pose");
            if (a.size() != 18) throw new IOException("format");
            float[] pose = new float[18];
            for (int i = 0; i < 18; i++) pose[i] = Mth.clamp(a.get(i).getAsFloat(), -360.0F, 360.0F);
            System.arraycopy(pose, 0, into.pose, 0, 18);
            if (o.has("tiltX")) into.tiltX = o.get("tiltX").getAsFloat();
            if (o.has("tiltZ")) into.tiltZ = o.get("tiltZ").getAsFloat();
            if (o.has("crouching")) into.crouching = o.get("crouching").getAsBoolean();
            if (o.has("scale")) into.scale = Mth.clamp(o.get("scale").getAsFloat(), 0.25F, 4.0F);
        } catch (RuntimeException e) {
            throw new IOException("format", e);
        }
    }
}
