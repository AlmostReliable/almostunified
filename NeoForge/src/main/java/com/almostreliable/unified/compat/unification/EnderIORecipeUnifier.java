package com.almostreliable.unified.compat.unification;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class EnderIORecipeUnifier implements RecipeUnifier {

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyInputs(helper, recipe);

        if (!(recipe.getProperty(RecipeConstants.OUTPUTS) instanceof JsonArray outputArray)) {
            return;
        }

        for (JsonElement outputElement : outputArray) {
            if (!(outputElement instanceof JsonObject outputObject)) {
                continue;
            }

            if (!(outputObject.get(RecipeConstants.ITEM) instanceof JsonObject itemObject)) {
                continue;
            }

            if (itemObject.has(RecipeConstants.ID)) {
                helper.unifyOutputItem(itemObject);
                continue;
            }

            if (itemObject.get(RecipeConstants.TAG) instanceof JsonPrimitive tagPrimitive) {
                var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagPrimitive.getAsString()));
                helper.handleTagToItemReplacement(itemObject, RecipeConstants.ID, tag);
            }
        }
    }
}
