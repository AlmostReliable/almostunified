package com.almostreliable.unified.api;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface UnifyEntry<T> {
    ResourceKey<T> key();

    ResourceLocation id();

    T value();

    Holder<T> asHolder();
}
