package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.AlmostUnifiedRuntime;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MissingRecipesDumper {

    public static void write(AlmostUnifiedRuntime runtime, boolean dumpPotentialMissingRecipes, Map<ResourceLocation, JsonElement> recipes) {
        if (!dumpPotentialMissingRecipes) return;

        var itemsPerTag = getHidingIds(runtime);
        FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getLogPath(), "recipe_hiding_check.txt", sb -> {
            recipes.forEach(((recipe, json) -> {
                if (json instanceof JsonObject obj) {
                    write(recipe, obj, itemsPerTag, sb);
                }
            }));
        });
    }

    private static void write(ResourceLocation recipe, JsonObject json, Multimap<UnifyTag<Item>, ResourceLocation> itemsPerTag, StringBuilder sb) {
        String jsonStr = json.toString();
        Multimap<UnifyTag<Item>, ResourceLocation> found = HashMultimap.create();
        itemsPerTag.asMap().forEach((tag, items) -> {
            for (ResourceLocation item : items) {
                if (jsonStr.contains(item.toString())) {
                    found.put(tag, item);
                }
            }
        });

        if (found.isEmpty()) return;

        String type = Optional.ofNullable(json.get("type")).map(JsonElement::getAsString).orElse("");
        sb
                .append("Recipe ")
                .append(recipe)
                .append(" (")
                .append(type)
                .append(") contains potentially hiding items:")
                .append("\n")
                .append("Json: ")
                .append(jsonStr)
                .append("\n")
                .append("Items: ")
                .append("\n");
        found.asMap().forEach((tag, items) -> {
            for (ResourceLocation item : items) {
                sb.append("\t").append(item).append(" (#").append(tag.location()).append(")").append("\n");
            }
        });

        sb.append("\n");
    }


    public static Multimap<UnifyTag<Item>, ResourceLocation> getHidingIds(AlmostUnifiedRuntime runtime) {
        ReplacementMap repMap = runtime.getReplacementMap().orElse(null);
        var tagMap = runtime.getFilteredTagMap().orElse(null);

        Multimap<UnifyTag<Item>, ResourceLocation> hidings = HashMultimap.create();
        if (repMap == null || tagMap == null) return hidings;

        for (var unifyTag : tagMap.getTags()) {
            var itemsByTag = tagMap.getEntriesByTag(unifyTag);

            if (Utils.allSameNamespace(itemsByTag)) continue;

            ResourceLocation kingItem = repMap.getPreferredItemForTag(unifyTag, $ -> true);
            if (kingItem == null) continue;

            Set<ResourceLocation> r = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                if (!item.equals(kingItem)) {
                    r.add(item);
                }
            }

            hidings.putAll(unifyTag, r);
        }

        return hidings;
    }
}
