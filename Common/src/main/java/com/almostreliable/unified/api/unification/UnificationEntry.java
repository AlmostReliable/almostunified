package com.almostreliable.unified.api.unification;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Helper to abstract a single entry used in unification.
 * <p>
 * This helper allows easy access to registry information while also offering utility methods.
 *
 * @param <T> the type of the entry
 */
public interface UnificationEntry<T> {

    /**
     * Returns the {@link ResourceKey} this entry is bound to in the {@link Registry}.
     *
     * @return the {@link ResourceKey} this entry is bound to
     */
    ResourceKey<T> key();

    /**
     * Returns the id of this entry.
     * <p>
     * The id is the {@link ResourceLocation} of the entry in the {@link Registry}.
     *
     * @return the id of this entry
     */
    ResourceLocation id();

    /**
     * Returns the raw value of this entry.
     *
     * @return the raw value
     */
    T value();

    /**
     * Returns the tag of this entry.
     * <p>
     * The tag represents the relevant tag used for the unification. Each entry can only have a single unification tag.
     *
     * @return the tag
     */
    TagKey<T> tag();

    /**
     * Returns the value as a {@link Holder.Reference}.
     *
     * @return the value holder
     */
    Holder.Reference<T> asHolderOrThrow();
}
