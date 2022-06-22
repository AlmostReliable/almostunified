package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.ie.IEModPriorityOverride;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;

public class AlmostUnifiedRuntimeForge extends AlmostUnifiedRuntime {

    AlmostUnifiedRuntimeForge(RecipeHandlerFactory recipeHandlerFactory) {
        super(recipeHandlerFactory);
    }

    @Override
    protected void onRun() {
        if (AlmostUnifiedPlatform.INSTANCE.isModLoaded(ModConstants.IE)) {
            IEModPriorityOverride.overrideModPriorities(modPriorities).run();
        }
    }
}
