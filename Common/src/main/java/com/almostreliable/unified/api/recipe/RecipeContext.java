package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface RecipeContext {

    @Nullable
    ResourceLocation getReplacementForItem(@Nullable ResourceLocation item);

    @Nullable
    ResourceLocation getPreferredItemForTag(@Nullable UnifyTag<Item> tag, Predicate<ResourceLocation> filter);

    @Nullable
    UnifyTag<Item> getPreferredTagForItem(@Nullable ResourceLocation item);

    @Nullable
    UnifyTag<Item> getDelegateForRef(@Nullable ResourceLocation ref);

    @Nullable
    JsonElement createIngredientReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element, boolean includeTagCheck, String... lookupKeys);

    ResourceLocation getType();

    boolean hasProperty(String property);

    default String getModId() {
        return getType().getNamespace();
    }
}
