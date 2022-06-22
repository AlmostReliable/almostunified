package com.almostreliable.unified.recipe.fallbacks;

import com.almostreliable.unified.api.recipe.ReplacementFallbackStrategy;
import com.almostreliable.unified.utils.TagMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Comparator;

// TODO use config and not this :D
public class StoneStrataFallbackStrategy implements ReplacementFallbackStrategy {
    @Override
    public ResourceLocation getFallback(TagKey<Item> tag, Collection<ResourceLocation> potentialItems, TagMap tags) {
        if (tag.location().getPath().contains("ores")) {
            return potentialItems
                    .stream()
                    .min(Comparator.comparingInt(s -> s.getPath().length()))
                    .orElse(null);
        }
        return null;
    }
}
