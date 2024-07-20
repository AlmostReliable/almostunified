package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * The core interface for the Almost Unified api.
 * <p>
 * Use this to obtain an instance of the runtime or to lookup
 * replacement items.
 */
public interface AlmostUnifiedLookup {

    /**
     * The default instance of the Almost Unified lookup.
     * <p>
     * If unavailable, it will return an empty lookup instance which
     * only returns empty default values for each method.
     */
    AlmostUnifiedLookup INSTANCE = ServiceLoader.load(AlmostUnifiedLookup.class).findFirst().orElseGet(Empty::new);

    /**
     * Returns whether the Almost Unified runtime is loaded
     * and ready to be used.
     *
     * @return True if the runtime is loaded, false otherwise.
     */
    boolean isRuntimeLoaded();

    /**
     * Returns the instance of the Almost Unified runtime or null
     * if the runtime is not loaded.
     * <p>
     * This can be used if you are not sure whether the runtime is loaded.
     * <p>
     * If you are sure whether the runtime is loaded, use {@link #getRuntimeOrThrow()}.
     *
     * @return The Almost Unified runtime or null if the runtime is not loaded.
     */
    @Nullable
    AlmostUnifiedRuntime getRuntime();

    /**
     * Returns the instance of the Almost Unified runtime or throws
     * an exception if the runtime is not loaded.
     * <p>
     * This can be used if you are sure whether the runtime is loaded.
     * <p>
     * If you are not sure whether the runtime is loaded, use {@link #getRuntime()}.
     *
     * @return The Almost Unified runtime.
     */
    AlmostUnifiedRuntime getRuntimeOrThrow();

    /**
     * Returns the replacement item for a given {@link ItemLike}. Will return null if no configured
     * tag exists that includes the item.
     * <p>
     * If the item is part of some stone strata, it will only check items within the same stone strata.<br>
     * => e.g. "modid:deepslate_foo_ore" would not return "prio_modid:foo_ore".
     *
     * @param itemLike The item-like to find the replacement for
     * @return The replacement item or null if there is no replacement
     */
    @Nullable
    Item getReplacementForItem(ItemLike itemLike);

    /**
     * Returns the preferred item for a given {@link TagKey}. Will return null if no configured
     * tag exists that includes the item.
     * <p>
     * The preferred item is selected according to mod priorities, but it's possible to set a
     * fixed override in the config.
     *
     * @param tag The tag to find the preferred item for
     * @return The preferred item or null if there is no preferred item
     */
    @Nullable
    Item getPreferredItemForTag(TagKey<Item> tag);

    /**
     * Returns the preferred tag for a given {@link ItemLike} Will return null if no configured
     * tag exists that includes the item.
     *
     * @param itemLike The item-like to find the preferred tag for
     * @return The preferred tag or null if there is no preferred tag
     */
    @Nullable
    TagKey<Item> getPreferredTagForItem(ItemLike itemLike);

    /**
     * Returns all potential items which are part of a given tag.
     * <p>
     * Tags are only considered if they are part of the config,
     * otherwise, an empty set is always returned.
     *
     * @param tag The tag to find the potential items for
     * @return The potential items or an empty set if there are no potential items
     */
    Set<Item> getPotentialItems(TagKey<Item> tag);

    /**
     * Returns all configured tags.
     *
     * @return The configured tags
     */
    Collection<TagKey<Item>> getUnifiedTags();

    class Empty implements AlmostUnifiedLookup {

        @Override
        public boolean isRuntimeLoaded() {
            return false;
        }

        @Nullable
        @Override
        public AlmostUnifiedRuntime getRuntime() {
            return null;
        }

        @Override
        public AlmostUnifiedRuntime getRuntimeOrThrow() {
            throw new IllegalStateException("runtime is not loaded");
        }

        @Nullable
        @Override
        public Item getReplacementForItem(ItemLike itemLike) {
            return null;
        }

        @Nullable
        @Override
        public Item getPreferredItemForTag(TagKey<Item> tag) {
            return null;
        }

        @Nullable
        @Override
        public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
            return null;
        }

        @Override
        public Set<Item> getPotentialItems(TagKey<Item> tag) {
            return Set.of();
        }

        @Override
        public Collection<TagKey<Item>> getUnifiedTags() {
            return Set.of();
        }
    }
}
