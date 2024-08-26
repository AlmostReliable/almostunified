package com.almostreliable.unified.unification;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.unification.UnificationEntry;
import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.utils.Utils;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TagInheritance {

    private final Options<Item> itemOptions;
    private final Options<Block> blockOptions;

    public TagInheritance(Mode itemMode, Map<TagKey<Item>, Set<Pattern>> itemInheritance, Mode blockMode, Map<TagKey<Block>, Set<Pattern>> blockInheritance) {
        itemOptions = new Options<>(itemMode, itemInheritance);
        blockOptions = new Options<>(blockMode, blockInheritance);
    }

    public boolean apply(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, List<? extends UnificationLookup> unificationLookups) {
        Multimap<UnificationEntry<Item>, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<UnificationEntry<Item>, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(unificationLookups);
        if (relations.isEmpty()) return false;

        for (var relation : relations) {
            var targetItem = relation.targetItem;
            var targetItemHolder = targetItem.asHolderOrThrow();
            var targetBlockHolder = findTargetBlockHolder(blockTags, targetItem);

            var targetItemTags = itemTags
                .getTags(targetItem)
                .stream()
                .map(rl -> TagKey.create(Registries.ITEM, rl))
                .collect(ImmutableSet.toImmutableSet());

            for (var item : relation.items) {
                var appliedItemTags = applyItemTags(itemTags, targetItemHolder, targetItemTags, item);
                changedItemTags.putAll(targetItem, appliedItemTags);

                if (targetBlockHolder != null) {
                    var appliedBlockTags = applyBlockTags(blockTags, targetBlockHolder, targetItemTags, item);
                    changedBlockTags.putAll(targetItem, appliedBlockTags);
                }
            }
        }

        if (!changedBlockTags.isEmpty()) {
            changedBlockTags.asMap().forEach((target, tags) -> {
                AlmostUnifiedCommon.LOGGER.info("[TagInheritance] Added '{}' to block tags {}", target.id(), tags);
            });
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((target, tags) -> {
                AlmostUnifiedCommon.LOGGER.info("[TagInheritance] Added '{}' to item tags {}", target.id(), tags);
            });
            return true;
        }

        return false;
    }

    @Nullable
    private Holder<Block> findTargetBlockHolder(VanillaTagWrapper<Block> tagMap, UnificationEntry<Item> targetItem) {
        var blockTags = tagMap.getTags(targetItem.id());
        if (blockTags.isEmpty()) return null;

        return BuiltInRegistries.BLOCK.getHolderOrThrow(ResourceKey.create(Registries.BLOCK, targetItem.id()));
    }

    private Set<ResourceLocation> applyItemTags(VanillaTagWrapper<Item> vanillaTags, Holder<Item> targetItem, Set<TagKey<Item>> targetItemTags, UnificationEntry<Item> item) {
        var itemTags = vanillaTags.getTags(item);
        Set<ResourceLocation> changed = new HashSet<>();

        for (var itemTag : itemTags) {
            var tag = TagKey.create(Registries.ITEM, itemTag);
            if (itemOptions.shouldInherit(tag, targetItemTags) && addToVanilla(targetItem, tag, vanillaTags)) {
                changed.add(itemTag);
            }
        }

        return changed;
    }

    private Set<ResourceLocation> applyBlockTags(VanillaTagWrapper<Block> blockTagMap, Holder<Block> targetBlock, Set<TagKey<Item>> targetItemTags, UnificationEntry<Item> item) {
        var blockTags = blockTagMap.getTags(item.id());
        Set<ResourceLocation> changed = new HashSet<>();

        for (var blockTag : blockTags) {
            var tag = TagKey.create(Registries.BLOCK, blockTag);
            if (blockOptions.shouldInherit(tag, targetItemTags) && addToVanilla(targetBlock, tag, blockTagMap)) {
                changed.add(blockTag);
            }
        }

        return changed;
    }

    /**
     * Add given holder to the given tag.
     *
     * @param holder      The holder
     * @param tag         The tag the holder should be added to
     * @param vanillaTags The vanilla tag wrapper
     * @return true if the holder was added, false if it was already present
     */
    private static <T> boolean addToVanilla(Holder<T> holder, TagKey<T> tag, VanillaTagWrapper<T> vanillaTags) {
        var tagHolders = vanillaTags.get(tag);
        if (tagHolders.contains(holder)) return false; // already present, no need to add it again

        vanillaTags.add(tag.location(), holder);
        return true;
    }

    private Set<TagRelation> resolveRelations(Collection<? extends UnificationLookup> unificationLookups) {
        Set<TagRelation> relations = new HashSet<>();

        for (var unificationLookup : unificationLookups) {
            for (TagKey<Item> unifyTag : unificationLookup.getTags()) {
                if (itemOptions.skipForInheritance(unifyTag) && blockOptions.skipForInheritance(unifyTag)) {
                    continue;
                }

                var itemsByTag = unificationLookup.getTagEntries(unifyTag);

                // avoid handling single entries and tags that only contain the same namespace for all items
                if (Utils.allSameNamespace(itemsByTag)) continue;

                var target = unificationLookup.getTagTargetItem(unifyTag);
                if (target == null) continue;

                var items = removeTargetItem(itemsByTag, target);

                if (items.isEmpty()) continue;
                relations.add(new TagRelation(unifyTag, target, items));
            }
        }

        return relations;
    }

    /**
     * Returns a set of all items that are not the target item and are valid by checking if they are registered.
     *
     * @param holders The set of all items that are in the tag
     * @param target  The target item
     * @return A set of all items that are not the target item and are valid
     */
    private Set<UnificationEntry<Item>> removeTargetItem(Collection<UnificationEntry<Item>> holders, UnificationEntry<Item> target) {
        Set<UnificationEntry<Item>> result = new HashSet<>(holders.size());
        for (var holder : holders) {
            if (!holder.equals(target)) {
                result.add(holder);
            }
        }

        return result;
    }

    private record TagRelation(TagKey<Item> tag, UnificationEntry<Item> targetItem,
                               Set<UnificationEntry<Item>> items) {}

    public enum Mode {
        ALLOW,
        DENY
    }

    private record Options<T>(Mode mode, Map<TagKey<T>, Set<Pattern>> inheritance) {

        /**
         * Checks if given tag is used in the inheritance config.
         * <p>
         * If mode is allowed, the tag should match any pattern in the config. If mode is deny, the tag should not match
         * any pattern in the config.
         *
         * @param tag The tag to check
         * @return True if the tag should be skipped
         */
        public boolean skipForInheritance(TagKey<Item> tag) {
            var tagStr = tag.location().toString();
            boolean modeResult = mode == Mode.ALLOW;
            for (Set<Pattern> patterns : inheritance.values()) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(tagStr).matches()) {
                        return !modeResult;
                    }
                }
            }

            return modeResult;
        }

        /**
         * Checks if given inheritance tag would match any of the target item tags.
         * <p>
         * E. g based on a simple config:
         * <pre>
         * {@code {
         *     "minecraft:beacon_payment_items": [
         *          "c:ores/silver"
         *     ]
         * }}
         * </pre>
         * "minecraft:beacon_payment_items" would be the inheritance tag and "c:ores/silver" would be one of the target item tags.
         * If mode is {@code DENY}, the check would be inverted.
         *
         * @param inheritanceTag The inheritance tag
         * @param targetItemTags The target item tags
         * @return True if we should allow the inheritance or false if we should deny the inheritance
         */
        public boolean shouldInherit(TagKey<T> inheritanceTag, Collection<TagKey<Item>> targetItemTags) {
            var patterns = inheritance.getOrDefault(inheritanceTag, Set.of());
            boolean result = checkPatterns(targetItemTags, patterns);
            // noinspection SimplifiableConditionalExpression
            return mode == Mode.ALLOW ? result : !result;
        }

        private boolean checkPatterns(Collection<TagKey<Item>> tags, Collection<Pattern> patterns) {
            for (var pattern : patterns) {
                for (var tag : tags) {
                    if (pattern.matcher(tag.location().toString()).matches()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
