package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ModPriorities extends Iterable<String> {

    @Nullable
    String getPriorityOverride(TagKey<Item> tag);

    @Nullable
    ResourceLocation findPreferredItemId(TagKey<Item> tag, List<ResourceLocation> items);

    void forEachOverride(BiConsumer<TagKey<Item>, String> callback);

    default Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
