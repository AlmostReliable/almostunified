package com.almostreliable.unified.utils;

import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.impl.TagMapImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

// TODO make modified tags in vanillaTags map immutable again
public class VanillaTagWrapper<T> {

    private final Registry<T> registry;
    private final Map<ResourceLocation, Collection<Holder<T>>> vanillaTags;
    @Nullable
    private Map<Holder<T>, Set<ResourceLocation>> holdersToTags;
    private final Set<ResourceLocation> modifiedTags = new HashSet<>();

    public static <T> VanillaTagWrapper<T> of(Registry<T> registry, Map<ResourceLocation, Collection<Holder<T>>> vanillaTags) {
        return new VanillaTagWrapper<>(registry, vanillaTags);
    }

    public VanillaTagWrapper(Registry<T> registry, Map<ResourceLocation, Collection<Holder<T>>> vanillaTags) {
        this.registry = registry;
        this.vanillaTags = vanillaTags;
    }

    public TagMap<T> createUnifyTagMap(Predicate<TagKey<T>> tagFilter, Predicate<ResourceLocation> entryFilter) {
        var builder = new TagMapImpl.Builder<>(registry);

        for (var entry : vanillaTags.entrySet()) {
            var tag = TagKey.create(registry.key(), entry.getKey());
            if (!tagFilter.test(tag)) {
                continue;
            }

            for (Holder<T> holder : entry.getValue()) {
                registry.getResourceKey(holder.value()).ifPresent(key -> {
                    if (entryFilter.test(key.location())) {
                        builder.put(tag, key.location());
                    }
                });
            }
        }

        return builder.build();
    }

    public void addHolder(ResourceLocation tag, Holder<T> holder) {
        Collection<Holder<T>> existingHolders = vanillaTags.get(tag);
        if (existingHolders == null) {
            return;
        }

        if (modifiedTags.contains(tag)) {
            existingHolders.add(holder);
            return;
        }

        Set<Holder<T>> newHolders = new HashSet<>(existingHolders);
        newHolders.add(holder);
        vanillaTags.put(tag, newHolders);
        modifiedTags.add(tag);
    }

    public Collection<Holder<T>> get(TagKey<T> tag) {
        return Collections.unmodifiableCollection(vanillaTags.getOrDefault(tag.location(), Collections.emptyList()));
    }

    public Collection<Holder<T>> get(ResourceLocation tag) {
        return Collections.unmodifiableCollection(vanillaTags.getOrDefault(tag, Collections.emptyList()));
    }

    public Set<ResourceLocation> getTags(ResourceLocation entryId) {
        var key = ResourceKey.create(registry.key(), entryId);
        return registry.getHolder(key).map(this::getTags).orElse(Set.of());
    }

    public Set<ResourceLocation> getTags(UnifyEntry<T> entry) {
        return getTags(entry.asHolder());
    }

    public Set<ResourceLocation> getTags(Holder<T> holder) {
        if (holdersToTags == null) {
            holdersToTags = createInvertMap();
        }

        return holdersToTags.getOrDefault(holder, Set.of());
    }

    private Map<Holder<T>, Set<ResourceLocation>> createInvertMap() {
        Map<Holder<T>, Set<ResourceLocation>> map = new HashMap<>();

        for (var entry : vanillaTags.entrySet()) {
            for (Holder<T> holder : entry.getValue()) {
                map.putIfAbsent(holder, new HashSet<>());
                map.get(holder).add(entry.getKey());
            }
        }


        return map;
    }
}
