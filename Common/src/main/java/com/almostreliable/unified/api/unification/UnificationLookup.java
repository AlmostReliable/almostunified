package com.almostreliable.unified.api.unification;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Interface exposing composable unification information for a single unification config.
 * <p>
 * There exists one instance for each config.<br>
 * A unification lookup only exposes composable information. The composition of all lookups is used to check global
 * unification information. For example, when retrieving the replacement of a specific item. The composition can't
 * expose internal information such as mod priorities.
 * <p>
 * If a lookup with exposed configuration is required, use {@link UnificationSettings} instead.
 */
public interface UnificationLookup {

    /**
     * Returns all {@link TagKey}s used for the unification process of the config this lookup is for.
     * <p>
     * The returned collection will only contain tags that have their {@link Placeholders} replaced and have been
     * validated. All tags will be unique.
     *
     * @return the {@link TagKey}s used for the unification, or empty if no tags are used
     */
    Collection<TagKey<Item>> getTags();

    /**
     * Returns all {@link UnificationEntry}s for a given {@link TagKey}.
     * <p>
     * The returned collection will only contain entries if the provided {@link TagKey} is part of the config this
     * lookup is for.
     *
     * @param tag the {@link TagKey} to get the entries for
     * @return the {@link UnificationEntry}s for the tag, or empty if no {@link UnificationEntry}s are found
     */
    Collection<UnificationEntry<Item>> getTagEntries(TagKey<Item> tag);

    /**
     * Returns the {@link UnificationEntry} for the given item id.
     * <p>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned.
     *
     * @param item the item id to get the {@link UnificationEntry} for
     * @return the {@link UnificationEntry} for the item id, or null if no {@link UnificationEntry} is found
     */
    @Nullable
    UnificationEntry<Item> getItemEntry(ResourceLocation item);

    /**
     * Returns the {@link UnificationEntry} for the given {@link Item}.
     * <p>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned.
     *
     * @param item the {@link Item} to get the {@link UnificationEntry} for
     * @return the {@link UnificationEntry} for the {@link Item}, or null if no {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getItemEntry(Item item) {
        return getItemEntry(BuiltInRegistries.ITEM.getKey(item));
    }

    /**
     * Returns the {@link UnificationEntry} for the given item {@link Holder}.
     * <p>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned.
     *
     * @param item the item {@link Holder} to get the {@link UnificationEntry} for
     * @return the {@link UnificationEntry} for the item {@link Holder}, or null if no {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getItemEntry(Holder<Item> item) {
        return getItemEntry(item.value());
    }

    /**
     * Returns the relevant {@link TagKey} for the given item id.
     * <p>
     * Since an item can only have a single relevant tag, this method is guaranteed to return a single {@link TagKey} as
     * long as the config this lookup is for covers the {@link Item}.
     *
     * @param item the item id to get the relevant {@link TagKey} for
     * @return the relevant {@link TagKey} for the item id, or null if no relevant {@link TagKey} is found
     */
    @Nullable
    TagKey<Item> getRelevantItemTag(ResourceLocation item);

    /**
     * Returns the relevant {@link TagKey} for the given {@link Item}.
     * <p>
     * Since an item can only have a single relevant tag, this method is guaranteed to return a single {@link TagKey} as
     * long as the config this lookup is for covers the {@link Item}.
     *
     * @param item the {@link Item} to get the relevant {@link TagKey} for
     * @return the relevant {@link TagKey} for the {@link Item}, or null if no relevant {@link TagKey} is found
     */
    @Nullable
    default TagKey<Item> getRelevantItemTag(Item item) {
        return getRelevantItemTag(BuiltInRegistries.ITEM.getKey(item));
    }

    /**
     * Returns the relevant {@link TagKey} for the given item {@link Holder}.
     * <p>
     * Since an item can only have a single relevant tag, this method is guaranteed to return a single {@link TagKey} as
     * long as the config this lookup is for covers the {@link Item}.
     *
     * @param item the item {@link Holder} to get the relevant {@link TagKey} for
     * @return the relevant {@link TagKey} for the item {@link Holder}, or null if no relevant {@link TagKey} is found
     */
    @Nullable
    default TagKey<Item> getRelevantItemTag(Holder<Item> item) {
        return getRelevantItemTag(item.value());
    }

