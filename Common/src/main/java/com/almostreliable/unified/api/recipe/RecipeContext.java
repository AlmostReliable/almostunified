package com.almostreliable.unified.api.recipe;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public interface RecipeContext {

    @Nullable
    ResourceLocation getReplacementForItem(@Nullable ResourceLocation item);

    @Nullable
    ResourceLocation getPreferredItemByTag(TagKey<Item> tag);

    @Nullable
    TagKey<Item> getPreferredTagByItem(@Nullable ResourceLocation item);

    JsonElement replaceIngredient(JsonElement element);

    JsonElement replaceResult(JsonElement element);

    ResourceLocation getType();

    ResourceLocation getId();

    boolean hasProperty(String property);

    default String getModId() {
        return getType().getNamespace();
    }
}
