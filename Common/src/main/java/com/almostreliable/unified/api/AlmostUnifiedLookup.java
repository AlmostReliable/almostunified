package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ServiceLoader;
import java.util.Set;

public interface AlmostUnifiedLookup {

    AlmostUnifiedLookup INSTANCE = ServiceLoader.load(AlmostUnifiedLookup.class).findFirst().orElseGet(Empty::new);

    boolean isLoaded();

    /**
     * Returns replacement item for given {@link ItemLike}. If no configured tag exists which includes the item it will return null. <p>
     * If the item is part of some stone strata, it will only check items within the same stone strata. <br>
     * => e.g. "modid:deepslate_foo_ore" would not return "prio_modid:foo_ore".
     *
     * @param itemLike The item like to find the replacement for
     * @return replacement item or null if there is no replacement
     */
    @Nullable
    Item getReplacementForItem(ItemLike itemLike);

    /**
     * Returns the preferred item for given {@link TagKey}. If no configured tag exists which includes the item it will return null. <p>
     * The preferred item is mainly chose by mod priorities, but it's possible to provide a fixed override through the config.
     *
     * @param tag The tag to find the preferred item for
     * @return preferred item or null if there is no preferred item
     */
    @Nullable
    Item getPreferredItemForTag(TagKey<Item> tag);

    /**
     * Returns the preferred tag for given {@link ItemLike}. If no configured tag exists which includes the item it will return null. <p>
     *
     * @param itemLike The item like to find the preferred tag for
     * @return preferred tag or null if there is no preferred tag
     */
    @Nullable
    TagKey<Item> getPreferredTagForItem(ItemLike itemLike);

    /**
     * Returns all potential items which are part of the given tag. Tags are only considered if they are part of the config, otherwise they will always return an empty set.
     *
     * @param tag The tag to find the potential items for
     * @return potential items or empty set if there are no potential items
     */
    Set<Item> getPotentialItems(TagKey<Item> tag);

    /**
     * Returns all configured tags.
     *
     * @return configured tags
     */
    Set<TagKey<Item>> getConfiguredTags();

    class Empty implements AlmostUnifiedLookup {

        @Override
        public boolean isLoaded() {
            return false;
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
        public Set<TagKey<Item>> getConfiguredTags() {
            return Set.of();
        }
    }
}
