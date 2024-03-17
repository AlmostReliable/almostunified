package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

import java.util.List;

public class BloodMagicRecipeUnifier implements RecipeUnifier {

    public static final String ADDED_INPUT = "addedinput";
    public static final String ADDED_OUTPUT = "addedoutput";
    public static final String BASE_INPUT = "baseinput";
    public static final String INPUT0 = "input0";
    public static final String INPUT1 = "input1";
    public static final String INPUT2 = "input2";
    public static final String INPUT3 = "input3";
    public static final String TOOL = "tool";
    public static final String TYPE = "type";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(
                // arc
                TOOL,
                // array
                ADDED_INPUT,
                BASE_INPUT,
                // soulforge
                INPUT0,
                INPUT1,
                INPUT2,
                INPUT3
        ).forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json)));

        // arc
        builder.put(ADDED_OUTPUT, (json, ctx) -> ctx.createResultReplacement(json, false, TYPE, RecipeConstants.ITEM));
    }
}
