package com.almostreliable.unified.unification;

import com.almostreliable.unified.api.unification.UnificationEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.Objects;

public class UnificationEntryImpl<T> implements UnificationEntry<T> {

    private final Registry<T> registry;
    private final ResourceKey<T> key;

    @Nullable private T value;
    @Nullable private TagKey<T> tag;

    public UnificationEntryImpl(Registry<T> registry, ResourceLocation key) {
        this.registry = registry;
        this.key = ResourceKey.create(registry.key(), key);
    }

    public UnificationEntryImpl(Registry<T> registry, T entry) {
        this.key = registry
                .getResourceKey(entry)
                .orElseThrow(() -> new IllegalArgumentException("Entry " + entry + " does not belong to " + registry));
        this.registry = registry;
        this.value = entry;
    }

    @Override
    public ResourceKey<T> key() {
        return key;
    }

    @Override
    public ResourceLocation id() {
        return key.location();
    }

    @Override
    public T value() {
        if (value == null) {
            value = registry
                    .getOptional(key)
                    .orElseThrow(() -> new IllegalStateException("entry " + key + " not found in " + registry));
        }

        return value;
    }

    @Override
    public TagKey<T> tag() {
        if (tag == null) {
            throw new IllegalStateException("tag not bound to " + this);
        }

        return tag;
    }

    @Override
    public Holder.Reference<T> asHolderOrThrow() {
        return registry.getHolderOrThrow(key);
    }

    public void bindTag(TagKey<T> tag) {
        if (this.tag != null) {
            throw new IllegalStateException("tag already bound to " + this.tag);
        }

        this.tag = tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UnificationEntry<?> holder && holder.key() == key();
    }

    @Override
    public String toString() {
        return "UnificationEntry{" + key() + "}";
    }
}
