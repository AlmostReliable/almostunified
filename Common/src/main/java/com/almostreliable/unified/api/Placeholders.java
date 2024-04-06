package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface Placeholders {

    Collection<ResourceLocation> inflate(String str);

    Collection<String> getKeys();

    Collection<String> getValues(String key);
}