package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedFallbackRuntime;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.CRTLookup;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
@EmiEntrypoint
public class AlmostEMI implements EmiPlugin {

    // temporarily disabled to due to it breaking transfer handlers
    //    @Override
    //    public void initialize(EmiInitRegistry registry) {
    //        AlmostUnifiedFallbackRuntime.getInstance().reload();
    //
    //        var emiDisabled = AlmostUnified.getRuntime()
    //                .getUnifyConfig()
    //                .map(UnifyConfig::reiOrJeiDisabled)
    //                .orElse(false);
    //        if (emiDisabled) return;
    //
    //        for (ItemStack item : HideHelper.createHidingList(AlmostUnified.getRuntime())) {
    //            registry.disableStack(EmiStack.of(item));
    //        }
    //    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeDecorator(new IndicatorDecorator());

        AlmostUnifiedFallbackRuntime.getInstance().reload();

        var emiDisabled = AlmostUnified.getRuntime()
                .getUnifyConfig()
                .map(UnifyConfig::reiOrJeiDisabled)
                .orElse(false);
        if (emiDisabled) return;

        for (ItemStack item : HideHelper.getStacksToHide()) {
            registry.removeEmiStacks(EmiStack.of(item));
        }
    }

    private static class IndicatorDecorator implements EmiRecipeDecorator {

        @Override
        public void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets) {
            var recipeId = recipe.getId();
            if (recipeId == null) return;

            var link = CRTLookup.getLink(recipeId);
            if (link == null) return;

            int pX = recipe.getDisplayWidth() - 5;
            int pY = recipe.getDisplayHeight() - 3;
            int size = RecipeIndicator.RENDER_SIZE - 1;

            widgets.addDrawable(0, 0, 0, 0, (guiGraphics, mX, mY, delta) ->
                    RecipeIndicator.renderIndicator(guiGraphics, pX, pY, size));
            widgets.addTooltipText(RecipeIndicator.constructTooltip(link), pX, pY, size, size);
        }
    }
}
