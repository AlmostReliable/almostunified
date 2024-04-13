package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.utils.FileUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DebugConfig extends Config {
    public static final String NAME = "debug";

    public final boolean dumpTagMap;
    public final boolean dumpDuplicates;
    public final boolean dumpUnification;
    public final boolean dumpOverview;
    public final boolean dumpRecipes;

    public DebugConfig(String name, boolean dumpTagMap, boolean dumpDuplicates, boolean dumpUnification, boolean dumpOverview, boolean dumpRecipes) {
        super(name);
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

        FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getLogPath(), "unify_tag_dump.txt", sb -> {
            sb.append(StreamSupport.stream(lookup
                            .getUnifiedTags().spliterator(), false)
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

        FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getLogPath(),
                filename,
                sb -> recipes.forEach((key, value) -> sb
                        .append(key.toString())
                        .append(" [JSON]:")
                        .append(value.toString())
                        .append("\n")));
    }

    public static class Serializer extends Config.Serializer<DebugConfig> {

        public static final String DUMP_TAG_MAP = "dumpTagMap";
        public static final String DUMP_DUPLICATES = "dumpDuplicates";
        public static final String DUMP_UNIFICATION = "dumpUnification";
        public static final String DUMP_OVERVIEW = "dumpOverview";
        public static final String DUMP_RECIPES = "dumpRecipes";

        @Override
        public DebugConfig deserialize(String name, JsonObject json) {
            return new DebugConfig(
                    name,
                    safeGet(() -> json.get(DUMP_TAG_MAP).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_DUPLICATES).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_UNIFICATION).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_OVERVIEW).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_RECIPES).getAsBoolean(), false)
            );
        }

        @Override
        public JsonObject serialize(DebugConfig src) {
            JsonObject json = new JsonObject();
            json.addProperty(DUMP_TAG_MAP, src.dumpTagMap);
            json.addProperty(DUMP_DUPLICATES, src.dumpDuplicates);
            json.addProperty(DUMP_UNIFICATION, src.dumpUnification);
            json.addProperty(DUMP_OVERVIEW, src.dumpOverview);
            json.addProperty(DUMP_RECIPES, src.dumpRecipes);
            return json;
        }
    }
}
