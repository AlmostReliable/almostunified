package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Set;

public interface UnifySettings {

    ModPriorities getModPriorities();

    Collection<String> getStoneStrata();

    boolean shouldIncludeItem(ResourceLocation item);

    boolean shouldIncludeRecipe(ResourceLocation recipe);

    boolean shouldIncludeRecipeType(ResourceLocation type);

    Set<TagKey<Item>> getTags();

    void clearCache();
}
