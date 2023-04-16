package com.almostreliable.unified.mixin;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.recipes.RecipeTransferButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeTransferButton.class)
public interface JeiRecipeTransferButtonAccessor {

    @Accessor(value = "recipeLayout", remap = false)
    IRecipeLayoutDrawable<?> getRecipeLayout();
}
