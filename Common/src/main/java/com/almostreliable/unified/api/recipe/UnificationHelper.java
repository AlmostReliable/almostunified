package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.api.UnifyLookup;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * Helper to aid in recipe unification.
 * <p>
 * An instance of this is passed to {@link RecipeUnifier#unify(UnificationHelper, RecipeJson)} to help unifying the
 * given recipe JSON.
 */
public interface UnificationHelper {

    UnifyLookup getLookup();

    void unifyInputs(RecipeJson recipe, String recipeKey);

    void unifyInputs(RecipeJson recipe, Iterable<String> recipeKeys);

    boolean unifyBasicInput(JsonElement jsonElement, Iterable<String> depthInputLookups);

    boolean unifyBasicInput(JsonElement jsonElement);

    boolean unifySimpleInputs(JsonArray json, Iterable<String> depthInputLookups);

    boolean unifySimpleInputs(JsonArray json);

    boolean unifySimpleInputs(JsonObject json, Iterable<String> depthInputLookups);

    boolean unifySimpleInputs(JsonObject json);

    boolean unifyItemInput(JsonObject json);

    boolean unifyTagInput(JsonObject json);

    void unifyOutputs(RecipeJson recipe, String recipeKey);

    void unifyOutputs(RecipeJson recipe, Iterable<String> recipeKeys);

    void unifyOutputs(RecipeJson recipe, String recipeKey, boolean unifyTagToItems, String... nestedLookupKeys);

    void unifyOutputs(RecipeJson recipe, boolean unifyTagToItems, String... keys);

    boolean unifyBasicOutput(JsonObject json, boolean unifyTagToItems, String... lookupKeys);

    boolean unifyBasicOutput(JsonArray json, boolean unifyTagToItems, String... lookupKeys);

    @Nullable
    JsonPrimitive createOutputReplacement(JsonPrimitive primitive);
}
