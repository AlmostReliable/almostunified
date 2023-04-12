package com.almostreliable.unified;

import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.auto.service.AutoService;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AlmostUnifiedLookup.class)
public class AlmostUnifiedLookupImpl implements AlmostUnifiedLookup {

    @Override
    public boolean isLoaded() {
        return AlmostUnified.isRuntimeLoaded();
    }

    @Nullable
    @Override
    public Item getReplacementForItem(ItemLike itemLike) {
        ResourceLocation id = Registry.ITEM.getKey(itemLike.asItem());
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getReplacementForItem(id))
                .flatMap(Registry.ITEM::getOptional)
                .orElse(null);
    }

    @Nullable
    @Override
    public Item getPreferredItemForTag(TagKey<Item> tag) {
        UnifyTag<Item> asUnifyTag = UnifyTag.item(tag.location());
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getPreferredItemForTag(asUnifyTag, $ -> true))
                .flatMap(Registry.ITEM::getOptional)
                .orElse(null);
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
        ResourceLocation id = Registry.ITEM.getKey(itemLike.asItem());
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getPreferredTagForItem(id))
                .map(ut -> TagKey.create(Registry.ITEM_REGISTRY, ut.location()))
                .orElse(null);
    }

    @Nullable
    @Override
    public UnifyTag<Item> getDelegateForRef(ResourceLocation ref) {
        UnifyTag<Item> rafUnifyTag = UnifyTag.item(ref);
        return AlmostUnified
                .getRuntime()
                .getTagDelegateHelper()
                .map(helper -> helper.getDelegateForRef(rafUnifyTag))
                .orElse(null);
    }

    @Override
    public Set<Item> getPotentialItems(TagKey<Item> tag) {
        UnifyTag<Item> asUnifyTag = UnifyTag.item(tag.location());
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .map(tagMap -> tagMap
                        .getItems(asUnifyTag)
                        .stream()
                        .flatMap(rl -> Registry.ITEM.getOptional(rl).stream())
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    @Override
    public Set<TagKey<Item>> getConfiguredTags() {
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .map(tagMap -> tagMap
                        .getTags()
                        .stream()
                        .map(ut -> TagKey.create(Registry.ITEM_REGISTRY, ut.location()))
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }
}
