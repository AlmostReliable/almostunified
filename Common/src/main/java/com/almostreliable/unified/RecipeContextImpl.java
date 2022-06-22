package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.handler.RecipeConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

@SuppressWarnings("SameParameterValue")
public class RecipeContextImpl implements RecipeContext {
    private final ResourceLocation type;
    private final ResourceLocation id;
    private final JsonObject currentRecipe;
    private final ReplacementMap replacementMap;

    public RecipeContextImpl(ResourceLocation type, ResourceLocation id, JsonObject currentRecipe, ReplacementMap replacementMap) {
        this.type = type;
        this.id = id;
        this.currentRecipe = currentRecipe;
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
    public ResourceLocation getPreferredItemByTag(TagKey<Item> tag) {
        return replacementMap.getPreferredItemByTag(tag);
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagByItem(@Nullable ResourceLocation item) {
        if (item == null) {
            return null;
        }

        return replacementMap.getPreferredTag(item);
    }

    @Nullable
    protected JsonElement depthReplace(JsonElement element, String potentialFrom, String potentialTo, UnaryOperator<JsonPrimitive> primitiveCallback) {
        if (element instanceof JsonPrimitive primitive) {
            return primitiveCallback.apply(primitive);
        }

        if (element instanceof JsonObject object) {
            JsonElement replace = depthReplace(object.get(potentialFrom), potentialFrom, potentialTo, primitiveCallback);
            if (replace != null) {
                object.remove(potentialFrom);
                object.add(potentialTo, replace);
            }
        }

        if (element instanceof JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                JsonElement replace = depthReplace(array.get(i), potentialFrom, potentialTo, primitiveCallback);
                if (replace != null) {
                    array.set(i, replace);
                }
            }
        }

        return null;
    }

    @Override
    @Nullable
    public JsonElement replaceIngredient(JsonElement element) {
        return depthReplace(element, RecipeConstants.ITEM, RecipeConstants.TAG, primitive -> {
            ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
            TagKey<Item> tag = getPreferredTagByItem(item);
            if (tag != null) {
                return new JsonPrimitive(tag.location().toString());
            }
            return null;
        });
    }

    @Override
    public JsonElement replaceResult(JsonElement element) {
        return depthReplace(element, RecipeConstants.ITEM, RecipeConstants.ITEM, primitive -> {
            ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
            ResourceLocation replacement = getReplacementForItem(item);
            if (replacement != null) {
                return new JsonPrimitive(replacement.toString());
            }
            return null;
        });
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean hasProperty(String property) {
        return currentRecipe.has(property);
    }
}
