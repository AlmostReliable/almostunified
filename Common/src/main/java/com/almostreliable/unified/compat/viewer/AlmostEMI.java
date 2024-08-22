package com.almostreliable.unified.compat.viewer;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
@EmiEntrypoint
public class AlmostEMI implements EmiPlugin {

    @Override
    public void initialize(EmiInitRegistry registry) {
        if (!BuiltInRegistries.ITEM.getTagOrEmpty(ItemHider.EMI_STRICT_TAG).iterator().hasNext()) return;
        for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(ItemHider.HIDE_TAG)) {
            registry.disableStack(EmiStack.of(new ItemStack(itemHolder)));
        }
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeDecorator(new IndicatorDecorator());

        if (BuiltInRegistries.ITEM.getTagOrEmpty(ItemHider.EMI_STRICT_TAG).iterator().hasNext()) return;
        for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(ItemHider.HIDE_TAG)) {
            registry.removeEmiStacks(EmiStack.of(new ItemStack(itemHolder)));
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
