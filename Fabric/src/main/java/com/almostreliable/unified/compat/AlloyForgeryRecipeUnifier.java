package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.almostreliable.unified.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class AlloyForgeryRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.OUTPUT, this::replaceTagOutput);

    }

    @Nullable
    private JsonElement replaceTagOutput(JsonElement element, RecipeContext ctx) {
        if (!(element instanceof JsonObject json)) {
            return null;
        }

        if (json.get("priority") instanceof JsonArray && json.get("default") instanceof JsonPrimitive primitive) {
            ResourceLocation item = ctx.getPreferredItemForTag(Utils.toItemTag(primitive.getAsString()), $ -> true);
            if (item != null) {
                json.addProperty("id", item.toString());
                json.remove("priority");
                json.remove("default");
                return element;
            }

            return null;
        }

        return ctx.createResultReplacement(element, false, "id");
    }
}
