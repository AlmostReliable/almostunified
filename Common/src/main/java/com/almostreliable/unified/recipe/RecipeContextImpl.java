package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.Utils;
import com.almostreliable.unified.utils.json.JsonCursor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

@SuppressWarnings("SameParameterValue")
public class RecipeContextImpl implements RecipeContext {

    private final ReplacementMap replacementMap;

    public RecipeContextImpl(ReplacementMap replacementMap) {
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
    public ResourceLocation getPreferredItemForTag(@Nullable TagKey<Item> tag) {
        if (tag == null) {
            return null;
        }

        return replacementMap.getPreferredItemForTag(tag);
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
    public void replaceBasicInput(JsonCursor cursor) {
        if (cursor.isArray()) {
            cursor.walk(this::replaceBasicInput);
            return;
        }

        if (cursor.isObject()) {
            cursor.next(RecipeConstants.VALUE).ifPresent(this::replaceBasicInput);
            cursor.next(RecipeConstants.BASE).ifPresent(this::replaceBasicInput);
            cursor.next(RecipeConstants.INGREDIENT).ifPresent(this::replaceBasicInput);
            cursor.next(RecipeConstants.TAG).ifPresent(this::tryReplaceTagOwnership);
            tryReplaceItemWithTag(cursor);
        }
    }

    @Override
    public void replaceBasicOutput(JsonCursor cursor) {
        replaceBasicOutput(cursor, true, RecipeConstants.ITEM);
    }

    @Override
    public void replaceBasicOutput(JsonCursor cursor, boolean replaceTag, String... keyLookups) {
        if (cursor.isPrimitive()) {
            var item = ResourceLocation.tryParse(cursor.valueAsString());
            var replacement = getReplacementForItem(item);
            if (replacement != null) {
                cursor.set(replacement.toString());
            }

            return;
        }

        if (cursor.isArray()) {
            cursor.walk(c -> replaceBasicOutput(c, replaceTag, keyLookups));
            return;
        }

        if (cursor.isObject()) {
            for (String key : keyLookups) {
                cursor.next(key).ifPresent(c -> replaceBasicOutput(c, replaceTag, keyLookups));
            }

            if (replaceTag) {
                tryReplaceTagWithItem(cursor);
            }
        }
    }

    private void tryReplaceItemWithTag(JsonCursor cursor) {
        if (!(cursor.value() instanceof JsonObject obj)) {
            return;
        }

        TagKey<Item> tag = cursor
                .next(RecipeConstants.ITEM)
                .filter(JsonCursor::isPrimitive)
                .map(c -> getPreferredTagForItem(ResourceLocation.tryParse(c.valueAsString())))
                .orElse(null);
        if (tag != null) {
            obj.remove(RecipeConstants.ITEM);
            obj.addProperty(RecipeConstants.TAG, tag.location().toString());
        }
    }

    private void tryReplaceTagWithItem(JsonCursor cursor) {
        if (!(cursor.value() instanceof JsonObject obj)) {
            return;
        }

        var itemId = cursor
                .next(RecipeConstants.TAG)
                .filter(JsonCursor::isPrimitive)
                .map(primitive -> getPreferredItemForTag(Utils.toItemTag(primitive.valueAsString())))
                .orElse(null);
        if (itemId != null) {
            obj.remove(RecipeConstants.TAG);
            obj.addProperty(RecipeConstants.ITEM, itemId.toString());
        }
    }

    private void tryReplaceTagOwnership(JsonCursor cursor) {
        if (!cursor.isPrimitive()) {
            return;
        }

        var tag = Utils.toItemTag(cursor.valueAsString());
        var owner = replacementMap.getTagOwnerships().getOwnerByTag(tag);
        if (owner != null) {
            cursor.set(owner.location().toString());
        }
    }
}
