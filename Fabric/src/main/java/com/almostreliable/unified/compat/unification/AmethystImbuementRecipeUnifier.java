package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

public class AmethystImbuementRecipeUnifier implements RecipeUnifier {
    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        helper.unifyInputs(
            recipe,
            "imbueA",
            "imbueB",
            "imbueC",
            "imbueD",
            "craftA",
            "craftB",
            "craftC",
            "craftD",
            "craftE",
            "craftF",
            "craftG",
            "craftH",
            "craftI"
        );

        helper.unifyOutputs(recipe, "resultA");
    }
}
