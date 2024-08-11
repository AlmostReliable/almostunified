package com.almostreliable.unified.api.recipe;

import java.util.Set;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public interface RecipeConstants {

    // inputs
    String ITEM = "item";
    String TAG = "tag";
    String INPUT = "input";
    String INPUTS = "inputs";
    String INGREDIENT = "ingredient";
    String INGREDIENTS = "ingredients";
    String INPUT_ITEMS = "inputItems";
    String CATALYST = "catalyst";

    // outputs
    String OUTPUT = "output";
    String OUTPUTS = "outputs";
    String RESULT = "result";
    String RESULTS = "results";
    String OUTPUT_ITEMS = "outputItems";

    // inner keys
    String VALUE = "value";
    String BASE = "base";
    String ID = "id";

    // defaults
    String[] DEFAULT_INPUT_KEYS = {
            INPUT,
            INPUTS,
            INGREDIENT,
            INGREDIENTS,
            INPUT_ITEMS
    };
    String[] DEFAULT_INPUT_INNER_KEYS = {
            VALUE,
            BASE,
            INGREDIENT
    };
    Set<String> DEFAULT_OUTPUT_KEYS = Set.of(
            OUTPUT,
            OUTPUTS,
            RESULT,
            RESULTS,
            OUTPUT_ITEMS
    );
    String[] DEFAULT_OUTPUT_INNER_KEYS = {
            ITEM,
            INGREDIENT
    };
}
