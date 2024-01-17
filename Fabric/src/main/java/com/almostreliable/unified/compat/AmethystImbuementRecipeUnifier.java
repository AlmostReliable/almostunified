package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class AmethystImbuementRecipeUnifier implements RecipeUnifier {
    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);

        context.unifyInputs(recipe,
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

        context.unifyOutputs(recipe, "resultA");
    }
}
