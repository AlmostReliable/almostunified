package com.almostreliable.unified;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class AlmostUnified {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);
    @Nullable
    private static AlmostUnifiedRuntime RUNTIME;


    public AlmostUnified() {
//        MANAGER.register(new ResourceLocation("minecraft:crafting_shaped"), new ShapedRecipeTransformer());
    }

    public static AlmostUnifiedRuntime getRuntime() {
        if (RUNTIME == null) {
            throw new IllegalStateException("AlmostUnifiedRuntime not initialized");
        }
        return RUNTIME;
    }

    static void initializeRuntime() {
        RUNTIME = new AlmostUnifiedRuntime();
    }
}
