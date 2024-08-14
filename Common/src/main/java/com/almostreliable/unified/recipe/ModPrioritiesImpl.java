package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.UnifyEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ModPrioritiesImpl implements ModPriorities {

    private final List<String> modPriorities;
    private final Map<TagKey<Item>, String> priorityOverrides;

    public ModPrioritiesImpl(List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides) {
        this.modPriorities = modPriorities;
        this.priorityOverrides = priorityOverrides;
    }

    @Nullable
    @Override
    public String getPriorityOverride(TagKey<Item> tag) {
        return priorityOverrides.get(tag);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> findPriorityOverrideItem(TagKey<Item> tag, List<UnifyEntry<Item>> items) {
        String priorityOverride = getPriorityOverride(tag);
        if (priorityOverride == null) return null;

        var entry = findItemByNamespace(items, priorityOverride);
        if (entry != null) return entry;

        AlmostUnifiedCommon.LOGGER.warn(
                "Priority override mod '{}' for tag '{}' does not contain a valid item. Falling back to default priority.",
                priorityOverride,
                tag.location()
        );
        return null;
    }

    @Nullable
    @Override
    public UnifyEntry<Item> findTargetItem(TagKey<Item> tag, List<UnifyEntry<Item>> items) {
        var overrideEntry = findPriorityOverrideItem(tag, items);
        if (overrideEntry != null) {
            return overrideEntry;
        }

        for (String modPriority : modPriorities) {
            var entry = findItemByNamespace(items, modPriority);
            if (entry != null) return entry;
        }

        return null;
    }

    @Override
    public Iterator<String> iterator() {
        return modPriorities.iterator();
    }

    @Nullable
    private static UnifyEntry<Item> findItemByNamespace(List<UnifyEntry<Item>> items, String namespace) {
        for (var item : items) {
            if (item.id().getNamespace().equals(namespace)) {
                return item;
            }
        }

        return null;
    }
}
