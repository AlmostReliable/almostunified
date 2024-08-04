package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.TagSubstitutions;
import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.utils.CompositeCollection;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class CompositeUnifyLookup implements UnifyLookup {

    private final Iterable<? extends UnifyLookup> unifyLookups;
    private final TagSubstitutions tagSubstitutions;
    @Nullable
    private Collection<TagKey<Item>> unifiedTagsView;

    public CompositeUnifyLookup(Iterable<? extends UnifyLookup> unifyLookups, TagSubstitutions tagSubstitutions) {
        this.unifyLookups = unifyLookups;
        this.tagSubstitutions = tagSubstitutions;
    }

    @Override
    public Collection<TagKey<Item>> getUnifiedTags() {
        if (unifiedTagsView == null) {
            Collection<Collection<TagKey<Item>>> iterables = new ArrayList<>();
            for (var unifyLookup : unifyLookups) {
                iterables.add(unifyLookup.getUnifiedTags());
            }

            unifiedTagsView = new CompositeCollection<>(iterables);
        }

        return unifiedTagsView;
    }

    @Override
    public Collection<UnifyEntry<Item>> getEntries(TagKey<Item> tag) {
        for (var unifyLookup : unifyLookups) {
            var resultItems = unifyLookup.getEntries(tag);
            if (!resultItems.isEmpty()) {
                return resultItems;
            }
        }

        return Collections.emptyList();
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getEntry(ResourceLocation entry) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getEntry(entry);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getEntry(Item item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getEntry(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Item item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Holder<Item> item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(ResourceLocation item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(Item item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(Holder<Item> item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag) {
        for (var unifyLookup : unifyLookups) {
            var result = unifyLookup.getPreferredEntryForTag(tag);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        for (var unifyLookup : unifyLookups) {
            var result = unifyLookup.getPreferredEntryForTag(tag, itemFilter);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
        for (var unifyLookup : unifyLookups) {
            if (unifyLookup.isItemInUnifiedIngredient(ingredient, item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TagSubstitutions getTagSubstitutions() {
        return tagSubstitutions;
    }
}
