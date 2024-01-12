package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.utils.json.JsonCursor;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface RecipeContext {

    @Nullable
    ResourceLocation getReplacementForItem(@Nullable ResourceLocation item);

    @Nullable
    ResourceLocation getPreferredItemForTag(@Nullable TagKey<Item> tag, Predicate<ResourceLocation> filter);

    @Nullable
    TagKey<Item> getPreferredTagForItem(@Nullable ResourceLocation item);

    @Nullable
    JsonElement createIngredientReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element, boolean includeTagCheck, String... lookupKeys);

    void replaceBasicInput(JsonCursor cursor);

    void replaceBasicOutput(JsonCursor cursor);

    void replaceBasicOutput(JsonCursor cursor, boolean replaceTag, String... keyLookups);
}
