package com.almostreliable.unified.unification;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.unification.StoneVariants;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class StoneVariantsImpl implements StoneVariants {

    private static final Pattern ORE_TAG_PATTERN = Pattern.compile("(c:ores/.+|(minecraft|c):.+_ores)");

    private final Map<TagKey<Item>, Boolean> isOreTagCache = new HashMap<>();

    private final List<String> stoneVariants;
    private final Map<ResourceLocation, String> itemToStoneVariant;

    private StoneVariantsImpl(Collection<String> stoneVariants, Map<ResourceLocation, String> itemToStoneVariant) {
        this.stoneVariants = sortStoneVariants(stoneVariants);
        this.itemToStoneVariant = itemToStoneVariant;
    }

    public static StoneVariants create(Collection<String> stoneVariants, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        Set<TagKey<Item>> stoneVariantItemTags = new HashSet<>();
        Set<TagKey<Block>> stoneVariantBlockTags = new HashSet<>();

        for (String stoneVariant : stoneVariants) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("c", "ores_in_ground/" + stoneVariant);
            stoneVariantItemTags.add(TagKey.create(Registries.ITEM, id));
            stoneVariantBlockTags.add(TagKey.create(Registries.BLOCK, id));
        }

        var itemToStoneVariantTag = mapEntriesToStoneVariantTags(stoneVariantItemTags, itemTags);
        var blockToStoneVariantTag = mapEntriesToStoneVariantTags(stoneVariantBlockTags, blockTags);

        Map<ResourceLocation, String> itemToStoneVariant = new HashMap<>();
        itemToStoneVariantTag.forEach((item, tag) -> {
            String itemStoneVariant = getVariantFromStoneVariantTag(tag);

            var blockTagFromItem = blockToStoneVariantTag.get(item);
            if (blockTagFromItem != null) {
                String blockStoneVariant = getVariantFromStoneVariantTag(blockTagFromItem);
                if (blockStoneVariant.length() > itemStoneVariant.length()) {
                    itemToStoneVariant.put(item, blockStoneVariant);
                    return;
                }
            }

            itemToStoneVariant.put(item, itemStoneVariant);
        });

        return new StoneVariantsImpl(stoneVariants, itemToStoneVariant);
    }

    /**
     * Maps all entries of a stone variant tag to its respective stone variant tag.
     *
     * @param stoneVariantTags the stone variant tags
     * @param tags             the vanilla tag wrapper to get the tag entries from
     * @param <T>              the tag type
     * @return the entry to stone variant tag mapping
     */
    private static <T> Map<ResourceLocation, TagKey<T>> mapEntriesToStoneVariantTags(Set<TagKey<T>> stoneVariantTags, VanillaTagWrapper<T> tags) {
        Map<ResourceLocation, TagKey<T>> idToStoneVariantTag = new HashMap<>();

        for (var stoneVariantTag : stoneVariantTags) {
            for (var holder : tags.get(stoneVariantTag)) {
                ResourceLocation id = holder
                    .unwrapKey()
                    .orElseThrow(() -> new IllegalStateException("Tag is not bound for holder " + holder))
                    .location();

                var oldTag = idToStoneVariantTag.put(id, stoneVariantTag);
                if (oldTag != null) {
                    AlmostUnifiedCommon.LOGGER.error(
                        "{} is bound to multiple stone variant tags: {} and {}",
                        id,
                        oldTag,
                        stoneVariantTag
                    );
                }
            }
        }

        return idToStoneVariantTag;
    }

    /**
     * Returns the variant of a stone variant tag.
     * <p>
     * Example: {@code c:ores_in_ground/deepslate} -> {@code deepslate}
     *
     * @param tag the stone variant tag
     * @return the stone variant
     */
    private static String getVariantFromStoneVariantTag(TagKey<?> tag) {
        String tagString = tag.location().toString();
        int i = tagString.lastIndexOf('/');
        String stoneVariant = tagString.substring(i + 1);
        stoneVariant = stoneVariant.equals("stone") ? "" : stoneVariant;
        return stoneVariant;
    }

    @Override
    public String getStoneVariant(ResourceLocation item) {
        return itemToStoneVariant.computeIfAbsent(item, this::computeStoneVariant);
    }

    @Override
    public boolean isOreTag(TagKey<Item> tag) {
        return isOreTagCache.computeIfAbsent(tag, t -> ORE_TAG_PATTERN.matcher(t.location().toString()).matches());
    }

    /**
     * Returns a list of all stone variants sorted from longest to shortest.
     * <p>
     * This is required to ensure that the longest variant is returned first and no sub-matches happen.<br>
     * Example: "nether" and "blue_nether" would both match "nether" if the list is not sorted.
     *
     * @param stoneVariants the stone variants to sort
     * @return the sorted stone variants
     */
    private static List<String> sortStoneVariants(Collection<String> stoneVariants) {
        return stoneVariants.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
    }

    /**
     * Implementation logic for caching in {@link #getStoneVariant(ResourceLocation)}.
     *
     * @param item the item to get the stone variant of
     * @return the stone variant of the item
     */
    private String computeStoneVariant(ResourceLocation item) {
        for (String stoneVariant : stoneVariants) {
            if (item.getPath().contains(stoneVariant + "_") || item.getPath().endsWith("_" + stoneVariant)) {
                return stoneVariant.equals("stone") ? "" : stoneVariant;
            }
        }

        return "";
    }
}
