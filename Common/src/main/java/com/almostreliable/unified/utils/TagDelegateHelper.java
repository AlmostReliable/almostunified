package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

public class TagDelegateHelper {

    /**
     * A map of reference tags to their delegate tags.
     * <p>
     * Example:<br>
     * If the map contains the entry {@code minecraft:logs -> minecraft:planks},
     * any recipes where the tag {@code minecraft:logs} is being used, it will
     * replace the tag with {@code minecraft:planks}.
     * <p>
     * Map Key = Tag to replace<br>
     * Map Value = Tag to delegate to
     */
    private final Map<UnifyTag<Item>, UnifyTag<Item>> refToDelegate = new HashMap<>();
    private final Multimap<UnifyTag<Item>, UnifyTag<Item>> delegateToRefs = HashMultimap.create();

    public TagDelegateHelper(Map<ResourceLocation, Set<ResourceLocation>> tagDelegates) {
        tagDelegates.forEach((rawDelegate, rawRefs) -> {
            rawRefs.forEach(rawRef -> {
                UnifyTag<Item> delegate = UnifyTag.item(rawDelegate);
                UnifyTag<Item> ref = UnifyTag.item(rawRef);
                refToDelegate.put(ref, delegate);
                delegateToRefs.put(delegate, ref);
            });
        });
    }

    /**
     * Ensures that all tag delegates are also unify tags and that all delegate refs are no unify tags.
     *
     * @param unifyTags The list of unify tags.
     */
    public void validate(List<UnifyTag<Item>> unifyTags) {
        Set<UnifyTag<Item>> tags = new HashSet<>(unifyTags);

        for (UnifyTag<Item> delegate : delegateToRefs.keySet()) {
            if (!tags.contains(delegate)) {
                AlmostUnified.LOG.warn("Tag delegate {} is not present in the unify tag list.", delegate.location());
            }
        }

        for (var entry : refToDelegate.entrySet()) {
            UnifyTag<Item> ref = entry.getKey();
            UnifyTag<Item> delegate = entry.getValue();
            if (tags.contains(ref)) {
                AlmostUnified.LOG.warn(
                        "Tag {} is present in the unify tag list, but is also marked as ref for delegate {}.",
                        ref.location(),
                        delegate.location()
                );
                // TODO: is it safe to not remove the ref from the maps?
            }
        }
    }

    /**
     * Gets holders of all refs of the provided delegate tag.
     * <p>
     * Ensures every ref is an actual tag.
     *
     * @param tags     The global tag map.
     * @param delegate The delegate tag to get all ref holders for.
     * @param <T>      The type of the tag.
     * @return A list of holders for all refs of the delegate tag.
     */
    public <T> List<Holder<T>> getHoldersForDelegate(Map<ResourceLocation, Collection<Holder<T>>> tags, UnifyTag<Item> delegate) {
        var refs = delegateToRefs.get(delegate);
        List<Holder<T>> holders = new ArrayList<>();
        for (var ref : refs) {
            var refHolders = tags.get(ref.location());
            if (refHolders == null) {
                AlmostUnified.LOG.warn("Tag delegate ref '{}' for tag '{}' does not exist", ref, delegate);
            } else {
                holders.addAll(refHolders);
            }
        }
        return holders;
    }

    /**
     * Gets the delegate tag for the provided ref tag.
     *
     * @param ref The ref tag to get the delegate for.
     * @return The delegate tag.
     */
    @Nullable
    public UnifyTag<Item> getDelegateForRef(UnifyTag<Item> ref) {
        return refToDelegate.get(ref);
    }
}
