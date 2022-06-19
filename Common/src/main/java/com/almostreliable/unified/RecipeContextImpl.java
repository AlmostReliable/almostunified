package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecipeContextImpl implements RecipeContext {
    public static final String TAG = "tag";
    public static final String ITEM = "item";
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

    @Override
    public boolean replaceIngredient(JsonElement element) {
        AtomicBoolean changed = new AtomicBoolean(false);

        JsonUtils.arrayForEach(element, JsonObject.class, json -> {
            if (json.get(ITEM) instanceof JsonPrimitive primitive) {
                ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
                TagKey<Item> tag = getPreferredTagByItem(item);
                if (tag != null) {
                    json.remove(ITEM);
                    json.addProperty(TAG, tag.location().toString());
                    changed.set(true);
                }
            }
        });

        return changed.get();
    }

    @Override
    public boolean replaceResult(JsonElement element) {
        AtomicBoolean changed = new AtomicBoolean(false);

        JsonUtils.arrayForEach(element, JsonObject.class, json -> {
            if (json.get(ITEM) instanceof JsonPrimitive primitive) {
                ResourceLocation item = ResourceLocation.tryParse(primitive.getAsString());
                ResourceLocation replacement = getReplacementForItem(item);
                if (replacement != null) {
                    json.addProperty(ITEM, replacement.toString());
                    changed.set(true);
                }
            }
        });

        return changed.get();
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
