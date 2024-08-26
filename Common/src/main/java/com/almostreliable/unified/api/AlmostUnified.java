package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import com.almostreliable.unified.api.unification.Placeholders;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * The core interface for the Almost Unified API.
 * <p>
 * Use this to get an instance of the {@link AlmostUnifiedRuntime} or to look up unification information.
 *
 * @since 1.0.0
 */
public interface AlmostUnified {

    /**
     * The default instance of Almost Unified.
     * <p>
     * If unavailable, it will return an empty instance that only returns default values for each method.<br>
     * This instance is only available on the logical server side.
     */
    @SuppressWarnings("InnerClassReferencedViaSubclass")
    AlmostUnified INSTANCE = ServiceLoader.load(AlmostUnified.class).findFirst().orElseGet(Empty::new);

    /**
     * Returns whether the {@link AlmostUnifiedRuntime} is loaded and ready to be used.
     * <p>
     * The {@link AlmostUnifiedRuntime} is only available on the logical server side.
     *
     * @return true if the {@link AlmostUnifiedRuntime} is loaded, false otherwise
     */
    boolean isRuntimeLoaded();

    /**
     * Returns the instance of the {@link AlmostUnifiedRuntime}.
     * <p>
     * The {@link AlmostUnifiedRuntime} is only available on the logical server side. This method returns null if the
     * runtime is not loaded. To check this beforehand, use {@link #isRuntimeLoaded()}. If you are sure the runtime is
     * available, you can use {@link #getRuntimeOrThrow()} instead.
     *
     * @return the {@link AlmostUnifiedRuntime}, or null if the runtime is not loaded
     */
    @Nullable
    AlmostUnifiedRuntime getRuntime();

    /**
     * Returns the instance of the {@link AlmostUnifiedRuntime}.
     * <p>
     * The {@link AlmostUnifiedRuntime} is only available on the logical server side. This method throws an exception
     * if the runtime is not loaded. To check this beforehand, use {@link #isRuntimeLoaded()}.
     *
     * @return the {@link AlmostUnifiedRuntime}
     */
    AlmostUnifiedRuntime getRuntimeOrThrow();

    /**
     * Returns all {@link TagKey}s used for the unification process.
     * <p>
     * The returned collection will only contain tags that have their {@link Placeholders} replaced and have been
     * validated. All tags will be unique.
     *
     * @return the {@link TagKey}s used for the unification, or empty if no tags are used
     */
    Collection<TagKey<Item>> getTags();

    /**
     * Returns all item entries for the given {@link TagKey}.
     * <p>
     * The returned collection will only contain entries if the provided {@link TagKey} is a configured unification tag.
     *
     * @param tag the {@link TagKey} to get the entries for
     * @return the item entries for the {@link TagKey}, or empty if not found
     */
    Collection<Item> getTagEntries(TagKey<Item> tag);

    /**
     * Returns the relevant {@link TagKey} for the given {@link ItemLike}
     * <p>
     * Since an item can only have a single relevant tag, this method is guaranteed to return a single {@link TagKey} as
     * long as there exists a configured unification tag that includes the item.
     *
     * @param itemLike the {@link ItemLike} to get the relevant {@link TagKey} for
     * @return the relevant {@link TagKey}, or null if not found
     */
    @Nullable
    TagKey<Item> getRelevantItemTag(ItemLike itemLike);

    /**
     * Returns the target item for the given variant {@link ItemLike}.
     * <p>
     * The target item describes the item with the highest priority among all variant items within a {@link TagKey}.
     * It is used to replace the variant items in the unification process.<br>
     * This method will return null if no configured unification tag exists that includes the given item.
     * <p>
     * If the item is part of a stone variant, it will only check items within the same stone variant.
     *
     * @param itemLike the variant {@link ItemLike} to get the target item for
     * @return the target item, or null if not found
     */
    @Nullable
    Item getVariantItemTarget(ItemLike itemLike);

    /**
     * Returns the target item for the given {@link TagKey}.
     * <p>
     * The target item describes the item with the highest priority among all variant items within a {@link TagKey}.
     * It is used to replace the variant items in the unification process.<br>
     * This method will return null the given {@link TagKey} is not a configured unification tag.
     *
     * @param tag the {@link TagKey} to get the target item for
     * @return the target item, or null if not found
     */
    @Nullable
    Item getTagTargetItem(TagKey<Item> tag);

    class Empty implements AlmostUnified {

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

        @Override
        public Collection<TagKey<Item>> getTags() {
            return Set.of();
        }

        @Override
        public Collection<Item> getTagEntries(TagKey<Item> tag) {
            return Set.of();
        }

        @Nullable
        @Override
        public TagKey<Item> getRelevantItemTag(ItemLike itemLike) {
            return null;
        }

        @Nullable
        @Override
        public Item getVariantItemTarget(ItemLike itemLike) {
            return null;
        }

        @Nullable
        @Override
        public Item getTagTargetItem(TagKey<Item> tag) {
            return null;
        }
    }
}
