package com.almostreliable.unified.api.unification;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Helper for tracking tag substitutions.
 * <p>
 * The tag substitutions system allows converting tags (replaced tags) to other tags (substitute tags).<br>
 * The system copies all entries of the replaced tags to their respective substitute tags. After that, it replaces
 * all occurrences of the replaced tags with their substitute tags in all recipes.
 * <p>
 * Example:<br>
 * If we define the following entry {@code minecraft:logs -> minecraft:planks}, any recipes making use of the tag
 * {@code minecraft:logs} will use the {@code minecraft:planks} tag instead.
 * <p>
 * This can be useful when mods use different tag conventions like {@code c:ingots/iron} and {@code c:iron_ingots}.
 *
 * @since 1.0.0
 */
public interface TagSubstitutions {

    /**
     * Returns the substitute tag for the provided replaced tag.
     *
     * @param replacedTag the replaced tag to get the substitute for
     * @return the substitute tag or null if the provided tag is not a valid configured replaced tag
     */
    @Nullable
    TagKey<Item> getSubstituteTag(TagKey<Item> replacedTag);

    /**
     * Returns all replaced tags for the provided substitute tag.
     *
     * @param substituteTag the substitute tag to get the replaced tags for
     * @return a collection of all replaced tags for the provided substitute tag or an empty collection if the
     * provided tag is not a valid configured substitute tag
     */
    Collection<TagKey<Item>> getReplacedTags(TagKey<Item> substituteTag);

    /**
     * Returns all valid configured replaced tags for all substitute tags.
     *
     * @return a collection of all valid configured replaced tags
     */
    Set<TagKey<Item>> getReplacedTags();
}
