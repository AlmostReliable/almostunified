package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class RecipeContextImpl implements RecipeContext {

    private final List<String> defaultInputDepthLookups = List.of(
            RecipeConstants.VALUE,
            RecipeConstants.BASE,
            RecipeConstants.INGREDIENT
    );
    private final UnifyLookup unifyLookup;

    public RecipeContextImpl(UnifyLookup unifyLookup) {
        this.unifyLookup = unifyLookup;
    }

    @Nullable
    @Override
    public ResourceLocation getReplacementForItem(@Nullable ResourceLocation item) {
        if (item == null) {
            return null;
        }

        UnifyEntry<Item> entry = unifyLookup.getReplacementForItem(item);
        if (entry == null) {
            return null;
        }

        return entry.id();
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(@Nullable TagKey<Item> tag) {
        if (tag == null) {
            return null;
        }

        UnifyEntry<Item> entry = unifyLookup.getPreferredItemForTag(tag);
        if (entry == null) {
            return null;
        }

        return entry.id();
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(@Nullable ResourceLocation item) {
        if (item == null) {
            return null;
        }

        return unifyLookup.getPreferredTagForItem(item);
    }

    @Nullable
    @Override
    public JsonElement createIngredientReplacement(@Nullable JsonElement element) {
        if (element == null) {
            return null;
        }

        JsonElement copy = element.deepCopy();
        tryCreateIngredientReplacement(copy);
        return element.equals(copy) ? null : copy;
    }

    private void tryCreateIngredientReplacement(@Nullable JsonElement element) {
        if (element instanceof JsonArray array) {
            for (JsonElement e : array) {
                tryCreateIngredientReplacement(e);
            }
        }

        if (element instanceof JsonObject object) {
            tryCreateIngredientReplacement(object.get(RecipeConstants.VALUE));
            tryCreateIngredientReplacement(object.get(RecipeConstants.BASE));
            tryCreateIngredientReplacement(object.get(RecipeConstants.INGREDIENT));

            if (object.get(RecipeConstants.TAG) instanceof JsonPrimitive primitive) {
                var tag = Utils.toItemTag(primitive.getAsString());
                var ownerTag = unifyLookup.getTagOwnerships().getOwner(tag);
                if (ownerTag != null) {
                    object.addProperty(RecipeConstants.TAG, ownerTag.location().toString());
                }
            }

            if (object.get(RecipeConstants.ITEM) instanceof JsonPrimitive primitive) {
                ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
                var tag = getPreferredTagForItem(item);
                if (tag != null) {
                    object.remove(RecipeConstants.ITEM);
                    object.addProperty(RecipeConstants.TAG, tag.location().toString());
                }
            }
        }
    }

    @Override
    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element) {
        return createResultReplacement(element, true, RecipeConstants.ITEM);
    }

    @Override
    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup, String... lookupKeys) {
        if (element == null) {
            return null;
        }

        JsonElement copy = element.deepCopy();
        JsonElement result = tryCreateResultReplacement(copy, tagLookup, lookupKeys);
        return element.equals(result) ? null : result;
    }

    @Nullable
    private JsonElement tryCreateResultReplacement(JsonElement element, boolean tagLookup, String... lookupKeys) {
        if (element instanceof JsonPrimitive primitive) {
            ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
            ResourceLocation replacement = getReplacementForItem(item);
            if (replacement != null) {
                return new JsonPrimitive(replacement.toString());
            }
            return null;
        }

        if (element instanceof JsonArray array &&
            JsonUtils.replaceOn(array, j -> tryCreateResultReplacement(j, tagLookup, lookupKeys))) {
            return element;
        }

        if (element instanceof JsonObject object) {
            for (String key : lookupKeys) {
                if (JsonUtils.replaceOn(object, key, j -> tryCreateResultReplacement(j, tagLookup, lookupKeys))) {
                    return element;
                }
            }

            // when tags are used as outputs, replace them with the preferred item
            if (tagLookup && object.get(RecipeConstants.TAG) instanceof JsonPrimitive primitive) {
                ResourceLocation item = getPreferredItemForTag(Utils.toItemTag(primitive.getAsString()));
                if (item != null) {
                    object.remove(RecipeConstants.TAG);
                    object.addProperty(RecipeConstants.ITEM, item.toString());
                }
                return element;
            }
        }

        return null;
    }


    @Override
    public void unifyInputs(RecipeJson recipe, String recipeKey) {
        var element = recipe.getProperty(recipeKey);
        if (element != null && unifyBasicInput(element)) {
            recipe.markChanged();
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
        return unifyBasicInput(jsonElement, defaultInputDepthLookups);
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
        return unifySimpleInputs(json, defaultInputDepthLookups);
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
        return unifySimpleInputs(json, defaultInputDepthLookups);
    }

    @Override
    public boolean unifyItemInput(JsonObject json) {
        if (json.get(RecipeConstants.ITEM) instanceof JsonPrimitive primitive) {
            ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
            var tag = getPreferredTagForItem(item);
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
            var tag = Utils.toItemTag(primitive.getAsString());
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
                recipe.markChanged();
            }

            return;
        }

        if (unifyBasicOutput(element, unifyTagToItems, nestedLookupKeys)) {
            recipe.markChanged();
        }
    }

    @Override
    public void unifyOutputs(RecipeJson recipe, boolean unifyTagToItems, String... keys) {
        for (String key : keys) {
            unifyOutputs(recipe, key, unifyTagToItems, RecipeConstants.ITEM);
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
            ResourceLocation item = getPreferredItemForTag(Utils.toItemTag(primitive.getAsString()));
            if (item != null) {
                json.remove(RecipeConstants.TAG);
                json.addProperty(RecipeConstants.ITEM, item.toString());
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
        ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
        ResourceLocation replacement = getReplacementForItem(item);
        if (replacement != null && !replacement.equals(item)) {
            return new JsonPrimitive(replacement.toString());
        }

        return null;
    }
}
