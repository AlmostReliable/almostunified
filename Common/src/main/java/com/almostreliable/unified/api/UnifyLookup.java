package com.almostreliable.unified.api;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

public interface UnifyLookup {

    /**
     * Get all available tags, which are used for the unification process.
     *
     * @return all available tags, empty if no tags are available
     */
    Collection<TagKey<Item>> getUnifiedTags();

    /**
     * Returns all potential entries which are part of a given tag.
     * <p>
     * Tags are only considered if they are part of the config,
     * otherwise, an empty set is always returned.
     *
     * @param tag The tag to find the potential items for
     * @return The potential entries, otherwise an empty collection
     */
    Collection<UnifyEntry<Item>> getEntries(TagKey<Item> tag);

    @Nullable
    UnifyEntry<Item> getEntry(ResourceLocation entry);

    @Nullable
    UnifyEntry<Item> getEntry(Item item);

    /**
     * Returns the preferred tag for a given {@link Item} Will return null if no configured
     * tag exists that includes the item.
     *
     * @param item The item to find the preferred tag for
     * @return The preferred tag or null if there is no preferred tag
     */
    @Nullable
    TagKey<Item> getPreferredTagForItem(Item item);

    /**
     * @see #getPreferredTagForItem(Item)
     */
    @Nullable
    TagKey<Item> getPreferredTagForItem(ResourceLocation item);

    /**
     * @see #getPreferredTagForItem(Item)
     */
    @Nullable
    TagKey<Item> getPreferredTagForItem(Holder<Item> item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(ResourceLocation item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(Item item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(Holder<Item> item);

    @Nullable
    default UnifyEntry<Item> getReplacementForItem(UnifyEntry<Item> item) {
        return getReplacementForItem(item.asHolderOrThrow());
    }

    /**
     * Returns the preferred unify entry for a given {@link TagKey}. Will return null if no configured
     * tag exists that includes the item.
     * <p>
     * The preferred unify entry is selected according to mod priorities, but it's possible to set a
     * fixed override in the config.
     *
     * @param tag The tag to find the preferred entry for
     * @return The preferred entry or null if there is no preferred entry
     */
    @Nullable
    UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag);

    @Nullable
    UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter);

    /**
     * Gets all unify tags of the items within the given ingredient and checks
     * whether the given item is in one of those tags.
     *
     * @param ingredient The ingredient to get the unify tags from.
     * @param item       The item to check.
     * @return Whether the item is in one of the unify tags of the ingredient.
     */
    boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item);

    TagOwnerships getTagOwnerships();
}
