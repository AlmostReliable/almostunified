package com.almostreliable.unified.unification.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.jetbrains.annotations.Nullable;

public record UnificationHelperImpl(CustomIngredientUnifierRegistry customIngredientUnifierRegistry,
                                    UnificationLookup getUnificationLookup) implements UnificationHelper {

    @Nullable
    @Override
    public CustomIngredientUnifier getCustomIngredientUnifier(ResourceLocation type) {
        return customIngredientUnifierRegistry.getCustomIngredientUnifier(type);
    }

    @Override
    public boolean unifyInputs(RecipeJson recipe, String... keys) {
        Preconditions.checkArgument(keys.length > 0, "at least one key is required");
        boolean changed = false;

        for (String key : keys) {
            JsonElement jsonElement = recipe.getProperty(key);
            if (jsonElement == null) continue;

            changed |= unifyInputElement(jsonElement);
        }

        return changed;
    }

    @Override
    public boolean unifyInputElement(@Nullable JsonElement jsonElement, String... keys) {
        return jsonElement != null && switch (jsonElement) {
            case JsonArray jsonArray -> unifyInputArray(jsonArray, keys);
            case JsonObject jsonObject -> unifyInputObject(jsonObject, keys);
            default -> false;
        };
    }

    @Override
    public boolean unifyInputArray(JsonArray jsonArray, String... keys) {
        boolean changed = false;

        for (JsonElement jsonElement : jsonArray) {
            changed |= unifyInputElement(jsonElement, keys);
        }

        return changed;
    }

    @Override
    public boolean unifyInputObject(JsonObject jsonObject, String... keys) {
        Boolean modified = handleCustomIngredientUnification(jsonObject);
        if (modified != null) return modified;

        boolean changed = false;

        for (String key : keys.length == 0 ? RecipeConstants.DEFAULT_INPUT_INNER_KEYS : keys) {
            var jsonElement = jsonObject.get(key);
            if (jsonElement == null) continue;

            changed |= unifyInputElement(jsonElement);
        }

        changed |= unifyInputTag(jsonObject);
        changed |= unifyInputItem(jsonObject);

        return changed;
    }

    @Override
    public boolean unifyInputTag(JsonObject jsonObject) {
        if (!(jsonObject.get(RecipeConstants.TAG) instanceof JsonPrimitive jsonPrimitive)) return false;

        var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(jsonPrimitive.getAsString()));
        var substituteTag = AlmostUnified.INSTANCE.getRuntimeOrThrow().getTagSubstitutions().getSubstituteTag(tag);
        if (substituteTag == null) return false;

        jsonObject.addProperty(RecipeConstants.TAG, substituteTag.location().toString());
        return true;
    }

    // TODO: add ignore list for recipes that should not have its inputs converted to tags
    @Override
    public boolean unifyInputItem(JsonObject jsonObject) {
        if (!(jsonObject.get(RecipeConstants.ITEM) instanceof JsonPrimitive jsonPrimitive)) return false;

        ResourceLocation item = ResourceLocation.parse(jsonPrimitive.getAsString());
        var tag = getUnificationLookup.getRelevantItemTag(item);
        if (tag != null) {
            jsonObject.remove(RecipeConstants.ITEM);
            jsonObject.addProperty(RecipeConstants.TAG, tag.location().toString());
            return true;
        }

        return false;
    }

    @Override
    public boolean unifyOutputs(RecipeJson recipe, String... keys) {
        return unifyOutputs(recipe, true, keys);
    }

    @Override
    public boolean unifyOutputs(RecipeJson recipe, boolean tagsToItems, String... keys) {
        Preconditions.checkArgument(keys.length > 0, "at least one key is required");
        boolean changed = false;

        for (String key : keys) {
            JsonElement jsonElement = recipe.getProperty(key);
            if (jsonElement == null) continue;

            if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
                var replacement = handleOutputItemReplacement(jsonPrimitive);
                if (replacement == null) continue;
                recipe.setProperty(key, replacement);
                changed = true;
                continue;
            }

            changed |= unifyOutputElement(jsonElement, tagsToItems);
        }

        return changed;
    }

    @Override
    public boolean unifyOutputs(RecipeJson recipe, String key, boolean tagsToItems, String... innerKeys) {
        Preconditions.checkArgument(innerKeys.length > 0, "at least one inner key is required");
        return unifyOutputElement(recipe.getProperty(key), tagsToItems, innerKeys);
    }

    @Override
    public boolean unifyOutputElement(@Nullable JsonElement json, boolean tagsToItems, String... keys) {
        return json != null && switch (json) {
            case JsonArray jsonArray -> unifyOutputArray(jsonArray, tagsToItems, keys);
            case JsonObject jsonObject -> unifyOutputObject(jsonObject, tagsToItems, keys);
            default -> false;
        };

    }

    @Override
    public boolean unifyOutputArray(JsonArray jsonArray, boolean tagsToItems, String... keys) {
        boolean changed = false;

        for (JsonElement jsonElement : jsonArray) {
            changed |= unifyOutputElement(jsonElement, tagsToItems, keys);
        }

        return changed;
    }

    @Override
    public boolean unifyOutputObject(JsonObject jsonObject, boolean tagsToItems, String... keys) {
        Boolean modified = handleCustomIngredientUnification(jsonObject);
        if (modified != null) return modified;

        boolean changed = false;

        for (String key : keys.length == 0 ? RecipeConstants.DEFAULT_OUTPUT_INNER_KEYS : keys) {
            var jsonElement = jsonObject.get(key);
            if (jsonElement == null) continue;

            changed |= unifyOutputElement(jsonElement, tagsToItems, keys);
        }

        changed |= unifyOutputTag(jsonObject, tagsToItems);
        changed |= unifyOutputItem(jsonObject);

        return changed;
    }

    @Override
    public boolean unifyOutputTag(JsonObject jsonObject, boolean tagsToItems) {
        if (!(jsonObject.get(RecipeConstants.TAG) instanceof JsonPrimitive jsonPrimitive)) return false;

        var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(jsonPrimitive.getAsString()));

        if (tagsToItems) {
            var entry = getUnificationLookup.getTagTargetItem(tag);
            if (entry == null) return false;

            jsonObject.remove(RecipeConstants.TAG);
            jsonObject.addProperty(RecipeConstants.ITEM, entry.id().toString());
            return true;
        }

        var substituteTag = AlmostUnified.INSTANCE.getRuntimeOrThrow().getTagSubstitutions().getSubstituteTag(tag);
        if (substituteTag == null) return false;

        jsonObject.addProperty(RecipeConstants.TAG, substituteTag.location().toString());
        return true;
    }

    @Override
    public boolean unifyOutputItem(JsonObject jsonObject) {
        boolean changed = unifyOutputItem(jsonObject, RecipeConstants.ITEM);
        changed |= unifyOutputItem(jsonObject, RecipeConstants.ID);
        return changed;
    }

    @Override
    public boolean unifyOutputItem(JsonObject jsonObject, String key) {
        if (!(jsonObject.get(key) instanceof JsonPrimitive jsonPrimitive)) return false;

        JsonPrimitive replacement = handleOutputItemReplacement(jsonPrimitive);
        if (replacement == null) return false;

        jsonObject.addProperty(key, replacement.getAsString());
        return true;
    }

    @Override
    @Nullable
    public JsonPrimitive handleOutputItemReplacement(JsonPrimitive jsonPrimitive) {
        ResourceLocation item = ResourceLocation.parse(jsonPrimitive.getAsString());
        var entry = getUnificationLookup.getVariantItemTarget(item);
        if (entry == null || entry.id().equals(item)) return null;
        return new JsonPrimitive(entry.id().toString());
    }

    /**
     * Handles custom ingredient unification.
     * <p>
     * This method checks if the given {@link JsonObject} has a {@code type} property and if a
     * {@link CustomIngredientUnifier} is associated with that type. If not, it will return null and the default logic
     * should continue. Otherwise, it will return whether the {@link CustomIngredientUnifier} modified the
     * {@link JsonObject}.
     *
     * @param jsonObject the ingredient {@link JsonObject} to unify
     * @return true if the ingredient was changed, false otherwise, or null if no unifier was found
     */
    @Nullable
    private Boolean handleCustomIngredientUnification(JsonObject jsonObject) {
        if (!(jsonObject.get("type") instanceof JsonPrimitive jsonPrimitive)) {
            return null;
        }

        ResourceLocation type = ResourceLocation.tryParse(jsonPrimitive.getAsString());
        if (type == null) return null;

        CustomIngredientUnifier unifier = getCustomIngredientUnifier(type);
        if (unifier == null) return null;

        jsonObject.remove("type"); // avoids looping when calling helper methods within ingredient unifiers
        boolean changed = unifier.unify(this, jsonObject);
        jsonObject.addProperty("type", type.toString());

        return changed;
    }
}
