package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("SpellCheckingInspection")
public final class Defaults {

    private Defaults() {}

    public static final List<String> STONE_STRATAS = List.of(
            "stone",
            "andesite",
            "deepslate",
            "diorite",
            "granite",
            "nether"
    );

    public static final Map<String, Collection<String>> PLACEHOLDERS = Util.make(() -> {
        ImmutableMap.Builder<String, Collection<String>> builder = ImmutableMap.builder();

        builder.put("material", List.of(
                "aeternium",
                "aluminum",
                "amber",
                "apatite",
                "bitumen",
                "brass",
                "bronze",
                "chrome",
                "cinnabar",
                "cobalt",
                "constantan",
                "copper",
                "diamond",
                "electrum",
                "elementium",
                "emerald",
                "enderium",
                "fluorite",
                "gold",
                "graphite",
                "invar",
                "iridium",
                "iron",
                "lapis",
                "lead",
                "lumium",
                "mithril",
                "netherite",
                "nickel",
                "obsidian",
                "osmium",
                "peridot",
                "platinum",
                "potassium_nitrate",
                "ruby",
                "sapphire",
                "signalum",
                "silver",
                "steel",
                "sulfur",
                "tin",
                "tungsten",
                "uranium",
                "zinc"
        ));

        return builder.build();
    });

    public static final List<String> MOD_PRIORITIES = Stream.of(
            "minecraft",
            "kubejs",
            "crafttweaker",
            "create",
            "thermal",
            "immersiveengineering",
            "mekanism",
            "techreborn",
            "modern_industrialization",
            "indrev"
    ).filter(AlmostUnifiedPlatform.INSTANCE::isModLoaded).toList();

    public static final List<String> TAGS = List.of(
            "c:dusts/{material}",
            "c:gears/{material}",
            "c:gems/{material}",
            "c:ingots/{material}",
            "c:nuggets/{material}",
            "c:ores/{material}",
            "c:plates/{material}",
            "c:raw_blocks/{material}",
            "c:raw_materials/{material}",
            "c:rods/{material}",
            "c:storage_blocks/{material}",
            "c:wires/{material}"
    );

    public static final List<String> IGNORED_RECIPE_TYPES = List.of("cucumber:shaped_tag");

    public static JsonCompare.CompareSettings getDefaultDuplicateRules(AlmostUnifiedPlatform.Platform platform) {
        JsonCompare.CompareSettings result = getDefaultCompareSettings(platform);
        result.addRule("cookingtime", new JsonCompare.HigherRule());
        result.addRule("energy", new JsonCompare.HigherRule());
        result.addRule("experience", new JsonCompare.HigherRule());
        return result;
    }

    public static LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> getDefaultDuplicateOverrides(AlmostUnifiedPlatform.Platform platform) {
        JsonCompare.CompareSettings result = getDefaultCompareSettings(platform);
        result.ignoreField("pattern");
        result.ignoreField("key");

        LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> resultMap = new LinkedHashMap<>();
        resultMap.put(ResourceLocation.withDefaultNamespace("crafting_shaped"), result);
        return resultMap;
    }

    private static JsonCompare.CompareSettings getDefaultCompareSettings(AlmostUnifiedPlatform.Platform platform) {
        JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
        result.ignoreField(switch (platform) {
            case NEO_FORGE -> "neoforge:conditions";
            case FABRIC -> "fabric:load_conditions";
        });
        result.ignoreField("group");
        result.ignoreField("category");
        result.ignoreField("show_notification");
        return result;
    }
}
