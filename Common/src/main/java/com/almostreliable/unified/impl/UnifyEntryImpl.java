package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.UnifyEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.Objects;

public class UnifyEntryImpl<T> implements UnifyEntry<T> {

    private final Registry<T> registry;
    private final ResourceKey<T> entryKey;
    @Nullable
    private T value;
    @Nullable
    private TagKey<T> tag;

    public UnifyEntryImpl(Registry<T> registry, ResourceLocation entryKey) {
        this.entryKey = ResourceKey.create(registry.key(), entryKey);
        this.registry = registry;
    }

    public UnifyEntryImpl(Registry<T> registry, T entry) {
        this.entryKey = registry
                .getResourceKey(entry)
                .orElseThrow(() -> new IllegalArgumentException("Entry " + entry + " does not belong to " + registry));
        this.registry = registry;
        this.value = entry;
    }

    public ResourceKey<T> key() {
        return entryKey;
    }

    public ResourceLocation id() {
        return entryKey.location();
    }

    @Override
    public T value() {
        if (value == null) {
            value = registry
                    .getOptional(entryKey)
                    .orElseThrow(() -> new IllegalStateException("Entry " + entryKey + " not found in " + registry));
        }

        return value;
    }

    @Override
    public boolean isTagBound() {
        return tag != null;
    }

    @Override
    public TagKey<T> tag() {
        if (tag == null) {
            throw new IllegalStateException("Tag not bound to " + this);
        }

        return tag;
    }

    @Override
    public UnifyEntry<T> dominantEntry() {
        return null;
    }

    @Override
    public Holder<T> asHolder() {
        return registry.getHolderOrThrow(entryKey);
    }

    public void bindTag(TagKey<T> tag) {
        if (this.tag != null) {
            throw new IllegalStateException("Tag already bound to " + this.tag);
        }

        this.tag = tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entryKey);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UnifyEntry<?> holder) {
            return holder.key() == key();
        }

        return false;
    }

    @Override
    public String toString() {
        return "UnifyEntry{" + key() + "}";
    }
}
