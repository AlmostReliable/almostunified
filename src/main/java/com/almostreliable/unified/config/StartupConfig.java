package com.almostreliable.unified.config;

import com.google.gson.JsonObject;

public final class StartupConfig extends Config {

    public static final String NAME = "startup";
    public static final StartupSerializer SERIALIZER = new StartupSerializer();

    private final boolean serverOnly;
    private final Boolean worldGenUnification;

    private StartupConfig(boolean serverOnly, boolean worldGenUnification) {
        super(NAME);
        this.serverOnly = serverOnly;
        this.worldGenUnification = worldGenUnification;
    }

    public boolean isServerOnly() {
        return serverOnly;
    }

    public boolean allowWorldGenUnification() {
        return worldGenUnification;
    }

    public static final class StartupSerializer extends Config.Serializer<StartupConfig> {

        private static final String SERVER_ONLY = "server_only";
        private static final String WORLD_GEN_UNIFICATION = "world_gen_unification";

        private StartupSerializer() {}

        @Override
        public StartupConfig handleDeserialization(JsonObject json) {
            boolean serverOnly = safeGet(() -> json.get(SERVER_ONLY).getAsBoolean(), false);
            boolean worldGenUnification = safeGet(() -> json.get(WORLD_GEN_UNIFICATION).getAsBoolean(), false);
            return new StartupConfig(serverOnly, worldGenUnification);
        }

        @Override
        public JsonObject serialize(StartupConfig config) {
            JsonObject json = new JsonObject();
            json.addProperty(SERVER_ONLY, config.serverOnly);
            json.addProperty(WORLD_GEN_UNIFICATION, config.worldGenUnification);

            return json;
        }
    }
}
