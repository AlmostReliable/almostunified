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
    private final boolean logInvalidTags;

    private DebugConfig(boolean dumpDuplicates, boolean dumpOverview, boolean dumpRecipes, boolean dumpTags, boolean dumpUnification, boolean logInvalidTags) {
        super(NAME);
        this.dumpDuplicates = dumpDuplicates;
        this.dumpOverview = dumpOverview;
        this.dumpRecipes = dumpRecipes;
        this.dumpTags = dumpTags;
        this.dumpUnification = dumpUnification;
        this.logInvalidTags = logInvalidTags;
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

    public boolean shouldLogInvalidTags() {
        return logInvalidTags;
    }

    public static final class DebugSerializer extends Config.Serializer<DebugConfig> {

        private static final String DUMP_DUPLICATES = "dump_duplicates";
        private static final String DUMP_OVERVIEW = "dump_overview";
        private static final String DUMP_RECIPES = "dump_recipes";
        private static final String DUMP_TAGS = "dump_tags";
        private static final String DUMP_UNIFICATION = "dump_unification";
        private static final String LOG_INVALID_TAGS = "log_invalid_tags";

        private DebugSerializer() {}

        @Override
        public DebugConfig handleDeserialization(JsonObject json) {
            return new DebugConfig(
                safeGet(() -> json.get(DUMP_DUPLICATES).getAsBoolean(), false),
                safeGet(() -> json.get(DUMP_OVERVIEW).getAsBoolean(), false),
                safeGet(() -> json.get(DUMP_RECIPES).getAsBoolean(), false),
                safeGet(() -> json.get(DUMP_TAGS).getAsBoolean(), false),
                safeGet(() -> json.get(DUMP_UNIFICATION).getAsBoolean(), false),
                safeGet(() -> json.get(LOG_INVALID_TAGS).getAsBoolean(), false)
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
            json.addProperty(LOG_INVALID_TAGS, config.logInvalidTags);
            return json;
        }
    }
}
