package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;

import java.util.List;


public class Defaults {
    public static final List<String> STONE_STRATA = List.of("stone",
            "nether",
            "deepslate",
            "granite",
            "diorite",
            "andesite");

    @SuppressWarnings("SpellCheckingInspection")
    public static final List<String> MOD_PRIORITIES = List.of(
            "minecraft",
            "kubejs",
            "crafttweaker",
            "create",
            "thermal",
            "immersiveengineering",
            "mekanism");
    @SuppressWarnings("SpellCheckingInspection")
    public static final List<String> MATERIALS = List.of("aeternium",
            "aluminum",
            "amber",
            "apatite",
            "bitumen",
            "brass",
            "bronze",
            "charcoal",
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
            "fluorite",
            "gold",
            "invar",
            "iron",
            "lapis",
            "lead",
            "lumium",
            "nickel",
            "obsidian",
            "osmium",
            "potassium_nitrate",
            "signalum",
            "silver",
            "steel",
            "sulfur",
            "tin",
            "uranium",
            "zinc"

    );
    public static final List<String> TYPES = List.of(
            "nuggets",
            "dusts",
            "gears",
            "gems",
            "ingots",
            "raw_materials",
            "ores",
            "plates",
            "rods",
            "storage_blocks");

    public static final List<String> TAGS = getDefaultPatterns();

    private static List<String> getDefaultPatterns() {
        return switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
            case Forge -> List.of(
                    "forge:nuggets/{material}",
                    "forge:dusts/{material}",
                    "forge:gears/{material}",
                    "forge:gems/{material}",
                    "forge:ingots/{material}",
                    "forge:raw_materials/{material}",
                    "forge:ores/{material}",
                    "forge:plates/{material}",
                    "forge:rods/{material}",
                    "forge:storage_blocks/{material}"
            );
            case Fabric -> List.of(
                    "c:nuggets/{material}",
                    "c:dusts/{material}",
                    "c:gears/{material}",
                    "c:gems/{material}",
                    "c:ingots/{material}",
                    "c:raw_materials/{material}",
                    "c:ores/{material}",
                    "c:plates/{material}",
                    "c:rods/{material}",
                    "c:storage_blocks/{material}",
                    // Modder's just can't decide
                    "c:{material}_nuggets",
                    "c:{material}_dusts",
                    "c:{material}_gears",
                    "c:{material}_gems",
                    "c:{material}_ingots",
                    "c:{material}_raw_materials",
                    "c:{material}_ores",
                    "c:{material}_plates",
                    "c:{material}_rods",
                    "c:{material}_storage_blocks"
            );
        };
    }
}
