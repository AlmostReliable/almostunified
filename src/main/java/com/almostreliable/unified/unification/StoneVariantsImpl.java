package com.almostreliable.unified.unification;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.unification.StoneVariants;
import com.almostreliable.unified.utils.VanillaTagWrapper;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

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
    private final Map<Identifier, String> itemToStoneVariant;

    private StoneVariantsImpl(Collection<String> stoneVariants, Map<Identifier, String> itemToStoneVariant) {
        this.stoneVariants = sortStoneVariants(stoneVariants);
        this.itemToStoneVariant = itemToStoneVariant;
    }

    public static StoneVariants create(
        Collection<String> stoneVariants, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        Set<TagKey<Item>> stoneVariantItemTags = new HashSet<>();
        Set<TagKey<Block>> stoneVariantBlockTags = new HashSet<>();

        for (String stoneVariant : stoneVariants) {
            Identifier id = Identifier.fromNamespaceAndPath("c", "ores_in_ground/" + stoneVariant);
            stoneVariantItemTags.add(TagKey.create(Registries.ITEM, id));
            stoneVariantBlockTags.add(TagKey.create(Registries.BLOCK, id));
        }

        var itemToStoneVariantTag = mapEntriesToStoneVariantTags(stoneVariantItemTags, itemTags);
        var blockToStoneVariantTag = mapEntriesToStoneVariantTags(stoneVariantBlockTags, blockTags);

        Map<Identifier, String> itemToStoneVariant = new HashMap<>();

        for (var entry : itemToStoneVariantTag.entrySet()) {
            Identifier item = entry.getKey();
            TagKey<Item> tag = entry.getValue();
            String itemTagStoneVariant = getVariantFromStoneVariantTag(stoneVariants, tag);
            if (itemTagStoneVariant != null) {
                itemToStoneVariant.put(item, itemTagStoneVariant);
            }
        }

        for (var entry : blockToStoneVariantTag.entrySet()) {
            Identifier item = entry.getKey();
            TagKey<Block> tag = entry.getValue();
            String blockTagStoneVariant = getVariantFromStoneVariantTag(stoneVariants, tag);
            if (blockTagStoneVariant == null) continue;

            String itemTagStoneVariant = itemToStoneVariant.get(item);
            if (itemTagStoneVariant == null || blockTagStoneVariant.length() > itemTagStoneVariant.length()) {
                itemToStoneVariant.put(item, blockTagStoneVariant);
            }
        }

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
    private static <T> Map<Identifier, TagKey<T>> mapEntriesToStoneVariantTags(Set<TagKey<T>> stoneVariantTags, VanillaTagWrapper<T> tags) {
        Map<Identifier, TagKey<T>> idToStoneVariantTag = new HashMap<>();

        for (var stoneVariantTag : stoneVariantTags) {
            for (var holder : tags.get(stoneVariantTag)) {
                Identifier id = holder
                    .unwrapKey()
                    .orElseThrow(() -> new IllegalStateException("Tag is not bound for holder " + holder))
                    .identifier();

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
     * @param stoneVariants the available stone variants
     * @param tag           the stone variant tag
     * @return the stone variant, or null if the stone strata is not a configured variant
     */
    @Nullable
    private static String getVariantFromStoneVariantTag(Collection<String> stoneVariants, TagKey<?> tag) {
        String tagString = tag.location().toString();
        int i = tagString.lastIndexOf('/');
        String stoneVariant = tagString.substring(i + 1);
        if (!stoneVariants.contains(stoneVariant)) {
            return null;
        }

        return stoneVariant.equals("stone") ? "" : stoneVariant;
    }

    @Override
    public String getStoneVariant(Identifier item) {
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
     * Implementation logic for caching in {@link #getStoneVariant(Identifier)}.
     *
     * @param item the item to get the stone variant of
     * @return the stone variant of the item
     */
    private String computeStoneVariant(Identifier item) {
        for (String stoneVariant : stoneVariants) {
            if (item.getPath().contains(stoneVariant + "_") || item.getPath().endsWith("_" + stoneVariant)) {
                return stoneVariant.equals("stone") ? "" : stoneVariant;
            }
        }

        return "";
    }
}
