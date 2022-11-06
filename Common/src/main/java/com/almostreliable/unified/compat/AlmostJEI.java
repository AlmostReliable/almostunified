package com.almostreliable.unified.compat;

import com.almostreliable.unified.BuildConfig;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.CRTLookup;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

@JeiPlugin
public class AlmostJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BuildConfig.MOD_ID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        UnifyConfig config = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        if (config.reiOrJeiDisabled()) {
            return;
        }

        Collection<ItemStack> items = HideHelper.createHidingList(config);
        if (!items.isEmpty()) {
            jei.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, items);
        }
    }

    public static <R> void recipeViewerIndicator(PoseStack stack, int mX, int mY, int x, int y, IRecipeCategory<R> recipeCategory, R recipe, int recipeBorderPadding) {
        var recipeId = recipeCategory.getRegistryName(recipe);
        if (recipeId == null) return;

        var link = CRTLookup.getLink(recipeId);
        if (link == null) return;

        var posX = x - RecipeIndicator.SIZE / 2 - recipeBorderPadding + 1;
        var posY = y - RecipeIndicator.SIZE / 2 - recipeBorderPadding + 1;
        var area = new Rect2i(posX, posY, 10, 10);
        RecipeIndicator.renderIndicator(stack, area);
        if (mX >= area.getX() && mX <= area.getX() + area.getWidth() &&
            mY >= area.getY() && mY <= area.getY() + area.getHeight()) {
            Utils.renderTooltip(stack, mX, mY, RecipeIndicator.constructTooltip(link));
        }
    }
}
