package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class AmethystImbuementRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        final List<String> fields = List.of("imbueA",
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
                "craftI");

        fields.forEach(field -> builder.put(field, (json, ctx) -> ctx.createIngredientReplacement(json)));
        builder.put("resultA", (json, ctx) -> ctx.createResultReplacement(json));
    }
}
