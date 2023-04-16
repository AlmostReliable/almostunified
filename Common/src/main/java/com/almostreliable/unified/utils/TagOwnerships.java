package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.BuildConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagOwnerships {

    /**
     * A map holding relationships between reference tags and their owner tags.
     * <p>
     * Example:<br>
     * If the map contains the entry {@code minecraft:logs -> minecraft:planks},
     * any recipes where the tag {@code minecraft:logs} is being used, it will
     * replace the tag with {@code minecraft:planks}.
     * <p>
     * Map Key = Tag to replace<br>
     * Map Value = Tag to replace with
     */
    private final Map<UnifyTag<Item>, UnifyTag<Item>> refsToOwner;
    private final Multimap<UnifyTag<Item>, UnifyTag<Item>> ownerToRefs;

    /**
     * Creates a new TagOwnerships instance that contains immutable maps of all tag ownership relationships.
     * <p>
     * It is ensured that all owner tags are present in the {@code unifyTags} set, and that all reference tags
     * are not present in the {@code unifyTags} set.
     *
     * @param unifyTags          The set of all unify tags from the config.
     * @param tagOwnershipConfig The map of all tag ownership relationships from the config.
     */
    public TagOwnerships(Set<UnifyTag<Item>> unifyTags, Map<ResourceLocation, Set<ResourceLocation>> tagOwnershipConfig) {
        ImmutableMap.Builder<UnifyTag<Item>, UnifyTag<Item>> refsToOwnerBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<UnifyTag<Item>, UnifyTag<Item>> ownerToRefsBuilder = ImmutableMultimap.builder();

        tagOwnershipConfig.forEach((rawOwner, rawRefs) -> {
            for (ResourceLocation rawRef : rawRefs) {
                UnifyTag<Item> ownerTag = UnifyTag.item(rawOwner);
                UnifyTag<Item> ref = UnifyTag.item(rawRef);

                if (!unifyTags.contains(ownerTag)) {
                    AlmostUnified.LOG.warn(
                            "Ownership tag {} is not present in the unify tag list.",
                            ownerTag.location()
                    );
                    continue;
                }

                if (unifyTags.contains(ref)) {
                    AlmostUnified.LOG.warn(
                            "Tag {} is present in the unify tag list, but is also marked as ownership reference for the owner tag {}.",
                            ref.location(),
                            ownerTag.location()
                    );
                    continue;
                }

                refsToOwnerBuilder.put(ref, ownerTag);
                ownerToRefsBuilder.put(ownerTag, ref);
            }
        });

        this.refsToOwner = refsToOwnerBuilder.build();
        this.ownerToRefs = ownerToRefsBuilder.build();
    }

    public void updateRawTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> rawTags) {
        ownerToRefs.asMap().forEach((owner, refs) -> {
            ResourceLocation ownerLocation = owner.location();
            var entries = rawTags.get(ownerLocation);
            if (entries == null) {
                AlmostUnified.LOG.warn("Ownership tag {} does not exist.", ownerLocation);
                return;
            }

            for (UnifyTag<Item> ref : refs) {
                ResourceLocation refLocation = ref.location();
                var refEntries = rawTags.get(refLocation);
                if (refEntries == null) {
                    AlmostUnified.LOG.warn(
                            "Reference tag {} for ownership tag {} does not exist.",
                            refLocation,
                            ownerLocation
                    );
                    continue;
                }

                TagEntry entry = TagEntry.tag(refLocation);
                var ews = new TagLoader.EntryWithSource(entry, BuildConfig.MOD_ID);
                entries.add(ews);
            }
        });
    }

    /**
     * Gets the owner tag for the provided reference tag.
     *
     * @param tag The reference tag to get the owner for.
     * @return The owner tag, or null if the provided tag is not a reference tag.
     */
    @Nullable
    public UnifyTag<Item> getOwnerByTag(UnifyTag<Item> tag) {
        return refsToOwner.get(tag);
    }
}
