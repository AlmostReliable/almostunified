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
    @Nullable private static TagManager tagManager;

    private static StartupConfig startupConfig;

    static void initStartupConfig() {
        startupConfig = Config.load(StartupConfig.NAME, new StartupConfig.Serializer());
    }

    public static StartupConfig getStartupConfig() {
        return startupConfig;
    }

    public static AlmostUnifiedRuntime getRuntime() {
        if (RUNTIME == null) {
            throw new IllegalStateException("AlmostUnifiedRuntime not initialized");
        }
        return RUNTIME;
    }

    public static void reloadRuntime() {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }

        RUNTIME = AlmostUnifiedRuntime.create(tagManager);
    }

    public static void updateTagManager(TagManager tm) {
        tagManager = tm;
    }
}
