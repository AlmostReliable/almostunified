package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ImmersiveEngineeringRecipeUnifier implements RecipeUnifier {

    // sub keys
    private static final String BASE_KEY = "base_ingredient";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.INPUT_0, this::createIngredientReplacement); // alloy recipes, refinery
        builder.put(RecipeConstants.INPUT_1, this::createIngredientReplacement); // alloy recipes, refinery
        builder.put(RecipeConstants.INPUT,
                this::createIngredientReplacement); // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
        builder.put(RecipeConstants.ADDITIVES, this::createIngredientReplacement); // arc furnace
        builder.put(RecipeConstants.INPUTS, this::createIngredientReplacement); // blueprint, mixer
        // alloy recipes, crusher
        builder.forEachObject(RecipeConstants.SECONDARIES,
                (jsonObject, context) ->
                        createResultReplacement(jsonObject.get(RecipeConstants.OUTPUT), context) instanceof JsonObject
                        ? jsonObject : null);
        builder.put(RecipeConstants.RESULT, this::createResultReplacement);
        builder.put(RecipeConstants.RESULTS, this::createResultReplacement);
        builder.put(RecipeConstants.SLAG, this::createResultReplacement); // arc furnace
    }

    @Nullable
    private JsonElement createResultReplacement(@Nullable JsonElement element, RecipeContext context) {
        if (element instanceof JsonArray array) {
            if (JsonUtils.replaceOn(array, e -> createResultReplacement(e, context))) {
                return array;
            }
        }

        if (element instanceof JsonObject object) {
            JsonElement tag = object.get(RecipeConstants.TAG);
            if (tag != null) {
                /*
                 * Immersive Engineering allows tags in result and filters them. So we replace the tags with
                 * the preferred item from our config.
                 */
                ResourceLocation item = context.getPreferredItemForTag(Utils.toItemTag(object
                        .get(RecipeConstants.TAG)
                        .getAsString()), $ -> true);
                if (item != null) {
                    object.remove(RecipeConstants.TAG);
                    object.addProperty(RecipeConstants.ITEM, item.toString());
                    return object;
                }
            }

            JsonElement base = object.get(BASE_KEY);
            if (base != null) {
                JsonElement baseResult = createResultReplacement(base, context);
                if (baseResult != null) {
                    object.add(BASE_KEY, baseResult);
                    return object;
                }
            }
        }

        return context.createResultReplacement(element);
    }

    @Nullable
    private JsonElement createIngredientReplacement(@Nullable JsonElement element, RecipeContext context) {
        if (element instanceof JsonObject json && json.has(BASE_KEY)) {
            return context.createIngredientReplacement(json.get(BASE_KEY));
        }

        return context.createIngredientReplacement(element);
    }
}
