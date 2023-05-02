package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public class ArsNouveauRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        // imbuement, enchanting apparatus, enchantment, reactive enchantment, spell write, armor upgrade
        builder.forEachObject(RecipeConstants.PEDESTAL_ITEMS, this::createIngredientReplacement);
        // glyph
        builder.forEachObject(RecipeConstants.INPUT_ITEMS, this::createIngredientReplacement);

        // enchanting apparatus
        builder.put(RecipeConstants.REAGENT, (json, ctx) -> ctx.createIngredientReplacement(json));
    }

    /**
     * Handles ingredient replacements for JSON arrays with nested item JSON objects.
     *
     * @param json The JSON object to apply the replacement on.
     * @param ctx  The recipe context.
     * @return The JSON object with the replacement applied, or {@code null} if there is no replacement.
     */
    @Nullable
    private JsonObject createIngredientReplacement(JsonObject json, RecipeContext ctx) {
        var replacement = ctx.createIngredientReplacement(json.get(RecipeConstants.ITEM));
        if (replacement instanceof JsonObject item) {
            json.add(RecipeConstants.ITEM, item);
            return json;
        }
        return null;
    }
}
