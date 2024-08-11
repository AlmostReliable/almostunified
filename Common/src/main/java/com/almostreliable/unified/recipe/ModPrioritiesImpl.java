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
import java.util.function.BiConsumer;

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
    public UnifyEntry<Item> findPreferredEntry(TagKey<Item> tag, List<UnifyEntry<Item>> items) {
        var overrideEntry = getOverrideForTag(tag, items);
        if (overrideEntry != null) {
            return overrideEntry;
        }

        for (String modPriority : this) {
            var entry = findItemByNamespace(items, modPriority);
            if (entry != null) return entry;
        }

        return null;
    }

    @Nullable
    private UnifyEntry<Item> getOverrideForTag(TagKey<Item> tag, List<UnifyEntry<Item>> items) {
        String priorityOverride = getPriorityOverride(tag);
        if (priorityOverride != null) {
            var entry = findItemByNamespace(items, priorityOverride);
            if (entry != null) return entry;
            AlmostUnifiedCommon.LOGGER.warn(
                    "Priority override mod '{}' for tag '{}' does not contain a valid item. Falling back to default priority.",
                    priorityOverride,
                    tag.location());
        }

        return null;
    }

    @Nullable
    private UnifyEntry<Item> findItemByNamespace(List<UnifyEntry<Item>> items, String namespace) {
        for (var item : items) {
            if (item.id().getNamespace().equals(namespace)) {
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
        priorityOverrides.forEach(callback);
    }
}
