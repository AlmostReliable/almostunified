package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.TagSubstitutions;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TagSubstitutionsImpl implements TagSubstitutions {

    private final Map<TagKey<Item>, TagKey<Item>> referencesToSubstitutes;
    private final Multimap<TagKey<Item>, TagKey<Item>> substitutesToReferences;

    private TagSubstitutionsImpl(Map<TagKey<Item>, TagKey<Item>> referencesToSubstitutes, Multimap<TagKey<Item>, TagKey<Item>> substitutesToReferences) {
        this.referencesToSubstitutes = referencesToSubstitutes;
        this.substitutesToReferences = substitutesToReferences;
    }

    /**
     * Creates a new tag substitutions instance that contains immutable maps of all tag substitution relationships.
     * <p>
     * It is ensured that all substitute tags are unify tags and that all reference tags are not unify tags.<br>
     * Since substitute tags have to be unify tags, it is ensured that they are valid and exist in the game. Reference
     * tags still need to be validated.
     *
     * @param validTagFilter        a filter that defines which tags are valid tags and exist in the game
     * @param unifyTagFilter        a filter that defines which tags are unify tags
     * @param configuredSubstitutes the tag substitution relationships from the config
     * @return the new tag substitutions instance
     */
    public static TagSubstitutionsImpl create(Predicate<TagKey<Item>> validTagFilter, Predicate<TagKey<Item>> unifyTagFilter, Map<ResourceLocation, Set<ResourceLocation>> configuredSubstitutes) {
        ImmutableMap.Builder<TagKey<Item>, TagKey<Item>> refsToSubsBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<TagKey<Item>, TagKey<Item>> subsToRefsBuilder = ImmutableMultimap.builder();
        Set<TagKey<Item>> invalidReferenceTags = new HashSet<>();
        Set<TagKey<Item>> unifyReferenceTags = new HashSet<>();

        configuredSubstitutes.forEach((rawSubstituteTag, rawReferenceTags) -> {
            for (ResourceLocation rawReferenceTag : rawReferenceTags) {
                var substituteTag = TagKey.create(Registries.ITEM, rawSubstituteTag);
                var referenceTag = TagKey.create(Registries.ITEM, rawReferenceTag);

                if (!unifyTagFilter.test(substituteTag)) {
                    AlmostUnified.LOGGER.warn(
                            "[TagSubstitutions] Substitute tag '#{}' is not configured as a unify tag! Config entry '#{} -> {}' will be ignored.",
                            substituteTag.location(),
                            substituteTag.location(),
                            rawReferenceTags.stream().map(t -> "#" + t).collect(Collectors.joining(", "))
                    );
                    return; // don't check other reference tags if the substitute tag is invalid
                }

                if (!validTagFilter.test(referenceTag)) {
                    invalidReferenceTags.add(referenceTag);
                    continue; // only skip the current invalid reference tag
                }

                if (unifyTagFilter.test(referenceTag)) {
                    unifyReferenceTags.add(referenceTag);
                    continue; // only skip the current invalid reference tag
                }

                refsToSubsBuilder.put(referenceTag, substituteTag);
                subsToRefsBuilder.put(substituteTag, referenceTag);
            }

            if (!invalidReferenceTags.isEmpty()) {
                AlmostUnified.LOGGER.warn(
                        "[TagSubstitutions] Substitute tag '#{}' contains invalid reference tags! Affected tags: {}",
                        rawSubstituteTag,
                        invalidReferenceTags.stream().map(t -> "#" + t.location()).collect(Collectors.joining(", "))
                );
            }

            if (!unifyReferenceTags.isEmpty()) {
                AlmostUnified.LOGGER.warn(
                        "[TagSubstitutions] Substitute tag '#{}' contains reference tags that are configured as unify tags! Affected tags: {}",
                        rawSubstituteTag,
                        unifyReferenceTags.stream().map(t -> "#" + t.location()).collect(Collectors.joining(", "))
                );
            }
        });

        return new TagSubstitutionsImpl(refsToSubsBuilder.build(), subsToRefsBuilder.build());
    }

    /**
     * Applies tag substitutions to the provided item tags.
     *
     * @param itemTags the item tags to apply the substitutions to
     */
    public void apply(VanillaTagWrapper<Item> itemTags) {
        Multimap<ResourceLocation, ResourceLocation> changedTags = HashMultimap.create();

        substitutesToReferences.asMap().forEach((substituteTag, referenceTags) -> {
            for (var referenceTag : referenceTags) {
                var referenceHolders = itemTags.get(referenceTag.location());
                for (var referenceHolder : referenceHolders) {
                    itemTags.add(substituteTag.location(), referenceHolder);
                    referenceHolder
                            .unwrapKey()
                            .ifPresent(key -> changedTags.put(substituteTag.location(), key.location()));
                }
            }
        });

        changedTags.asMap().forEach((tag, entries) -> AlmostUnified.LOGGER.info(
                "[TagSubstitutions] Added reference tag items to substitute tag '#{}'. Added items: {}",
                tag,
                entries
        ));
    }


    @Override
    @Nullable
    public TagKey<Item> getSubstituteTag(TagKey<Item> referenceTag) {
        return referencesToSubstitutes.get(referenceTag);
    }

    @Override
    public Collection<TagKey<Item>> getReferenceTags(TagKey<Item> substituteTag) {
        return Collections.unmodifiableCollection(substitutesToReferences.get(substituteTag));
    }

    @Override
    public Set<TagKey<Item>> getReferenceTags() {
        return referencesToSubstitutes.keySet();
    }
}
