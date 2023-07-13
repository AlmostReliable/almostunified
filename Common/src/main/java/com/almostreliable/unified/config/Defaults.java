package com.almostreliable.unified.config;

import com.almostreliable.unified.Platform;
import com.almostreliable.unified.utils.JsonCompare;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;

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

    public static List<String> getModPriorities(Platform platform) {
        return switch (platform) {
            case FORGE -> List.of(
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

    public static List<String> getTags(Platform platform) {
        return switch (platform) {
            case FORGE -> List.of(
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

    public static List<String> getIgnoredRecipeTypes(Platform platform) {
        return switch (platform) {
            default -> List.of("cucumber:shaped_tag");
        };
    }

    public static JsonCompare.CompareSettings getDefaultDuplicateRules(Platform platform) {
        JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
        result.ignoreField(switch (platform) {
            case FORGE -> "conditions";
            case FABRIC -> "fabric:load_conditions";
        });
        result.ignoreField("group");
        result.addRule("cookingtime", new JsonCompare.HigherRule());
        result.addRule("energy", new JsonCompare.HigherRule());
        result.addRule("experience", new JsonCompare.HigherRule());
        return result;
    }

    public static LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> getDefaultDuplicateOverrides(Platform platform) {
        JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
        result.ignoreField(switch (platform) {
            case FORGE -> "conditions";
            case FABRIC -> "fabric:load_conditions";
        });
        result.ignoreField("group");
        result.ignoreField("pattern");
        result.ignoreField("key");
        LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> resultMap = new LinkedHashMap<>();
        resultMap.put(new ResourceLocation("minecraft", "crafting_shaped"), result);
        return resultMap;
    }
}
