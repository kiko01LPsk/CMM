package net.kiko.client.mannequin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kiko.KikoMod;
import net.kiko.mannequin.MannequinSkinData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Uploaded models: reads a Blockbench "Bedrock Entity" file (.geo.json), keeps only what is needed,
 * packs it small (gzip + base64) for the mannequin, and builds model parts from it on every client.
 */
public final class CustomModelCodec {

    /** A built model: the root part, its bones by simplified name, and their resting rotations. */
    public static final class Built {
        public final ModelPart root;
        public final Map<String, ModelPart> bones = new HashMap<>();
        public final Map<ModelPart, float[]> rest = new HashMap<>();

        Built(ModelPart root) {
            this.root = root;
        }
    }

    private static final Map<String, Built> CACHE = new HashMap<>();

    private CustomModelCodec() {}

    public static final class ModelException extends Exception {
        public ModelException(String key) {
            super(key);
        }
    }

    // ── Reading a file ────────────────────────────────────────────────────────

    public static String load(String pathText) throws ModelException {
        String cleaned = pathText == null ? "" : pathText.trim();
        if (cleaned.length() > 1 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) throw new ModelException("screen.kiko.mannequin.model.no_file");
        String text;
        try {
            Path path = Path.of(cleaned);
            if (!Files.isRegularFile(path)) throw new ModelException("screen.kiko.mannequin.model.no_file");
            if (Files.size(path) > 4_000_000L) throw new ModelException("screen.kiko.mannequin.model.too_big");
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            throw new ModelException("screen.kiko.mannequin.model.no_file");
        }
        return encode(text);
    }

    /** Parses the file, keeps the bones and cubes, and returns the packed text for the mannequin. */
    static String encode(String fileText) throws ModelException {
        JsonObject geo;
        try {
            geo = findGeometry(JsonParser.parseString(fileText).getAsJsonObject());
        } catch (RuntimeException e) {
            throw new ModelException("screen.kiko.mannequin.model.invalid");
        }
        if (geo == null) throw new ModelException("screen.kiko.mannequin.model.invalid");

        JsonObject out = new JsonObject();
        int w = 64, h = 64;
        JsonObject desc = geo.has("description") && geo.get("description").isJsonObject()
                ? geo.getAsJsonObject("description") : geo;
        w = intOr(desc, "texture_width", intOr(desc, "texturewidth", w));
        h = intOr(desc, "texture_height", intOr(desc, "textureheight", h));
        out.addProperty("w", w);
        out.addProperty("h", h);

        JsonArray bonesOut = new JsonArray();
        if (!geo.has("bones") || !geo.get("bones").isJsonArray()) throw new ModelException("screen.kiko.mannequin.model.invalid");
        int cubes = 0;
        for (JsonElement be : geo.getAsJsonArray("bones")) {
            JsonObject b = be.getAsJsonObject();
            JsonObject bo = new JsonObject();
            bo.addProperty("name", b.get("name").getAsString());
            if (b.has("parent")) bo.addProperty("parent", b.get("parent").getAsString());
            if (b.has("pivot")) bo.add("pivot", b.get("pivot"));
            if (b.has("rotation")) bo.add("rotation", b.get("rotation"));
            if (b.has("mirror")) bo.add("mirror", b.get("mirror"));
            JsonArray cs = new JsonArray();
            if (b.has("cubes")) {
                for (JsonElement ce : b.getAsJsonArray("cubes")) {
                    JsonObject c = ce.getAsJsonObject();
                    JsonObject co = new JsonObject();
                    co.add("origin", c.get("origin"));
                    co.add("size", c.get("size"));
                    float[] uv = boxUv(c);
                    JsonArray uvArr = new JsonArray();
                    uvArr.add(uv[0]);
                    uvArr.add(uv[1]);
                    co.add("uv", uvArr);
                    if (c.has("inflate")) co.add("inflate", c.get("inflate"));
                    if (c.has("mirror")) co.add("mirror", c.get("mirror"));
                    if (c.has("pivot")) co.add("pivot", c.get("pivot"));
                    if (c.has("rotation")) co.add("rotation", c.get("rotation"));
                    cs.add(co);
                    cubes++;
                }
            }
            bo.add("cubes", cs);
            bonesOut.add(bo);
        }
        if (cubes == 0 || cubes > 4000) throw new ModelException("screen.kiko.mannequin.model.invalid");
        out.add("bones", bonesOut);

        String packed = pack(out.toString());
        if (packed.length() > MannequinSkinData.MAX_LENGTH) throw new ModelException("screen.kiko.mannequin.model.too_big");
        return packed;
    }

