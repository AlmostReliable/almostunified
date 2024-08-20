package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Helper for handling mod priorities.
 * <p>
 * Mod priorities are used to choose the target items in the unification process.<br>
 * If a tag contains multiple items from different mods, the priority defines which item is chosen first. Priority
 * is sorted from highest to lowest. All unlisted mods have less priority than all listed mods.
 * <p>
 * Priority overrides allow overriding the priority mod for specific tags.<br>
 * When a priority override is specified for a tag, the mod priorities will be ignored.
 */
public interface ModPriorities extends Iterable<String> {

    /**
     * Returns the priority override of the given tag.
     * <p>
     * This method returns the mod id if a priority override is configured for the given tag. If you want to resolve
     * the tag to an item, use {@link #findPriorityOverrideItem(TagKey, List)} or
     * {@link #findTargetItem(TagKey, List)} instead.
     *
     * @param tag the tag to get the priority override for
     * @return the priority override, or null if no override exists
     */
    @Nullable
    String getPriorityOverride(TagKey<Item> tag);

    /**
     * Returns the priority override item of the given tag contained in the list of potential items.
     * <p>
     * This method returns the item if a priority override is configured for the given tag. If you want to resolve the
     * tag to an item by also using the mod priorities, use {@link #findTargetItem(TagKey, List)} instead.
     *
     * @param tag   the tag to get the priority override item for
     * @param items the list of potential items, sorted from shortest to longest id
     * @return the priority override item, or null if no override exists
     */
    @Nullable
    UnificationEntry<Item> findPriorityOverrideItem(TagKey<Item> tag, List<UnificationEntry<Item>> items);

    /**
     * Returns the target item of the given tag contained in the list of potential items.
     * <p>
     * The item is chosen according to the priority overrides first if available. If no priority override is configured,
     * the item is chosen according to the mod priorities.
     * <p>
     * This method can return null if no override exists, and the potential items only include items with namespaces
     * that are not part of the mod priorities.
     *
     * @param tag   the tag to get the target item for
     * @param items the list of potential items, sorted from shortest to longest id
     * @return the target item of the given tag, or null if no target item could be found
     */
    @Nullable
    UnificationEntry<Item> findTargetItem(TagKey<Item> tag, List<UnificationEntry<Item>> items);

    default Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
