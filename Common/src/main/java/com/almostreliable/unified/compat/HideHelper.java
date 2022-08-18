package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HideHelper {

    public static Collection<ItemStack> createHidingList() {
        UnifyConfig config = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        List<UnifyTag<Item>> unifyTags = config.bakeTags();
        TagMap tagMap = TagMap.create(unifyTags);
        ReplacementMap repMap = new ReplacementMap(tagMap, config);

        return tagMap.getTags().stream().map(unifyTag -> {
            Collection<ResourceLocation> itemsByTag = tagMap.getItems(unifyTag);
            Set<ResourceLocation> replacements = itemsByTag
                    .stream()
                    .map(repMap::getReplacementForItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<ResourceLocation> toHide = itemsByTag.stream().filter(rl -> !replacements.contains(rl)).toList();

            if (!toHide.isEmpty()) {
                AlmostUnified.LOG.info("Hiding {}/{} items for tag {} -> {}",
                        toHide.size(),
                        itemsByTag.size(),
                        unifyTag.location(),
                        toHide);
            }

            return toHide.stream().flatMap(rl -> Registry.ITEM.getOptional(rl).stream()).map(ItemStack::new).toList();
        }).flatMap(Collection::stream).toList();
    }
}
