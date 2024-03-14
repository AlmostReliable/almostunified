package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.compat.HideHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

public final class UncoveredRecipesDumper {

    private UncoveredRecipesDumper() {}

    public static void write(Map<ResourceLocation, JsonElement> recipes) {
        var hidingMap = HideHelper.createHidingMap();
        FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getLogPath(), "uncovered_recipes.txt", sb -> {
            recipes.forEach((recipeId, json) -> {
                if (json instanceof JsonObject obj) {
                    write(recipeId, obj, hidingMap, sb);
                }
            });
        });
    }

    private static void write(ResourceLocation recipeId, JsonObject json, Multimap<UnifyTag<Item>, ResourceLocation> hidingMap, StringBuilder sb) {
        String jsonStr = json.toString();
        Multimap<UnifyTag<Item>, ResourceLocation> foundEntries = HashMultimap.create();
        hidingMap.asMap().forEach((tag, items) -> {
            for (ResourceLocation item : items) {
                if (jsonStr.contains(item.toString())) {
                    foundEntries.put(tag, item);
                }
            }
        });

        if (foundEntries.isEmpty()) return;

        String type = Optional.ofNullable(json.get("type")).map(JsonElement::getAsString).orElse("");
        sb
                .append("Id: ").append(recipeId).append("\n")
                .append("Type: ").append(type).append("\n")
                .append("Json: ").append(jsonStr).append("\n")
                .append("Items: ").append("\n");
        foundEntries.asMap().forEach((tag, items) -> {
            for (ResourceLocation item : items) {
                sb.append("\t").append(item).append(" (#").append(tag.location()).append(")").append("\n");
            }
        });

        sb.append("\n");
    }
}
