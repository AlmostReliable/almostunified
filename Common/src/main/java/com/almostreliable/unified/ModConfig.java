package com.almostreliable.unified;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

public class ModConfig {

    private static final String UNIFICATION_MOD_PRIORITIES = "unification.mod_priorities";
    private static final String UNIFICATION_VARIABLES = "unification.variables";
    private static final String UNIFICATION_VARIABLES_MATERIALS = "unification.variables.materials";
    private static final String UNIFICATION_VARIABLES_TYPES = "unification.variables.types";
    private static final String UNIFICATION_PATTERN = "unification.pattern";
    @SuppressWarnings("SpellCheckingInspection")
    private static final List<String> DEFAULT_MOD_PRIORITIES = List.of(
            "kubejs",
            "crafttweaker",
            "minecraft",
            "create",
            "thermal",
            "immersiveengineering",
            "mekanism",
            "pneumaticcraft",
            "refinedstorage",
            "bloodmagic",
            "undergarden",
            "byg",
            "atum",
            "betterendforge",
            "chipped",
            "chisel",
            "tconstruct",
            "mysticalagriculture");
    @SuppressWarnings("SpellCheckingInspection")
    private static final List<String> DEFAULT_METALS = List.of("aeternium",
            "aluminum",
            "amber",
            "apatite",
            "arcane",
            "bitumen",
            "brass",
            "bronze",
            "charcoal",
            "cinnabar",
            "coal",
            "coal_coke",
            "cobalt",
            "compressed_iron",
            "constantan",
            "copper",
            "diamond",
            "dimensional",
            "electrum",
            "elementium",
            "emerald",
            "ender",
            "enderium",
            "fluorite",
            "glowstone",
            "gold",
            "hepatizon",
            "infused_iron",
            "invar",
            "iron",
            "lapis",
            "lead",
            "lumium",
            "mana",
            "manyullyn",
            "nickel",
            "obsidian",
            "osmium",
            "potassium_nitrate",
            "quartz",
            "rose_gold",
            "signalum",
            "silver",
            "steel",
            "sulfur",
            "thallasium",
            "tin",
            "tinkers_bronze",
            "uranium",
            "zinc"

    );
    private static final List<String> DEFAULT_TYPES = List.of("nuggets",
            "dusts",
            "gears",
            "gems",
            "ingots",
            "ores",
            "plates",
            "rods",
            "storage_blocks");
    private static final List<String> DEFAULT_UNIFIES = getDefaultPatterns();
    private final String name;

    @Nullable private FileConfig currentConfig;
    private final ConfigSpec spec;

    public ModConfig(String name) {
        this.name = name;

        spec = new ConfigSpec();
        spec.defineList(UNIFICATION_MOD_PRIORITIES, () -> DEFAULT_MOD_PRIORITIES, o -> o instanceof String);
        spec.defineList(UNIFICATION_VARIABLES_MATERIALS, () -> DEFAULT_METALS, o -> o instanceof String);
        spec.defineList(UNIFICATION_VARIABLES_TYPES, () -> DEFAULT_TYPES, o -> o instanceof String);
        spec.defineList(UNIFICATION_PATTERN, () -> DEFAULT_UNIFIES, o -> o instanceof String);
    }

    private static List<String> getDefaultPatterns() {
        if (AlmostUnifiedPlatform.INSTANCE.getPlatformName().equals("Forge")) {
            return List.of("forge:{types}/{materials}");
        } else {
            return List.of("c:{materials}_{types}");
        }
    }

    protected FileConfig createConfig() {
        CommentedFileConfig config = CommentedFileConfig.ofConcurrent(AlmostUnifiedPlatform.INSTANCE.getConfigPath().resolve(name + ".toml"));
        config.setComment(UNIFICATION_MOD_PRIORITIES, "Mod priorities for unification's");
        config.setComment(UNIFICATION_VARIABLES,
                "Custom variables can be defined here. Look at the example for more info.\n" +
                "They will be used in `resources.unify` to determine which tags or items should be unified.");
        config.setComment(UNIFICATION_VARIABLES_MATERIALS, "List of materials to unify");
        config.setComment(UNIFICATION_VARIABLES_TYPES, "List of types to unify");
        config.setComment(UNIFICATION_PATTERN, """
                Define how the pattern for unification's should work.
                 - Using `{variable_names}`, will replace the values from `resources.variable_names` into the pattern.
                 - If the pattern starts with `!`, it will be ignored.""");

        return config;
    }

    public void load() {
        currentConfig = createConfig();
        currentConfig.load();

        if (!spec.isCorrect(currentConfig)) {
            AlmostUnified.LOG.warn("Config has missing or invalid values - correcting now");
            spec.correct(currentConfig);
            currentConfig.save();
        }

        currentConfig.close();
    }

    public List<String> getModPriorities() {
        if(currentConfig == null) {
            throw new IllegalStateException("Config is not loaded");
        }
        return currentConfig.get(UNIFICATION_MOD_PRIORITIES);
    }

    public List<ResourceLocation> getAllowedTags() {
        if(currentConfig == null) {
            throw new IllegalStateException("Config is not loaded");
        }

        Multimap<String, String> variables = compileVariables();
        List<ResourceLocation> collectedPattern = new ArrayList<>();
        Collection<String> patterns = currentConfig.get(UNIFICATION_PATTERN);

        for (String pattern : patterns) {
            Collection<String> compiledPattern = compilePattern(pattern, variables);
            if (pattern.startsWith("!")) {
                collectedPattern.removeIf(p -> compiledPattern.contains(p.toString()));
            } else {
                for (String s : compiledPattern) {
                    ResourceLocation rl = ResourceLocation.tryParse(s);
                    if (rl == null) {
                        AlmostUnified.LOG.warn("Invalid pattern: " + s);
                    } else {
                        collectedPattern.add(rl);
                    }
                }
            }
        }

        return Collections.unmodifiableList(collectedPattern);
    }

    private Collection<String> compilePattern(String pattern, Multimap<String, String> variables) {
        Set<String> result = new HashSet<>();

        Stack<String> stack = new Stack<>();
        stack.push(pattern);

        while (!stack.isEmpty()) {
            String p = stack.pop();

            int firstBracket = p.indexOf('{');
            int secondBracket = p.indexOf('}');
            if (firstBracket == -1 || secondBracket == -1) {
                result.add(p);
                continue;
            }

            if (firstBracket > secondBracket) {
                AlmostUnified.LOG.warn("Invalid pattern: {}, will be skipped", p);
                return new HashSet<>();
            }

            String toReplace = p.substring(firstBracket + 1, secondBracket);
            if (!variables.containsKey(toReplace)) {
                AlmostUnified.LOG.warn("Variable {} is not defined in config, pattern will be skipped", toReplace);
                return new HashSet<>();
            }

            for (String variable : variables.get(toReplace)) {
                String newPattern = p.replace("{" + toReplace + "}", variable);
                stack.push(newPattern);
            }
        }

        return result;
    }

    private Multimap<String, String> compileVariables() {
        if(currentConfig == null) {
            throw new IllegalStateException("Config is not loaded");
        }

        Multimap<String, String> computedVariables = HashMultimap.create();

        if (!currentConfig.contains(UNIFICATION_VARIABLES)) {
            return computedVariables;
        }

        if (currentConfig.get(UNIFICATION_VARIABLES) instanceof Config variables) {
            Map<String, Object> values = variables.valueMap();
            values.forEach((k, v) -> {
                if (v instanceof Collection<?> asCollection) {
                    computedVariables.putAll(k, asCollection.stream().map(Object::toString).toList());
                }
            });
        }

        return computedVariables;
    }
}
