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

public interface UnificationHandler {

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
    Collection<UnificationEntry<Item>> getEntries(TagKey<Item> tag);

    @Nullable
    UnificationEntry<Item> getEntry(ResourceLocation entry);

    @Nullable
    UnificationEntry<Item> getEntry(Item item);

    /**
     * Returns the relevant tag for a given {@link Item} Will return null if no configured
     * tag exists that includes the item.
     *
     * @param item The item to find the relevant tag for
     * @return The relevant tag or null if there is no relevant tag
     */
    @Nullable
    TagKey<Item> getRelevantItemTag(Item item);

    /**
     * @see #getRelevantItemTag(Item)
     */
    @Nullable
    TagKey<Item> getRelevantItemTag(ResourceLocation item);

    /**
     * @see #getRelevantItemTag(Item)
     */
    @Nullable
    TagKey<Item> getRelevantItemTag(Holder<Item> item);

    @Nullable
    UnificationEntry<Item> getItemReplacement(ResourceLocation item);

    @Nullable
    UnificationEntry<Item> getItemReplacement(Item item);

    @Nullable
    UnificationEntry<Item> getItemReplacement(Holder<Item> item);

    @Nullable
    default UnificationEntry<Item> getItemReplacement(UnificationEntry<Item> item) {
        return getItemReplacement(item.asHolderOrThrow());
    }

    /**
     * Returns the target unify entry for a given {@link TagKey}. Will return null if no configured
     * tag exists that includes the item.
     * <p>
     * The target unify entry is selected according to mod priorities, but it's possible to set a
     * fixed override in the config.
     *
     * @param tag The tag to find the target entry for
     * @return The target entry or null if there is no target entry
     */
    @Nullable
    UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag);

    @Nullable
    UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter);

    /**
     * Gets all unify tags of the items within the given ingredient and checks
     * whether the given item is in one of those tags.
     *
     * @param ingredient The ingredient to get the unify tags from.
     * @param item       The item to check.
     * @return Whether the item is in one of the unify tags of the ingredient.
     */
    boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item);

    TagSubstitutions getTagSubstitutions();
}
