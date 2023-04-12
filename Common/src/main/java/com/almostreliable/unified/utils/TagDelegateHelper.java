package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TagDelegateHelper {

    private final Map<ResourceLocation, Set<ResourceLocation>> configDelegates;
    private final Map<ResourceLocation, ResourceLocation> delegateToParent = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> parentToDelegates = new HashMap<>();

    public TagDelegateHelper(Map<ResourceLocation, Set<ResourceLocation>> configDelegates) {
        this.configDelegates = configDelegates;
    }

    public void validate(List<UnifyTag<Item>> allowedTags) {
        configDelegates.forEach((parent, children) -> {
            if (allowedTags.stream().map(UnifyTag::location).noneMatch(parent::equals)) {
                AlmostUnified.LOG.warn("Tag delegate parent {} is not a unify tag!", parent);
                return;
            }

            for (var child : children) {
                if (allowedTags.stream().map(UnifyTag::location).anyMatch(child::equals)) {
                    AlmostUnified.LOG.warn("Tag delegate child {} is a unify tag!", child);
                    continue;
                }
                delegateToParent.put(child, parent);
            }
        });
    }
}