    /**
     * Returns the replacement {@link UnificationEntry} for the given item id.
     * <p>
     * The replacement of an {@link Item} is the {@link Item} that is used instead of the provided item in the
     * unification process.<br>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned. This method will return the
     * {@link Item} itself if the provided {@link Item} is the target {@link Item} already.
     *
     * @param item the item id to get the replacement {@link UnificationEntry} for
     * @return the replacement {@link UnificationEntry} for the item id, or null if no replacement
     * {@link UnificationEntry} is found
     */
    @Nullable
    UnificationEntry<Item> getItemReplacement(ResourceLocation item);

    /**
     * Returns the replacement {@link UnificationEntry} for the given {@link Item}.
     * <p>
     * The replacement of an {@link Item} is the {@link Item} that is used instead of the provided item in the
     * unification process.<br>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned. This method will return the
     * {@link Item} itself if the provided {@link Item} is the target {@link Item} already.
     *
     * @param item the {@link Item} to get the replacement {@link UnificationEntry} for
     * @return the replacement {@link UnificationEntry} for the {@link Item}, or null if no replacement
     * {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getItemReplacement(Item item) {
        return getItemReplacement(BuiltInRegistries.ITEM.getKey(item));
    }

    /**
     * Returns the replacement {@link UnificationEntry} for the given item {@link Holder}.
     * <p>
     * The replacement of an {@link Item} is the {@link Item} that is used instead of the provided item in the
     * unification process.<br>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned. This method will return the
     * {@link Item} itself if the provided {@link Item} is the target {@link Item} already.
     *
     * @param item the item {@link Holder} to get the replacement {@link UnificationEntry} for
     * @return the replacement {@link UnificationEntry} for the item {@link Holder}, or null if no replacement
     * {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getItemReplacement(Holder<Item> item) {
        return getItemReplacement(item.value());
    }

    /**
     * Returns the replacement {@link UnificationEntry} for the given {@link UnificationEntry}.
     * <p>
     * The replacement of an {@link Item} is the {@link Item} that is used instead of the provided item in the
     * unification process.<br>
     * If the config this lookup is for doesn't cover the {@link Item}, null is returned. This method will return the
     * {@link Item} itself if the provided {@link Item} is the target {@link Item} already.
     *
     * @param item the {@link UnificationEntry} to get the replacement {@link UnificationEntry} for
     * @return the replacement {@link UnificationEntry} for the {@link UnificationEntry}, or null if no replacement
     * {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getItemReplacement(UnificationEntry<Item> item) {
        return getItemReplacement(item.asHolderOrThrow());
    }

    /**
     * Returns the target {@link UnificationEntry} for the given {@link TagKey} that matches the given filter.
     * <p>
     * The target {@link Item} of a {@link TagKey} is the {@link Item} that has the highest priority within the
     * {@link TagKey}.<br>
     * If the config this lookup is for doesn't cover the {@link TagKey}, null is returned.
     *
     * @param tag the {@link TagKey} to get the target {@link UnificationEntry} for
     * @return the target {@link UnificationEntry} for the {@link TagKey}, or null if no target
     * {@link UnificationEntry} is found
     */
    @Nullable
    UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter);

    /**
     * Returns the target {@link UnificationEntry} for the given {@link TagKey}.
     * <p>
     * The target {@link Item} of a {@link TagKey} is the {@link Item} that has the highest priority within the
     * {@link TagKey}.<br>
     * If the config this lookup is for doesn't cover the {@link TagKey}, null is returned.
     *
     * @param tag the {@link TagKey} to get the target {@link UnificationEntry} for
     * @return the target {@link UnificationEntry} for the {@link TagKey}, or null if no target
     * {@link UnificationEntry} is found
     */
    @Nullable
    default UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag) {
        return getTagTargetItem(tag, $ -> true);
    }

    /**
     * Returns whether the given {@link ItemStack} is part of any tags the given {@link Ingredient} points to.
     * <p>
     * To check this, this method fetches all unification tags of the {@link Item}s within the given {@link Ingredient}
     * and checks whether the given {@link ItemStack} is part of them.
     *
     * @param ingredient the {@link Ingredient} to check to get the relevant tags for
     * @param item       the {@link ItemStack} to check
     * @return whether the given {@link ItemStack} is part of any tags the given {@link Ingredient} points to
     */
    boolean isUnifiedIngredientItem(Ingredient ingredient, ItemStack item);
}
