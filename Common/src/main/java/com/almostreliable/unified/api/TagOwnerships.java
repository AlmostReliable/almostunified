package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * An interface for holding ownerships for tags.
 * <p>
 * Tags that group other under them are called owner tags while the tags that are grouped are called reference tags.
 * An owner tag can host multiple reference tags.
 * <p>
 * Ownerships are used to define tags that will be converted into their reference tags in the unify process.
 * <p>
 * Example:<br>
 * If we define following entry {@code minecraft:logs -> minecraft:planks}, later on any recipes where the tag {@code minecraft:logs} is being used, it will
 * replace the tag with {@code minecraft:planks}.
 */
public interface TagOwnerships {

    /**
     * Gets the owner tag for the provided reference tag.
     *
     * @param referenceTag The reference tag to get the owner for.
     * @return The owner tag, or null if the provided tag is not a reference tag.
     */
    @Nullable
    TagKey<Item> getOwner(TagKey<Item> referenceTag);

    /**
     * Gets all reference tags for the provided owner tag.
     *
     * @param ownerTag The owner tag to get the reference tags for.
     * @return A collection of all reference tags for the provided owner tag. If not found, an empty collection is returned.
     */
    Collection<TagKey<Item>> getRefs(TagKey<Item> ownerTag);

    /**
     * Gets all reference tags for all owner tags.
     *
     * @return A set of all reference tags.
     */
    Set<TagKey<Item>> getRefs();
}