    private static JsonObject findGeometry(JsonObject root) {
        if (root.has("minecraft:geometry") && root.get("minecraft:geometry").isJsonArray()) {
            JsonArray arr = root.getAsJsonArray("minecraft:geometry");
            if (arr.size() > 0) return arr.get(0).getAsJsonObject();
        }
        for (Map.Entry<String, JsonElement> e : root.entrySet()) {
            if (e.getKey().startsWith("geometry.") && e.getValue().isJsonObject()) return e.getValue().getAsJsonObject();
        }
        return null;
    }

    private static int intOr(JsonObject o, String key, int fallback) {
        return o.has(key) ? o.get(key).getAsInt() : fallback;
    }

    /** Box UV of a cube; a per-face UV cube is approximated from its north face. */
    private static float[] boxUv(JsonObject c) {
        JsonElement uv = c.get("uv");
        if (uv != null && uv.isJsonArray()) {
            JsonArray a = uv.getAsJsonArray();
            return new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat()};
        }
        if (uv != null && uv.isJsonObject()) {
            JsonObject faces = uv.getAsJsonObject();
            JsonArray size = c.getAsJsonArray("size");
            float d = size.get(2).getAsFloat();
            if (faces.has("north") && faces.getAsJsonObject("north").has("uv")) {
                JsonArray n = faces.getAsJsonObject("north").getAsJsonArray("uv");
                return new float[]{n.get(0).getAsFloat() - d, n.get(1).getAsFloat() - d};
            }
        }
        return new float[]{0, 0};
    }

    // ── Packing ───────────────────────────────────────────────────────────────

    private static String pack(String text) throws ModelException {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (GZIPOutputStream gz = new GZIPOutputStream(bytes)) {
                gz.write(text.getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException e) {
            throw new ModelException("screen.kiko.mannequin.model.invalid");
        }
    }

    private static String unpack(String packed) throws IOException {
        byte[] data = Base64.getDecoder().decode(packed);
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ── Building the model parts ──────────────────────────────────────────────

    /** The built model for this packed text, or null when it cannot be read. */
    public static Built get(String packed) {
        if (packed == null || packed.isEmpty()) return null;
        if (CACHE.containsKey(packed)) return CACHE.get(packed);
        Built built = null;
        try {
            built = build(JsonParser.parseString(unpack(packed)).getAsJsonObject());
        } catch (Exception e) {
            KikoMod.LOGGER.warn("Could not build an uploaded mannequin model", e);
        }
        CACHE.put(packed, built);
        return built;
    }

    private static float f(JsonArray a, int i) {
        return a != null && a.size() > i ? a.get(i).getAsFloat() : 0.0F;
    }

    private static JsonArray arr(JsonObject o, String key) {
        return o.has(key) && o.get(key).isJsonArray() ? o.getAsJsonArray(key) : null;
    }

    /** Bedrock (y up) to the game's model space (y down, 24 at the feet). */
    private static Built build(JsonObject model) {
        int texW = model.get("w").getAsInt();
        int texH = model.get("h").getAsInt();
        Map<String, JsonObject> byName = new HashMap<>();
        List<JsonObject> list = new ArrayList<>();
        for (JsonElement e : model.getAsJsonArray("bones")) {
            JsonObject b = e.getAsJsonObject();
            byName.put(b.get("name").getAsString(), b);
            list.add(b);
        }

        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> defs = new HashMap<>();
        Map<String, float[]> rests = new HashMap<>();
        List<String> order = new ArrayList<>();
        int guard = 0;
        // Parents before children (repeat until all bones are placed).
        while (defs.size() < list.size() && guard++ < list.size() + 2) {
            for (JsonObject b : list) {
                String name = b.get("name").getAsString();
                if (defs.containsKey(name)) continue;
                String parent = b.has("parent") ? b.get("parent").getAsString() : null;
                if (parent != null && byName.containsKey(parent) && !defs.containsKey(parent)) continue;
                PartDefinition parentDef = parent != null && defs.containsKey(parent) ? defs.get(parent) : mesh.getRoot();
                JsonArray pivot = arr(b, "pivot");
                JsonArray rot = arr(b, "rotation");
                float px = f(pivot, 0), py = f(pivot, 1), pz = f(pivot, 2);
                float ox = 0, oy = 24, oz = 0;
                if (parent != null && byName.containsKey(parent)) {
                    JsonArray pp = arr(byName.get(parent), "pivot");
                    ox = f(pp, 0); oy = f(pp, 1); oz = f(pp, 2);
                    // child offset is relative to the parent's pivot, y flipped
                    PartPose pose = PartPose.offsetAndRotation(px - ox, -(py - oy), pz - oz,
                            (float) Math.toRadians(-f(rot, 0)), (float) Math.toRadians(f(rot, 1)), (float) Math.toRadians(-f(rot, 2)));
                    defs.put(name, addBone(parentDef, name, b, px, py, pz, pose));
                } else {
                    PartPose pose = PartPose.offsetAndRotation(px, 24 - py, pz,
                            (float) Math.toRadians(-f(rot, 0)), (float) Math.toRadians(f(rot, 1)), (float) Math.toRadians(-f(rot, 2)));
                    defs.put(name, addBone(parentDef, name, b, px, py, pz, pose));
                }
                rests.put(name, new float[]{(float) Math.toRadians(-f(rot, 0)), (float) Math.toRadians(f(rot, 1)),
                        (float) Math.toRadians(-f(rot, 2))});
                order.add(name);
            }
        }

        ModelPart root = LayerDefinition.create(mesh, texW, texH).bakeRoot();
        Built built = new Built(root);
        for (String name : order) {
            ModelPart part = findPart(root, name, byName);
            if (part != null) {
                built.bones.put(simple(name), part);
                built.rest.put(part, rests.get(name));
            }
        }
        return built;
    }

    private static PartDefinition addBone(PartDefinition parent, String name, JsonObject bone,
                                          float px, float py, float pz, PartPose pose) {
        PartDefinition def = parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
        JsonArray cubes = bone.getAsJsonArray("cubes");
        boolean boneMirror = bone.has("mirror") && bone.get("mirror").getAsBoolean();
        int i = 0;
        for (JsonElement ce : cubes) {
            JsonObject c = ce.getAsJsonObject();
            JsonArray origin = arr(c, "origin"), size = arr(c, "size"), uv = arr(c, "uv");
            float sx = f(size, 0), sy = f(size, 1), sz = f(size, 2);
            boolean mirror = c.has("mirror") ? c.get("mirror").getAsBoolean() : boneMirror;
            float inflate = c.has("inflate") ? c.get("inflate").getAsFloat() : 0.0F;
            CubeListBuilder builder = CubeListBuilder.create().texOffs((int) f(uv, 0), (int) f(uv, 1)).mirror(mirror);
            JsonArray cp = arr(c, "pivot"), cr = arr(c, "rotation");
            if (cr != null && cp != null) {
                // a rotated cube sits in its own part, pivoting around the cube's pivot
                float cx = f(cp, 0), cy = f(cp, 1), cz = f(cp, 2);
                builder.addBox(f(origin, 0) - cx, cy - f(origin, 1) - sy, f(origin, 2) - cz, sx, sy, sz, new CubeDeformation(inflate));
                def.addOrReplaceChild("cube" + i, builder, PartPose.offsetAndRotation(cx - px, -(cy - py), cz - pz,
                        (float) Math.toRadians(-f(cr, 0)), (float) Math.toRadians(f(cr, 1)), (float) Math.toRadians(-f(cr, 2))));
            } else {
                builder.addBox(f(origin, 0) - px, py - f(origin, 1) - sy, f(origin, 2) - pz, sx, sy, sz, new CubeDeformation(inflate));
                def.addOrReplaceChild("cube" + i, builder, PartPose.ZERO);
            }
            i++;
        }
        return def;
    }

    /** The baked part of a bone: walk down from the root through the bone's ancestors. */
    private static ModelPart findPart(ModelPart root, String name, Map<String, JsonObject> byName) {
        List<String> chain = new ArrayList<>();
        String current = name;
        int guard = 0;
        while (current != null && byName.containsKey(current) && guard++ < 64) {
            chain.add(0, current);
            JsonObject b = byName.get(current);
            current = b.has("parent") ? b.get("parent").getAsString() : null;
        }
        ModelPart part = root;
        for (String step : chain) {
            if (!part.hasChild(step)) return null;
            part = part.getChild(step);
        }
        return part;
    }

    /** "Left Arm", "left_arm", "leftArm" all become "leftarm". */
    public static String simple(String name) {
        return name.toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "").replace("-", "");
    }
}
