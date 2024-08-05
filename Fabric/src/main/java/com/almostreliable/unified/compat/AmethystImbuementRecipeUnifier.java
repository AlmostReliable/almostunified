package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

import java.util.List;

public class AmethystImbuementRecipeUnifier implements RecipeUnifier {
    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        helper.unifyInputs(recipe,
                List.of("imbueA",
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
                        "craftI"));

        helper.unifyOutputs(recipe, "resultA");
    }
}
