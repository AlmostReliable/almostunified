package com.almostreliable.unified.unification;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.unification.ModPriorities;
import com.almostreliable.unified.api.unification.UnificationEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

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
    public UnificationEntry<Item> findPriorityOverrideItem(TagKey<Item> tag, List<UnificationEntry<Item>> items) {
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
    public UnificationEntry<Item> findTargetItem(TagKey<Item> tag, List<UnificationEntry<Item>> items) {
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
    private static UnificationEntry<Item> findItemByNamespace(List<UnificationEntry<Item>> items, String namespace) {
        for (var item : items) {
            if (item.id().getNamespace().equals(namespace)) {
                return item;
            }
        }

        return null;
    }
}
