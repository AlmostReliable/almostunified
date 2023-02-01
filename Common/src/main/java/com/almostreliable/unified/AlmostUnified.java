package com.almostreliable.unified;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.StartupConfig;
import net.minecraft.tags.TagManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class AlmostUnified {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);
    @Nullable private static AlmostUnifiedRuntime RUNTIME;
    @Nullable private static TagManager TAG_MANGER;
    @Nullable private static StartupConfig STARTUP_CONFIG;

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
            throw new IllegalStateException("AlmostUnifiedRuntime not initialized");
        }
        return RUNTIME;
    }

    public static void reloadRuntime() {
        if (TAG_MANGER == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }

        RUNTIME = AlmostUnifiedRuntime.create(TAG_MANGER);
    }

    public static void updateTagManager(TagManager tm) {
        TAG_MANGER = tm;
    }
}
