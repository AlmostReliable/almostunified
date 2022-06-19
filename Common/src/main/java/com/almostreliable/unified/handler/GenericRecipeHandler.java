package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class GenericRecipeHandler implements RecipeHandler {
    public static final GenericRecipeHandler INSTANCE = new GenericRecipeHandler();
    private final Set<String> inputKeys = Set.of(RecipeConstants.INPUT,
            RecipeConstants.INGREDIENT,
            RecipeConstants.INGREDIENTS);
    private final Set<String> outputKeys = Set.of(RecipeConstants.OUTPUT,
            RecipeConstants.RESULT,
            RecipeConstants.RESULTS);

    public boolean hasInputOrOutputProperty(RecipeContext property) {
        for (String inputKey : inputKeys) {
            if (property.hasProperty(inputKey)) {
                return true;
            }
        }

        for (String outputKey : outputKeys) {
            if (property.hasProperty(outputKey)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void transformRecipe(JsonObject json, RecipeContext context) {
        for (String inputKey : inputKeys) {
            if (json.has(inputKey)) {
                context.replaceIngredient(json.get(inputKey));
            }
        }

        for (String outputKey : outputKeys) {
            JsonElement jsonElement = json.get(outputKey);
            if (jsonElement instanceof JsonPrimitive) {
                ResourceLocation item = context.getReplacementForItem(ResourceLocation.tryParse(jsonElement.getAsString()));
                if (item != null) {
                    json.addProperty(outputKey, item.toString());
                }
            } else if (jsonElement != null) {
                // Forge patched recipe results to also allow JsonObjects. See SimpleCookingSerializer::fromJson as an example.
                context.replaceResult(jsonElement);
            }

        }
    }
}
