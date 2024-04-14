package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.TagOwnerships;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class TagOwnershipsImpl implements TagOwnerships {

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
    private final Map<TagKey<Item>, TagKey<Item>> refsToOwner;
    private final Multimap<TagKey<Item>, TagKey<Item>> ownerToRefs;

    /**
     * Creates a new TagOwnerships instance that contains immutable maps of all tag ownership relationships.
     * <p>
     * It is ensured that all owner tags are present in the {@code unifyTags} set, and that all reference tags
     * aren't present in the {@code unifyTags} set.
     *
     * @param enabledTagsFilter Filter for all enables tags which may be used in unification
     * @param rawTagOwnerships  The map of all tag ownership relationships.
     */
    public TagOwnershipsImpl(Predicate<TagKey<Item>> enabledTagsFilter, Map<ResourceLocation, Set<ResourceLocation>> rawTagOwnerships) {
        ImmutableMap.Builder<TagKey<Item>, TagKey<Item>> refsToOwnerBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<TagKey<Item>, TagKey<Item>> ownerToRefsBuilder = ImmutableMultimap.builder();

        rawTagOwnerships.forEach((rawOwner, rawRefs) -> {
            for (ResourceLocation rawRef : rawRefs) {
                TagKey<Item> owner = TagKey.create(Registries.ITEM, rawOwner);
                TagKey<Item> ref = TagKey.create(Registries.ITEM, rawRef);

                if (!enabledTagsFilter.test(owner)) {
                    AlmostUnified.LOG.warn(
                            "[TagOwnerships] Owner tag '#{}' is not present in the unify tag list!",
                            owner.location()
                    );
                    continue;
                }

                if (enabledTagsFilter.test(ref)) {
                    AlmostUnified.LOG.warn(
                            "[TagOwnerships] Reference tag '#{}' of owner tag '#{}' is present in the unify tag list!",
                            ref.location(),
                            owner.location()
                    );
                    continue;
                }

                refsToOwnerBuilder.put(ref, owner);
                ownerToRefsBuilder.put(owner, ref);
            }
        });

        this.refsToOwner = refsToOwnerBuilder.build();
        this.ownerToRefs = ownerToRefsBuilder.build();
    }

    public TagOwnershipsImpl() {
        this(t -> false, Collections.emptyMap());
    }

    /**
     * Applies tag ownerships to the provided raw tags.
     * <p>
     * The raw tags are then processed by the game and actual tags are created.
     *
     * @param rawTags The raw tags to apply ownerships to.
     */
    public void applyOwnerships(VanillaTagWrapper<Item> rawTags) {
        Multimap<ResourceLocation, ResourceLocation> changedTags = HashMultimap.create();

        ownerToRefs.asMap().forEach((owner, refs) -> {
            var rawHolders = rawTags.get(owner.location());
            if (rawHolders.isEmpty()) {
                AlmostUnified.LOG.warn("[TagOwnerships] Owner tag '#{}' does not exist!", owner.location());
                return;
            }

            for (var ref : refs) {
                var refHolders = rawTags.get(ref.location());
                if (refHolders.isEmpty()) {
                    AlmostUnified.LOG.warn(
                            "[TagOwnerships] Reference tag '#{}' of owner tag '#{}' does not exist!",
                            ref.location(),
                            owner.location()
                    );
                    continue;
                }

                for (Holder<Item> holder : refHolders) {
                    rawTags.add(owner.location(), holder);
                    holder.unwrapKey().ifPresent(key -> changedTags.put(owner.location(), key.location()));
                }
            }
        });

        if (!changedTags.isEmpty()) {
            changedTags.asMap().forEach((tag, items) -> {
                AlmostUnified.LOG.info("[TagOwnerships] Modified tag '#{}', added {}", tag, items);
            });
        }
    }


    @Override
    @Nullable
    public TagKey<Item> getOwner(TagKey<Item> referenceTag) {
        return refsToOwner.get(referenceTag);
    }

    @Override
    public Collection<TagKey<Item>> getRefs(TagKey<Item> ownerTag) {
        return Collections.unmodifiableCollection(ownerToRefs.get(ownerTag));
    }

    @Override
    public Set<TagKey<Item>> getRefs() {
        return refsToOwner.keySet();
    }
}
