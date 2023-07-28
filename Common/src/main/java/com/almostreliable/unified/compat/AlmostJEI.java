package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedFallbackRuntime;
import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.CRTLookup;
import com.almostreliable.unified.recipe.ClientRecipeTracker.ClientRecipeLink;
import com.almostreliable.unified.utils.Utils;
import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

@REIPluginCompatIgnore
@JeiPlugin
public class AlmostJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return Utils.getRL(ModConstants.JEI);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        AlmostUnifiedFallbackRuntime.getInstance().reload();

        Boolean jeiDisabled = AlmostUnified.getRuntime()
                .getUnifyConfig()
                .map(UnifyConfig::reiOrJeiDisabled)
                .orElse(false);
        if (jeiDisabled) return;

        Collection<ItemStack> items = HideHelper.createHidingList(AlmostUnified.getRuntime());
        if (!items.isEmpty()) {
            jei.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, items);
        }
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        var recipeTypes = registration.getJeiHelpers().getAllRecipeTypes();
        recipeTypes.forEach(rt -> registration.addRecipeCategoryDecorator(rt, new Decorator<>()));
    }

    private static class Decorator<T> implements IRecipeCategoryDecorator<T> {

        private static final int RECIPE_BORDER_PADDING = 4;

        @Override
        public void draw(T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
            var recipeLink = resolveLink(recipeCategory, recipe);
            if (recipeLink == null) return;

            var pX = recipeCategory.getWidth() + (2 * RECIPE_BORDER_PADDING) - RecipeIndicator.RENDER_SIZE;
            var pY = recipeCategory.getHeight() + (2 * RECIPE_BORDER_PADDING) - RecipeIndicator.RENDER_SIZE;
            RecipeIndicator.renderIndicator(guiGraphics, pX, pY, RecipeIndicator.RENDER_SIZE);

            if (mouseX >= pX && mouseX <= pX + RecipeIndicator.RENDER_SIZE &&
                mouseY >= pY && mouseY <= pY + RecipeIndicator.RENDER_SIZE) {
                RecipeIndicator.renderTooltip(guiGraphics, recipeLink, mouseX, mouseY);
            }
        }

        @Nullable
        private static <R> ClientRecipeLink resolveLink(IRecipeCategory<R> recipeCategory, R recipe) {
            var recipeId = recipeCategory.getRegistryName(recipe);
            if (recipeId == null) return null;

            return CRTLookup.getLink(recipeId);
        }
    }
}
