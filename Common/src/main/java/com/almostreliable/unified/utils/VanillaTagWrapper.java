package com.almostreliable.unified.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.almostreliable.unified.api.unification.UnificationEntry;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base wrapper to store vanilla tags and their holders. The wrapper allows to add new tags and holders to a tag.
 * By default, the holder collection for each tag is immutable. When attempting to modify the collection it will be copied and marked as modified.
 * <p>
 * After all operations are done, the vanilla tags should be sealed with {@link VanillaTagWrapper#seal()} to prevent further changes.
 *
 * @param <T>
 */
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

    public void add(ResourceLocation tag, Holder<T> holder) {
        if (modifiedTags.contains(tag)) {
            vanillaTags.get(tag).add(holder);
            return;
        }

        Collection<Holder<T>> existingHolders = vanillaTags.get(tag);
        Collection<Holder<T>> newHolders = existingHolders == null ? new HashSet<>() : new HashSet<>(existingHolders);
        newHolders.add(holder);
        vanillaTags.put(tag, newHolders);
        modifiedTags.add(tag);
    }

    public boolean has(TagKey<T> tag) {
        return vanillaTags.containsKey(tag.location());
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

    public Set<ResourceLocation> getTags(UnificationEntry<T> entry) {
        return getTags(entry.asHolderOrThrow());
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

    public void seal() {
        for (ResourceLocation modifiedTag : modifiedTags) {
            Collection<Holder<T>> holders = vanillaTags.get(modifiedTag);
            if (holders != null) {
                vanillaTags.put(modifiedTag, List.copyOf(holders));
            }
        }

        modifiedTags.clear();
        holdersToTags = null;
    }
}
