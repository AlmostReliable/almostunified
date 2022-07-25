package com.almostreliable.unified;

import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class AlmostUnified {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);
    @Nullable
    private static AlmostUnifiedRuntime RUNTIME;

    public static AlmostUnifiedRuntime getRuntime() {
        if (RUNTIME == null) {
            throw new IllegalStateException("AlmostUnifiedRuntime not initialized");
        }
        return RUNTIME;
    }

    public static boolean runtimeInitialized() {
        return RUNTIME != null;
    }

    static void initializeRuntime() {
        RecipeHandlerFactory factory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(factory);
        RUNTIME = AlmostUnifiedPlatform.INSTANCE.createRuntime(factory);
    }
}
