package com.almostreliable.unified.config;

import com.google.gson.JsonObject;

public final class DebugConfig extends Config {

    public static final String NAME = "debug";
    public static final DebugSerializer SERIALIZER = new DebugSerializer();

    private final boolean dumpDuplicates;
    private final boolean dumpOverview;
    private final boolean dumpRecipes;
    private final boolean dumpTags;
    private final boolean dumpUnification;

    private DebugConfig(boolean dumpDuplicates, boolean dumpOverview, boolean dumpRecipes, boolean dumpTags, boolean dumpUnification) {
        super(NAME);
        this.dumpDuplicates = dumpDuplicates;
        this.dumpOverview = dumpOverview;
        this.dumpRecipes = dumpRecipes;
        this.dumpTags = dumpTags;
        this.dumpUnification = dumpUnification;
    }

    public boolean shouldDumpDuplicates() {
        return dumpDuplicates;
    }

    public boolean shouldDumpOverview() {
        return dumpOverview;
    }

    public boolean shouldDumpRecipes() {
        return dumpRecipes;
    }

    public boolean shouldDumpTags() {
        return dumpTags;
    }

    public boolean shouldDumpUnification() {
        return dumpUnification;
    }

    public static final class DebugSerializer extends Config.Serializer<DebugConfig> {

        private static final String DUMP_DUPLICATES = "dumpDuplicates";
        private static final String DUMP_OVERVIEW = "dumpOverview";
        private static final String DUMP_RECIPES = "dumpRecipes";
        private static final String DUMP_TAGS = "dumpTags";
        private static final String DUMP_UNIFICATION = "dumpUnification";

        private DebugSerializer() {}

        @Override
        public DebugConfig handleDeserialization(JsonObject json) {
            return new DebugConfig(
                    safeGet(() -> json.get(DUMP_DUPLICATES).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_OVERVIEW).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_RECIPES).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_TAGS).getAsBoolean(), false),
                    safeGet(() -> json.get(DUMP_UNIFICATION).getAsBoolean(), false)
            );
        }

        @Override
        public JsonObject serialize(DebugConfig config) {
            JsonObject json = new JsonObject();
            json.addProperty(DUMP_DUPLICATES, config.dumpDuplicates);
            json.addProperty(DUMP_OVERVIEW, config.dumpOverview);
            json.addProperty(DUMP_RECIPES, config.dumpRecipes);
            json.addProperty(DUMP_TAGS, config.dumpTags);
            json.addProperty(DUMP_UNIFICATION, config.dumpUnification);
            return json;
        }
    }
}
