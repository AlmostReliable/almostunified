package com.almostreliable.unified.config;

import com.google.gson.JsonObject;

public class StartupConfig extends Config {

    public static final String NAME = "startup";
    private final boolean serverOnly;

    public StartupConfig(boolean serverOnly) {
        this.serverOnly = serverOnly;
    }

    public boolean isServerOnly() {
        return serverOnly;
    }

    public static class Serializer extends Config.Serializer<StartupConfig> {
        public static final String SERVER_ONLY = "serverOnly";

        @Override
        public StartupConfig deserialize(JsonObject json) {
            return new StartupConfig(safeGet(() -> json.get(SERVER_ONLY).getAsBoolean(), false));
        }

        @Override
        public JsonObject serialize(StartupConfig src) {
            JsonObject json = new JsonObject();
            json.addProperty(SERVER_ONLY, src.serverOnly);
            return json;
        }
    }
}
