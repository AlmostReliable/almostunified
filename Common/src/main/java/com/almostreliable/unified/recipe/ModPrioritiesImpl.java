package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.ModPriorities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public record ModPrioritiesImpl(List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides)
        implements ModPriorities {

    @Nullable
    @Override
    public String getPriorityOverride(TagKey<Item> tag) {
        return priorityOverrides.get(tag);
    }

    @Nullable
    @Override
    public ResourceLocation findPreferredItemId(TagKey<Item> tag, List<ResourceLocation> items) {
        ResourceLocation overrideItem = getOverrideForTag(tag, items);
        if (overrideItem != null) {
            return overrideItem;
        }

        for (String modPriority : this) {
            ResourceLocation item = findItemByNamespace(items, modPriority);
            if (item != null) return item;
        }

        return null;
    }

    @Nullable
    private ResourceLocation getOverrideForTag(TagKey<Item> tag, List<ResourceLocation> items) {
        String priorityOverride = getPriorityOverride(tag);
        if (priorityOverride != null) {
            ResourceLocation item = findItemByNamespace(items, priorityOverride);
            if (item != null) return item;
            AlmostUnified.LOG.warn(
                    "Priority override mod '{}' for tag '{}' does not contain a valid item. Falling back to default priority.",
                    priorityOverride,
                    tag.location());
        }
        return null;
    }

    @Nullable
    private ResourceLocation findItemByNamespace(List<ResourceLocation> items, String namespace) {
        for (ResourceLocation item : items) {
            if (item.getNamespace().equals(namespace)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> iterator() {
        return modPriorities.iterator();
    }

    @Override
    public void forEachOverride(BiConsumer<TagKey<Item>, String> callback) {
        priorityOverrides().forEach(callback);
    }
}
