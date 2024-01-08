package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@SuppressWarnings("SameParameterValue")
public class RecipeContextImpl implements RecipeContext {

    private final ReplacementMap replacementMap;
    private final JsonObject originalRecipe;

    public RecipeContextImpl(JsonObject json, ReplacementMap replacementMap) {
        this.originalRecipe = json;
        this.replacementMap = replacementMap;
    }

    @Nullable
    @Override
    public ResourceLocation getReplacementForItem(@Nullable ResourceLocation item) {
        if (item == null) {
            return null;
        }

        return replacementMap.getReplacementForItem(item);
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(@Nullable TagKey<Item> tag, Predicate<ResourceLocation> filter) {
        if (tag == null) {
            return null;
        }

        return replacementMap.getPreferredItemForTag(tag, filter);
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(@Nullable ResourceLocation item) {
        if (item == null) {
            return null;
        }

        return replacementMap.getPreferredTagForItem(item);
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
                var ownerTag = replacementMap.getTagOwnerships().getOwnerByTag(tag);
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
                ResourceLocation item = getPreferredItemForTag(Utils.toItemTag(primitive.getAsString()), $ -> true);
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
    public ResourceLocation getType() {
        String type = originalRecipe.get("type").getAsString();
        return new ResourceLocation(type);
    }

    @Override
    public boolean hasProperty(String property) {
        return originalRecipe.has(property);
    }
}
