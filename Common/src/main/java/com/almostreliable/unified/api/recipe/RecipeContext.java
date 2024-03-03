package com.almostreliable.unified.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public interface RecipeContext {

    @Nullable
    ResourceLocation getReplacementForItem(@Nullable ResourceLocation item);

    @Nullable
    ResourceLocation getPreferredItemForTag(@Nullable TagKey<Item> tag);

    @Nullable
    TagKey<Item> getPreferredTagForItem(@Nullable ResourceLocation item);

    @Nullable
    JsonElement createIngredientReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element, boolean includeTagCheck, String... lookupKeys);

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
