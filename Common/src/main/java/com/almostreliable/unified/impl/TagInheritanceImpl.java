package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.TagInheritance;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TagInheritanceImpl<T> implements TagInheritance<T> {
    private final Mode mode;
    private final Map<TagKey<T>, Set<Pattern>> inheritance;

    public TagInheritanceImpl(Mode mode, Map<TagKey<T>, Set<Pattern>> inheritance) {
        this.mode = mode;
        this.inheritance = inheritance;
    }

    @Override
    public boolean skipForInheritance(TagKey<Item> unifyEntry) {
        var asLoc = unifyEntry.location().toString();
        boolean modeResult = mode == Mode.ALLOW;
        for (Set<Pattern> patterns : inheritance.values()) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(asLoc).matches()) {
                    return !modeResult;
                }
            }
        }

        return modeResult;
    }

    @Override
    public boolean shouldInherit(TagKey<T> tag, Collection<TagKey<Item>> tags) {
        var patterns = inheritance.getOrDefault(tag, Set.of());
        boolean result = checkPatterns(tags, patterns);
        //noinspection SimplifiableConditionalExpression
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

    public enum Mode {
        ALLOW,
        DENY;
    }
}
