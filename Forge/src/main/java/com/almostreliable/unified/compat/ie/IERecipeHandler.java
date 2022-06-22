package com.almostreliable.unified.compat.ie;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import com.almostreliable.unified.api.RecipeTransformations;
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

    // TODO make it cleaner
    @Override
    public void collectTransformations(RecipeTransformations builder) {
        builder.put("input0", (json, context) -> {
            replaceIEIngredient(json, context);
            return json;
        }); // alloy recipes, refinery
        builder.put("input1", (json, context) -> {
            replaceIEIngredient(json, context);
            return json;
        }); // alloy recipes, refinery
        builder.put(RecipeConstants.INPUT, (json, context) -> {
            replaceIEIngredient(json, context);
            return json;
        }); // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
        builder.put("additives", (json, context) -> {
            replaceIEIngredient(json, context);
            return json;
        }); // arc furnace
        builder.put(RecipeConstants.INPUTS, (json, context) -> {
            replaceIEIngredient(json, context);
            return json;
        }); // blueprint, mixer

        // alloy recipes, crusher
        builder.forEachObject("secondaries", (jsonObject, context) -> {
            replaceIEResult(jsonObject.get(RecipeConstants.OUTPUT), context);
            return jsonObject;
        });

        builder.put(RecipeConstants.RESULT, (json, context) -> {
            replaceIEResult(json, context);
            return json;
        });

        builder.put(RecipeConstants.RESULTS, (json, context) -> {
            replaceIEResult(json, context);
            return json;
        });
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
