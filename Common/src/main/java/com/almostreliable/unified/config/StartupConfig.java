package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.google.gson.JsonObject;

public class StartupConfig extends Config {

    public static final String NAME = "startup";
    private final boolean serverOnly;
    private final Boolean worldGenUnification;

    public StartupConfig(String name, boolean serverOnly, boolean worldGenUnification) {
        super(name);
        this.serverOnly = serverOnly;
        this.worldGenUnification = worldGenUnification;
    }

    public boolean isServerOnly() {
        return serverOnly;
    }

    public boolean allowWorldGenUnification() {
        return worldGenUnification;
    }

    public static class Serializer extends Config.Serializer<StartupConfig> {
        public static final String SERVER_ONLY = "serverOnly";
        public static final String WORLD_GEN_UNIFICATION = "worldGenUnification";

        @Override
        public StartupConfig deserialize(String name, JsonObject json) {
            boolean serverOnly = safeGet(() -> json.get(SERVER_ONLY).getAsBoolean(), false);
            boolean worldGenUnification = switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
                case NEO_FORGE -> safeGet(() -> json.get(WORLD_GEN_UNIFICATION).getAsBoolean(), false);
                case FABRIC -> false;
            };
            return new StartupConfig(name, serverOnly, worldGenUnification);
        }

        @Override
        public JsonObject serialize(StartupConfig src) {
            JsonObject json = new JsonObject();
            json.addProperty(SERVER_ONLY, src.serverOnly);
            if (AlmostUnifiedPlatform.INSTANCE.getPlatform() == AlmostUnifiedPlatform.Platform.NEO_FORGE) {
                json.addProperty(WORLD_GEN_UNIFICATION, src.worldGenUnification);
            }

            return json;
        }
    }
}
