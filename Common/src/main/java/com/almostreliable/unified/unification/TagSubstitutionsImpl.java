package com.almostreliable.unified.unification;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.unification.TagSubstitutions;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TagSubstitutionsImpl implements TagSubstitutions {

    private final Map<TagKey<Item>, TagKey<Item>> replacedToSubstitute;
    private final Multimap<TagKey<Item>, TagKey<Item>> substituteToReplaced;

    private TagSubstitutionsImpl(Map<TagKey<Item>, TagKey<Item>> replacedToSubstitute, Multimap<TagKey<Item>, TagKey<Item>> substituteToReplaced) {
        this.replacedToSubstitute = replacedToSubstitute;
        this.substituteToReplaced = substituteToReplaced;
    }

    /**
     * Creates a new tag substitutions instance that contains immutable maps of all tag substitution relationships.
     * <p>
     * This method ensures that all substitute tags are unify tags and that all replaced tags are not unify tags.<br>
     * Since substitute tags have to be unify-tags, it is ensured that they are valid and exist in the game. Replaced
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
        Set<TagKey<Item>> invalidReplacedTags = new HashSet<>();
        Set<TagKey<Item>> unifyReplacedTags = new HashSet<>();

        configuredSubstitutes.forEach((rawSubstituteTag, rawReplacedTags) -> {
            for (ResourceLocation rawReplacedTag : rawReplacedTags) {
                var substituteTag = TagKey.create(Registries.ITEM, rawSubstituteTag);
                var replacedTag = TagKey.create(Registries.ITEM, rawReplacedTag);

                if (!unifyTagFilter.test(substituteTag)) {
                    AlmostUnifiedCommon.LOGGER.warn(
                        "[TagSubstitutions] Substitute tag '#{}' is not configured as a unify tag! Config entry '#{} -> {}' will be ignored.",
                        substituteTag.location(),
                        substituteTag.location(),
                        rawReplacedTags.stream().map(t -> "#" + t).collect(Collectors.joining(", "))
                    );
                    return; // don't check other replaced tags if the substitute tag is invalid
                }

                if (!validTagFilter.test(replacedTag)) {
                    invalidReplacedTags.add(replacedTag);
                    continue; // only skip the current invalid replaced tag
                }

                if (unifyTagFilter.test(replacedTag)) {
                    unifyReplacedTags.add(replacedTag);
                    continue; // only skip the current invalid replaced tag
                }

                refsToSubsBuilder.put(replacedTag, substituteTag);
                subsToRefsBuilder.put(substituteTag, replacedTag);
            }

            if (!invalidReplacedTags.isEmpty()) {
                AlmostUnifiedCommon.LOGGER.warn(
                    "[TagSubstitutions] Substitute tag '#{}' contains invalid replaced tags! Affected tags: {}",
                    rawSubstituteTag,
                    invalidReplacedTags.stream().map(t -> "#" + t.location()).collect(Collectors.joining(", "))
                );
            }

            if (!unifyReplacedTags.isEmpty()) {
                AlmostUnifiedCommon.LOGGER.warn(
                    "[TagSubstitutions] Substitute tag '#{}' contains replaced tags that are configured as unify tags! Affected tags: {}",
                    rawSubstituteTag,
                    unifyReplacedTags.stream().map(t -> "#" + t.location()).collect(Collectors.joining(", "))
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

        substituteToReplaced.asMap().forEach((substituteTag, replacedTags) -> {
            for (var replacedTag : replacedTags) {
                var replacedTagHolders = itemTags.get(replacedTag.location());
                for (var replacedTagHolder : replacedTagHolders) {
                    itemTags.add(substituteTag.location(), replacedTagHolder);
                    replacedTagHolder
                        .unwrapKey()
                        .ifPresent(key -> changedTags.put(substituteTag.location(), key.location()));
                }
            }
        });

        changedTags.asMap().forEach((tag, entries) -> AlmostUnifiedCommon.LOGGER.info(
            "[TagSubstitutions] Added items of replaced tags to substitute tag '#{}'. Added items: {}",
            tag,
            entries
        ));
    }


    @Override
    @Nullable
    public TagKey<Item> getSubstituteTag(TagKey<Item> replacedTag) {
        return replacedToSubstitute.get(replacedTag);
    }

    @Override
    public Collection<TagKey<Item>> getReplacedTags(TagKey<Item> substituteTag) {
        return Collections.unmodifiableCollection(substituteToReplaced.get(substituteTag));
    }

    @Override
    public Set<TagKey<Item>> getReplacedTags() {
        return replacedToSubstitute.keySet();
    }
}
