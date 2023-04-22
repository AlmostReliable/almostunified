package com.almostreliable.unified;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.ServerConfigs;
import com.almostreliable.unified.config.StartupConfig;
import com.almostreliable.unified.utils.TagOwnerships;
import com.google.common.base.Preconditions;
import net.minecraft.tags.TagManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class AlmostUnified {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);

    @Nullable private static AlmostUnifiedRuntime RUNTIME;
    @Nullable private static StartupConfig STARTUP_CONFIG;
    @Nullable private static ServerConfigs SERVER_CONFIGS;
    @Nullable private static TagManager TAG_MANAGER;
    @Nullable private static TagOwnerships TAG_OWNERSHIPS;

    public static StartupConfig getStartupConfig() {
        if (STARTUP_CONFIG == null) {
            STARTUP_CONFIG = Config.load(StartupConfig.NAME, new StartupConfig.Serializer());
        }
        return STARTUP_CONFIG;
    }

    public static boolean isRuntimeLoaded() {
        return RUNTIME != null;
    }

    public static AlmostUnifiedRuntime getRuntime() {
        if (RUNTIME == null) {
            return AlmostUnifiedFallbackRuntime.getInstance();
        }
        return RUNTIME;
    }

    public static void onTagManagerReload(TagManager tagManager) {
        SERVER_CONFIGS = ServerConfigs.load();
        TAG_MANAGER = tagManager;
        var unifyConfig = SERVER_CONFIGS.getUnifyConfig();
        TAG_OWNERSHIPS = new TagOwnerships(unifyConfig.bakeTags(), unifyConfig.getTagOwnerships());
    }

    public static void onReloadRecipeManager() {
        Preconditions.checkNotNull(SERVER_CONFIGS, "ServerConfigs were not loaded correctly");
        Preconditions.checkNotNull(TAG_MANAGER, "TagManager was not loaded correctly");
        Preconditions.checkNotNull(TAG_OWNERSHIPS, "TagOwnerships were not loaded correctly");

        RUNTIME = AlmostUnifiedRuntimeImpl.create(SERVER_CONFIGS, TAG_MANAGER, TAG_OWNERSHIPS);
    }
}
