package com.almostreliable.unified;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FakeResourceKeyRegistry {
    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<Registry<T>> create(String name) {
        try {
            Constructor<?> c = ResourceKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
            c.setAccessible(true);
            return (ResourceKey<Registry<T>>) c.newInstance(new ResourceLocation("test_registry"),
                    new ResourceLocation(name));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
