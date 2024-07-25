package com.almostreliable.unified.config;

import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.utils.FileUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public final class DebugConfig extends Config {

    public static final String NAME = "debug";
    public static final DebugSerializer SERIALIZER = new DebugSerializer();

    public final boolean dumpTagMap;
    public final boolean dumpDuplicates;
    public final boolean dumpUnification;
    public final boolean dumpOverview;
    public final boolean dumpRecipes;

    private DebugConfig(boolean dumpTagMap, boolean dumpDuplicates, boolean dumpUnification, boolean dumpOverview, boolean dumpRecipes) {
        super(NAME);
        this.dumpTagMap = dumpTagMap;
        this.dumpDuplicates = dumpDuplicates;
        this.dumpUnification = dumpUnification;
        this.dumpOverview = dumpOverview;
        this.dumpRecipes = dumpRecipes;
    }

    public void logUnifyTagDump(UnifyLookup lookup) {
        if (!dumpTagMap) {
            return;
        }

        FileUtils.writeLog("unify_tag_dump.txt", sb -> {
            sb.append(lookup.getUnifiedTags().stream()
                    .sorted(Comparator.comparing(t -> t.location().toString()))
                    .map(t -> StringUtils.rightPad(t.location().toString(), 40) + " => " + lookup
                            .getEntries(t)
                            .stream()
                            .map(entry -> entry.id().toString())
                            .sorted()
                            .collect(Collectors.joining(", ")) + "\n")
                    .collect(Collectors.joining()));
        });
    }

    public void logRecipes(Map<ResourceLocation, JsonElement> recipes, String filename) {
        if (!dumpRecipes) {
            return;
        }

        FileUtils.writeLog(
                filename,
                sb -> recipes.forEach((key, value) -> sb
                        .append(key.toString())
                        .append(" [JSON]:")
                        .append(value.toString())
                        .append("\n"))
        );
    }

    public static final class DebugSerializer extends Config.Serializer<DebugConfig> {

        private static final String DUMP_TAG_MAP = "dumpTagMap";
        private static final String DUMP_DUPLICATES = "dumpDuplicates";
        private static final String DUMP_UNIFICATION = "dumpUnification";
        private static final String DUMP_OVERVIEW = "dumpOverview";
        private static final String DUMP_RECIPES = "dumpRecipes";

        private DebugSerializer() {}

        @Override
        public DebugConfig handleDeserialization(JsonObject json) {
            return new DebugConfig(
                    safeGet(() -> json.get(DUMP_TAG_MAP).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_DUPLICATES).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_UNIFICATION).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_OVERVIEW).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_RECIPES).getAsBoolean(), false)
            );
        }

        @Override
        public JsonObject serialize(DebugConfig config) {
            JsonObject json = new JsonObject();
            json.addProperty(DUMP_TAG_MAP, config.dumpTagMap);
            json.addProperty(DUMP_DUPLICATES, config.dumpDuplicates);
            json.addProperty(DUMP_UNIFICATION, config.dumpUnification);
            json.addProperty(DUMP_OVERVIEW, config.dumpOverview);
            json.addProperty(DUMP_RECIPES, config.dumpRecipes);
            return json;
        }
    }
}
