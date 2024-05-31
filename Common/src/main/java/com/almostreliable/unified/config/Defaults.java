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

@SuppressWarnings("SpellCheckingInspection")
public final class Defaults {

    public static final List<String> STONE_STRATA = List.of(
            "stone",
            "nether",
            "deepslate",
            "granite",
            "diorite",
            "andesite"
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
                "charcoal",
                "chrome",
                "cinnabar",
                "coal",
                "coal_coke",
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

    public static final List<String> MATERIALS = List.of(
            "aeternium",
            "aluminum",
            "amber",
            "apatite",
            "bitumen",
            "brass",
            "bronze",
            "charcoal",
            "chrome",
            "cinnabar",
            "coal",
            "coal_coke",
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
    );

    private Defaults() {}

    public static List<String> getModPriorities(AlmostUnifiedPlatform.Platform platform) {
        return switch (platform) {
            case NEO_FORGE -> List.of(
                    "minecraft",
                    "kubejs",
                    "crafttweaker",
                    "create",
                    "thermal",
                    "immersiveengineering",
                    "mekanism"
            );
            case FABRIC -> List.of(
                    "minecraft",
                    "kubejs",
                    "crafttweaker",
                    "create",
                    "techreborn",
                    "modern_industrialization",
                    "indrev"
            );
        };
    }

    public static List<String> getTags(AlmostUnifiedPlatform.Platform platform) {
        return switch (platform) {
            case NEO_FORGE -> List.of(
                    "forge:nuggets/{material}",
                    "forge:dusts/{material}",
                    "forge:gears/{material}",
                    "forge:gems/{material}",
                    "forge:ingots/{material}",
                    "forge:raw_materials/{material}",
                    "forge:ores/{material}",
                    "forge:plates/{material}",
                    "forge:rods/{material}",
                    "forge:wires/{material}",
                    "forge:storage_blocks/{material}",
                    "forge:storage_blocks/raw_{material}"
            );
            case FABRIC -> List.of(
                    "c:{material}_nuggets",
                    "c:{material}_dusts",
                    "c:{material}_gears",
                    "c:{material}_gems",
                    "c:{material}_ingots",
                    "c:{material}_raw_materials",
                    "c:{material}_ores",
                    "c:{material}_plates",
                    "c:{material}_rods",
                    "c:{material}_blocks",
                    "c:{material}_wires",
                    "c:{material}_storage_blocks",
                    "c:raw_{material}_ores",
                    "c:raw_{material}_blocks",
                    "c:raw_{material}_storage_blocks"
            );
        };
    }

    public static List<String> getIgnoredRecipeTypes(AlmostUnifiedPlatform.Platform platform) {
        return List.of("cucumber:shaped_tag");
    }

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
        resultMap.put(new ResourceLocation("minecraft", "crafting_shaped"), result);
        return resultMap;
    }

    private static JsonCompare.CompareSettings getDefaultCompareSettings(AlmostUnifiedPlatform.Platform platform) {
        JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
        result.ignoreField(switch (platform) {
            case NEO_FORGE -> "conditions";
            case FABRIC -> "fabric:load_conditions";
        });
        result.ignoreField("group");
        result.ignoreField("category");
        result.ignoreField("show_notification");
        return result;
    }
}
