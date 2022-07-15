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
    ResourceLocation getPreferredItemByTag(@Nullable UnifyTag<Item> tag, Predicate<ResourceLocation> filter);

    @Nullable
    UnifyTag<Item> getPreferredTagByItem(@Nullable ResourceLocation item);

    JsonElement createIngredientReplacement(@Nullable JsonElement element);

    @Nullable
    JsonElement createResultReplacement(@Nullable JsonElement element);

    ResourceLocation getType();

    boolean hasProperty(String property);

    default String getModId() {
        return getType().getNamespace();
    }
}
