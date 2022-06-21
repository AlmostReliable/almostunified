package com.almostreliable.unified.compat.ie;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import com.almostreliable.unified.handler.RecipeConstants;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class IERecipeHandler implements RecipeHandler {

    // From IE
    protected static final String BASE_KEY = "base_ingredient";

    @Override
    public void transformRecipe(JsonObject json, RecipeContext context) {

        replaceIEIngredient(json.get("input0"), context); // alloy recipes, refinery
        replaceIEIngredient(json.get("input1"), context); // alloy recipes, refinery
        replaceIEIngredient(json.get(RecipeConstants.INPUT),
                context); // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
        replaceIEIngredient(json.get("additives"), context); // arc furnace
        replaceIEIngredient(json.get(RecipeConstants.INPUTS), context); // blueprint, mixer

        replaceIEResult(json.get("secondaries"), context); // alloy recipes, crusher

        JsonUtils.arrayForEach(json.get("secondaries"), JsonObject.class, secondary -> {
            replaceIEResult(secondary.get(RecipeConstants.OUTPUT), context);
        });

        replaceIEResult(json.get(RecipeConstants.RESULT), context);
        replaceIEResult(json.get(RecipeConstants.RESULTS), context);
    }

    protected void replaceIEResult(@Nullable JsonElement element, RecipeContext context) {
        if (element == null) {
            return;
        }

        JsonUtils.arrayForEach(element, JsonObject.class, json -> {
            if (json.has(RecipeConstants.ITEM)) {
                context.replaceResult(json);
            } else if (json.has(RecipeConstants.TAG)) {
                /*
                 * Immersive Engineering allows tags in result and filters them. So we replace the tags with
                 * the preferred item from our config.
                 */
                ResourceLocation item = context.getPreferredItemByTag(Utils.toItemTag(json
                        .get(RecipeConstants.TAG)
                        .getAsString()));
                if (item != null) {
                    json.remove(RecipeConstants.TAG);
                    json.addProperty(RecipeConstants.ITEM, item.toString());
                }
            } else if (json.has(BASE_KEY)) {
                replaceIEResult(json.get(BASE_KEY), context);
            }
        });
    }

    protected void replaceIEIngredient(@Nullable JsonElement element, RecipeContext context) {
        if (element == null) {
            return;
        }

        if (element instanceof JsonObject json && json.has(BASE_KEY)) {
            context.replaceIngredient(json.get(BASE_KEY));
        } else {
            context.replaceIngredient(element);
        }
    }
}
