package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TagOwnerships {

    /**
     * A map of reference tags to their delegate tags.
     * <p>
     * Example:<br>
     * If the map contains the entry {@code minecraft:logs -> minecraft:planks},
     * any recipes where the tag {@code minecraft:logs} is being used, it will
     * replace the tag with {@code minecraft:planks}.
     * <p>
     * Map Key = Tag to replace<br>
     * Map Value = Tag to delegate to
     */
    private final Map<UnifyTag<Item>, UnifyTag<Item>> tagToOwnerTag;
    private final Multimap<UnifyTag<Item>, UnifyTag<Item>> ownerTagToTags;

    /**
     * Ensures that all tag delegates are also unify tags and that all delegate refs are no unify tags.
     */
    public TagOwnerships(Set<UnifyTag<Item>> usedTags, Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships) {
        ImmutableMap.Builder<UnifyTag<Item>, UnifyTag<Item>> tempTagToOwnerTag = ImmutableMap.builder();
        ImmutableMultimap.Builder<UnifyTag<Item>, UnifyTag<Item>> tempOwnerTagToTags = ImmutableMultimap.builder();

        tagOwnerships.forEach((rawOwnerTag, rawTags) -> {
            rawTags.forEach(rawTag -> {
                UnifyTag<Item> ownerTag = UnifyTag.item(rawOwnerTag);
                UnifyTag<Item> tag = UnifyTag.item(rawTag);

                if (!usedTags.contains(ownerTag)) {
                    AlmostUnified.LOG.warn("Tag delegate {} is not present in the unify tag list.",
                            ownerTag.location());
                    return;
                }

                if (usedTags.contains(tag)) {
                    AlmostUnified.LOG.warn(
                            "Tag {} is present in the unify tag list, but is also marked as ref for delegate {}.",
                            tag.location(),
                            ownerTag.location()
                    );
                    return;
                }

                tempTagToOwnerTag.put(tag, ownerTag);
                tempOwnerTagToTags.put(ownerTag, tag);
            });
        });

        this.tagToOwnerTag = tempTagToOwnerTag.build();
        this.ownerTagToTags = tempOwnerTagToTags.build();
    }

    /**
     * Gets holders of all refs of the provided delegate tag.
     * <p>
     * Ensures every ref is an actual tag.
     *
     * @param globalTags The global tag map.
     * @param ownerTag   The delegate tag to get all ref holders for.
     * @return A list of holders for all refs of the delegate tag.
     */
    public List<Holder<Item>> getHoldersForOwnerTag(Map<ResourceLocation, Collection<Holder<Item>>> globalTags, UnifyTag<Item> ownerTag) {
        var tags = ownerTagToTags.get(ownerTag);
        List<Holder<Item>> holders = new ArrayList<>();
        for (var tag : tags) {
            var tagHolders = globalTags.get(tag.location());
            if (tagHolders == null) {
                AlmostUnified.LOG.warn("Tag delegate ref '{}' for tag '{}' does not exist", tag, ownerTag);
                continue;
            }
            holders.addAll(tagHolders);
        }
        return holders;
    }

    /**
     * Gets the delegate tag for the provided ref tag.
     *
     * @param tag The ref tag to get the delegate for.
     * @return The delegate tag.
     */
    @Nullable
    public UnifyTag<Item> getOwnershipTag(UnifyTag<Item> tag) {
        return tagToOwnerTag.get(tag);
    }

    @Nullable
    public Set<TagKey<Item>> getSubTags(UnifyTag<Item> tag) {
        if (!ownerTagToTags.containsKey(tag)) {
            return null;
        }
        return ownerTagToTags
                .get(tag)
                .stream()
                .map(t -> TagKey.create(Registry.ITEM_REGISTRY, t.location()))
                .collect(Collectors.toSet());
    }
}
