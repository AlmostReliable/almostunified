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

public class IERecipeUnifier implements RecipeUnifier {

    // From IE
    protected static final String BASE_KEY = "base_ingredient";

    // TODO make it cleaner
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put("input0", this::replaceIEIngredient); // alloy recipes, refinery
        builder.put("input1", this::replaceIEIngredient); // alloy recipes, refinery
        builder.put(RecipeConstants.INPUT,
                this::replaceIEIngredient); // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
        builder.put("additives", this::replaceIEIngredient); // arc furnace
        builder.put(RecipeConstants.INPUTS, this::replaceIEIngredient); // blueprint, mixer
        // alloy recipes, crusher
        builder.forEachObject("secondaries",
                (jsonObject, context) ->
                        replaceIEResult(jsonObject.get(RecipeConstants.OUTPUT), context) instanceof JsonObject
                        ? jsonObject : null);
        builder.put(RecipeConstants.RESULT, this::replaceIEResult);
        builder.put(RecipeConstants.RESULTS, this::replaceIEResult);
    }

    @Nullable
    protected JsonElement replaceIEResult(@Nullable JsonElement element, RecipeContext context) {
        if (element instanceof JsonArray array) {
            if (JsonUtils.replaceOn(array, e -> this.replaceIEResult(e, context))) {
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
                ResourceLocation item = context.getPreferredItemByTag(Utils.toItemTag(object
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
                JsonElement baseResult = replaceIEResult(base, context);
                if (baseResult != null) {
                    object.add(BASE_KEY, baseResult);
                    return object;
                }
            }
        }

        return context.createResultReplacement(element);
    }

    @Nullable
    public JsonElement replaceIEIngredient(@Nullable JsonElement element, RecipeContext context) {
        if (element instanceof JsonObject json && json.has(BASE_KEY)) {
            return context.createIngredientReplacement(json.get(BASE_KEY));
        }

        return context.createIngredientReplacement(element);
    }
}
