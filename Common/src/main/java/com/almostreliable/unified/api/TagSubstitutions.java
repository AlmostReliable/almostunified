package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Helper for tracking tag substitutions.
 * <p>
 * The tag substitutions system allows to convert tags (reference tags) to other tags (substitute tags).<br>
 * The system copies all entries of the reference tags to their respective substitute tags. After that, it replaces
 * all occurrences of the reference tags with their substitute tags in all recipes.
 * <p>
 * Example:<br>
 * If we define the following entry {@code minecraft:logs -> minecraft:planks}, any recipes making use of the tag
 * {@code minecraft:logs} will use the {@code minecraft:planks} tag instead.
 * <p>
 * This can be useful when mods use different tag conventions like {@code c:ingots/iron} and {@code c:iron_ingots}.
 */
public interface TagSubstitutions {

    /**
     * Returns the substitute tag for the provided reference tag.
     *
     * @param referenceTag the reference tag to get the substitute for
     * @return the substitute tag or null if the provided tag is not a valid configured reference tag
     */
    @Nullable
    TagKey<Item> getSubstituteTag(TagKey<Item> referenceTag);

    /**
     * Returns all reference tags for the provided substitute tag.
     *
     * @param substituteTag the substitute tag to get the reference tags for
     * @return a collection of all reference tags for the provided substitute tag or an empty collection if the
     * provided tag is not a valid configured substitute tag
     */
    Collection<TagKey<Item>> getReferenceTags(TagKey<Item> substituteTag);

    /**
     * Returns all valid configured reference tags for all substitute tags.
     *
     * @return a collection of all valid configured reference tags
     */
    Set<TagKey<Item>> getReferenceTags();
}
