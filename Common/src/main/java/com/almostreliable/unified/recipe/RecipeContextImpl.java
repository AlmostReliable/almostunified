package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class RecipeContextImpl implements RecipeContext {

    private static final List<String> DEFAULT_INPUT_DEPTH_LOOKUPS = List.of(
            RecipeConstants.VALUE,
            RecipeConstants.BASE,
            RecipeConstants.INGREDIENT
    );
    private final UnifyLookup unifyLookup;

    public RecipeContextImpl(UnifyLookup unifyLookup) {
        this.unifyLookup = unifyLookup;
    }

    @Override
    public UnifyLookup getLookup() {
        return unifyLookup;
    }

    @Override
    public void unifyInputs(RecipeJson recipe, String recipeKey) {
        var element = recipe.getProperty(recipeKey);
        if (element != null) {
            unifyBasicInput(element);
        }
    }

    @Override
    public void unifyInputs(RecipeJson recipe, Iterable<String> recipeKeys) {
        for (String recipeKey : recipeKeys) {
            unifyInputs(recipe, recipeKey);
        }
    }

    @Override
    public boolean unifyBasicInput(JsonElement jsonElement, Iterable<String> depthInputLookups) {
        if (jsonElement instanceof JsonArray array) {
            return unifySimpleInputs(array, depthInputLookups);
        }

        if (jsonElement instanceof JsonObject object) {
            return unifySimpleInputs(object, depthInputLookups);
        }

        return false;
    }

    @Override
    public boolean unifyBasicInput(JsonElement jsonElement) {
        return unifyBasicInput(jsonElement, DEFAULT_INPUT_DEPTH_LOOKUPS);
    }

    @Override
    public boolean unifySimpleInputs(JsonArray json, Iterable<String> depthInputLookups) {
        boolean changed = false;

        for (JsonElement element : json) {
            changed |= unifyBasicInput(element, depthInputLookups);
        }

        return changed;
    }

    @Override
    public boolean unifySimpleInputs(JsonArray json) {
        return unifySimpleInputs(json, DEFAULT_INPUT_DEPTH_LOOKUPS);
    }

    @Override
    public boolean unifySimpleInputs(JsonObject json, Iterable<String> depthInputLookups) {
        boolean changed = false;

        for (String key : depthInputLookups) {
            var element = json.get(key);
            if (element != null) {
                changed |= unifyBasicInput(element);
            }
        }

        changed |= unifyTagInput(json);
        changed |= unifyItemInput(json);

        return changed;
    }

    @Override
    public boolean unifySimpleInputs(JsonObject json) {
        return unifySimpleInputs(json, DEFAULT_INPUT_DEPTH_LOOKUPS);
    }

    @Override
    public boolean unifyItemInput(JsonObject json) {
        if (json.get(RecipeConstants.ITEM) instanceof JsonPrimitive primitive) {
            ResourceLocation item = ResourceLocation.parse(primitive.getAsString());
            var tag = getLookup().getPreferredTagForItem(item);
            if (tag != null) {
                json.remove(RecipeConstants.ITEM);
                json.addProperty(RecipeConstants.TAG, tag.location().toString());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean unifyTagInput(JsonObject json) {
        if (json.get(RecipeConstants.TAG) instanceof JsonPrimitive primitive) {
            var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(primitive.getAsString()));
            var ownerTag = unifyLookup.getTagOwnerships().getOwner(tag);
            if (ownerTag != null) {
                json.addProperty(RecipeConstants.TAG, ownerTag.location().toString());
                return true;
            }
        }

        return false;
    }

    @Override
    public void unifyOutputs(RecipeJson recipe, String recipeKey) {
        unifyOutputs(recipe, true, recipeKey);
    }

    @Override
    public void unifyOutputs(RecipeJson recipe, Iterable<String> recipeKeys) {
        for (String recipeKey : recipeKeys) {
            unifyOutputs(recipe, recipeKey);
        }
    }

    @Override
    public void unifyOutputs(RecipeJson recipe, String recipeKey, boolean unifyTagToItems, String... nestedLookupKeys) {
        var element = recipe.getProperty(recipeKey);
        if (element == null) return;

        if (element instanceof JsonPrimitive primitive) {
            var replacement = createOutputReplacement(primitive);
            if (replacement != null) {
                recipe.setProperty(recipeKey, replacement);
            }

            return;
        }

        unifyBasicOutput(element, unifyTagToItems, nestedLookupKeys);
    }

    @Override
    public void unifyOutputs(RecipeJson recipe, boolean unifyTagToItems, String... keys) {
        for (String key : keys) {
            unifyOutputs(recipe, key, unifyTagToItems, RecipeConstants.ID);
        }
    }

    @Override
    public boolean unifyBasicOutput(JsonObject json, boolean unifyTagToItems, String... lookupKeys) {
        boolean changed = false;

        for (String lookupKey : lookupKeys) {
            var element = json.get(lookupKey);
            if (element == null) continue;

            if (element instanceof JsonPrimitive primitive) {
                var replacement = createOutputReplacement(primitive);
                if (replacement != null) {
                    json.add(lookupKey, replacement);
                    changed = true;
                }

                continue;
            }

            if (unifyBasicOutput(element, unifyTagToItems, lookupKeys)) {
                changed = true;
            }
        }

        if (unifyTagToItems && json.get(RecipeConstants.TAG) instanceof JsonPrimitive primitive) {
            var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(primitive.getAsString()));
            var entry = getLookup().getPreferredEntryForTag(tag);
            if (entry != null) {
                json.remove(RecipeConstants.TAG);
                json.addProperty(RecipeConstants.ID, entry.id().toString());
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean unifyBasicOutput(JsonArray json, boolean unifyTagToItems, String... lookupKeys) {
        boolean changed = false;

        for (int i = 0; i < json.size(); i++) {
            var element = json.get(i);
            if (element == null) continue;

            if (element instanceof JsonPrimitive primitive) {
                var replacement = createOutputReplacement(primitive);
                if (replacement != null) {
                    json.set(i, replacement);
                    changed = true;
                }

                continue;
            }

            if (unifyBasicOutput(element, unifyTagToItems, lookupKeys)) {
                changed = true;
            }
        }

        return changed;
    }

    private boolean unifyBasicOutput(JsonElement json, boolean tagLookup, String... lookupKeys) {
        if (json instanceof JsonObject obj) {
            return unifyBasicOutput(obj, tagLookup, lookupKeys);
        }

        if (json instanceof JsonArray arr) {
            return unifyBasicOutput(arr, tagLookup, lookupKeys);
        }

        return false;
    }

    @Override
    @Nullable
    public JsonPrimitive createOutputReplacement(JsonPrimitive primitive) {
        ResourceLocation item = ResourceLocation.parse(primitive.getAsString());
        var entry = getLookup().getReplacementForItem(item);
        if (entry == null || entry.id().equals(item)) {
            return null;
        }

        return new JsonPrimitive(entry.id().toString());
    }
}
