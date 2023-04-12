package com.almostreliable.unified.api;

import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.resources.ResourceLocation;
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
     * Returns the parent tag for a given delegate tag. Will return null if none is configured or
     * not found.
     *
     * @param delegate The delegate tag to find the parent tag for
     * @return The parent tag or null if there is no parent tag
     */
    @Nullable
    UnifyTag<Item> getParentTagForDelegate(ResourceLocation delegate);

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

        @Nullable
        @Override
        public UnifyTag<Item> getParentTagForDelegate(ResourceLocation delegate) {
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
