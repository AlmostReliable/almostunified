package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface Placeholders {

    Collection<ResourceLocation> inflate(String str);

    Collection<String> getKeys();

    Collection<String> getValues(String key);

    void forEach(BiConsumer<String, Collection<String>> consumer);
}
