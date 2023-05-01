package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public class ArsNouveauRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        // pedestalItems is a JsonArray with a JsonObject of {"item":Ingredient}
        builder.put(RecipeConstants.PEDESTAL_ITEMS, JsonArray.class, (json, ctx) -> {
            for (int i = 0; i < json.size(); i++) {
                JsonElement entry = json.get(i);
                JsonElement result = null;
                if (entry instanceof JsonObject jsonEntry) {
                    result = ctx.createIngredientReplacement(jsonEntry.get(RecipeConstants.ITEM));
                }
                if (result != null) {
                    JsonObject resultObj = new JsonObject();
                    resultObj.add(RecipeConstants.ITEM, result);
                    json.set(i, resultObj);
                }
            }
            return json;
        });
    }

    @Nullable
    private JsonElement createIngredientReplacement(@Nullable JsonElement element, RecipeContext ctx) {
        if (element instanceof JsonObject json && json.has(RecipeConstants.ITEM)) {
            return ctx.createIngredientReplacement(json.get(RecipeConstants.ITEM));
        }
        return ctx.createIngredientReplacement(element);
    }
}
