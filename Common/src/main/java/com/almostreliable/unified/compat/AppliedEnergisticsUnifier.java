package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public class AppliedEnergisticsUnifier implements RecipeUnifier {

    public static final String DROPS = "drops";
    public static final String TOP = "top";
    public static final String MIDDLE = "middle";
    public static final String BOTTOM = "bottom";
    public static final String AMMO = "ammo";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        // entropy
        builder.put(RecipeConstants.OUTPUT, this::createOutputDropsReplacement);
        // inscriber
        builder.put(
                RecipeConstants.INGREDIENTS,
                (json, ctx) -> ctx.createIngredientReplacement(json, TOP, MIDDLE, BOTTOM)
        );
        // matter cannon
        builder.put(AMMO, (json, ctx) -> ctx.createIngredientReplacement(json));
    }

    @Nullable
    private JsonElement createOutputDropsReplacement(JsonElement json, RecipeContext ctx) {
        if (json instanceof JsonObject jsonObject && jsonObject.has(DROPS)) {
            JsonElement resultReplacement = ctx.createResultReplacement(
                    jsonObject.get(DROPS),
                    false,
                    RecipeConstants.ITEM
            );
            if (resultReplacement != null) {
                jsonObject.add(DROPS, resultReplacement);
                return jsonObject;
            }
        }

        return null;
    }
}
