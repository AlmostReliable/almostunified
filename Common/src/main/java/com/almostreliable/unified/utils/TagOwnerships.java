package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.BuildConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

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

    public static void updateRawTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> rawTags, TagOwnerships ownerships) {
        ownerships.getOwnerTagToTags().asMap().forEach((owner, refs) -> {
            ResourceLocation ownerLocation = owner.location();
            var entries = rawTags.get(ownerLocation);
            if (entries == null) {
                AlmostUnified.LOG.warn("Tag {} is not present in the tag list.", ownerLocation);
                return;
            }

            for (UnifyTag<Item> ref : refs) {
                ResourceLocation refLocation = ref.location();
                var refEntries = rawTags.get(refLocation);
                if (refEntries == null) {
                    AlmostUnified.LOG.warn("Tag {} is not present in the tag list.", refLocation);
                    continue;
                }

                TagEntry entry = TagEntry.tag(refLocation);
                var ews = new TagLoader.EntryWithSource(entry, BuildConfig.MOD_ID);
                entries.add(ews);
            }
        });
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

    public Multimap<UnifyTag<Item>, UnifyTag<Item>> getOwnerTagToTags() {
        return Multimaps.unmodifiableMultimap(ownerTagToTags);
    }
}
