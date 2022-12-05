package com.almostreliable.unified.util;

import com.almostreliable.unified.Platform;
import com.almostreliable.unified.TestUtils;
import com.almostreliable.unified.config.Defaults;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonCompareTest {

    private static final JsonCompare.CompareSettings DEFAULT_COMPARE_SETTINGS = getDefaultCompareSettings();
    private static final JsonCompare.CompareSettings DEFAULT_SHAPED_COMPARE_SETTINGS = getDefaultShapedCompareSettings();
    private static final String RECIPE = """
            {
              "type": "minecraft:smelting",
              "group": "coal",
              "ingredient": {
                "item": "minecraft:coal_ore"
              },
              "result": "minecraft:coal",
              "experience": 0.1,
              "cookingtime": 200
            }
            """;

    private static JsonCompare.CompareSettings getDefaultCompareSettings() {
        return Defaults.getDefaultDuplicateRules(Platform.FORGE);
    }

    private static JsonCompare.CompareSettings getDefaultShapedCompareSettings() {
        return Defaults.getDefaultDuplicateOverrides(Platform.FORGE).get(new ResourceLocation("crafting_shaped"));
    }

    @Test
    public void simpleCompareFirst() {
        JsonObject first = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.2));
        JsonObject second = TestUtils.json(RECIPE); // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.LowerRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(second, result);
    }

    @Test
    public void simpleCompareSecond() {
        JsonObject first = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.05));
        JsonObject second = TestUtils.json(RECIPE); // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.LowerRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(first, result);
    }

    @Test
    public void compareHigherWins() {
        JsonObject first = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.05));
        JsonObject second = TestUtils.json(RECIPE); // 0.1 experience  // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.HigherRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(second, result);
    }

    @Test
    public void compareMulti() {
        JsonObject a = TestUtils.json(RECIPE, j -> {
            j.addProperty("experience", 0.1);
            j.addProperty("cookingtime", 100);
        });
        JsonObject b = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.1));
        JsonObject c = TestUtils.json(RECIPE, j -> {
            j.addProperty("experience", 0.1);
            j.addProperty("cookingtime", 50);
        });
        JsonObject d = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.2));
        JsonObject e = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.2));
        JsonObject f = TestUtils.json(RECIPE, j -> j.addProperty("experience", 0.1));
        JsonObject g = TestUtils.json(RECIPE, j -> {
            j.addProperty("experience", 0.2);
            j.addProperty("cookingtime", 100);
        });

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.HigherRule());
        rules.put("cookingtime", new JsonCompare.LowerRule());

        List<JsonObject> list = Arrays.asList(a, b, c, d, e, f, g);
        list.sort((first, second) -> JsonCompare.compare(first, second, rules));
        List<JsonObject> results = Arrays.asList(g, d, e, c, a, b, f);
        for (int i = 0; i < list.size(); i++) {
            assertEquals(results.get(i), list.get(i), "Failed at index " + i);
        }
    }

    @Test
    public void simpleMatch() {
        JsonObject first = TestUtils.json(RECIPE);
        JsonObject second = TestUtils.json(RECIPE);
        boolean matches = JsonCompare.matches(first, second, DEFAULT_COMPARE_SETTINGS);
        assertTrue(matches);
    }

    @Test
    public void noMatch() {
        JsonObject first = TestUtils.json(RECIPE, j -> j.addProperty("experience", 100));
        JsonObject second = TestUtils.json(RECIPE);
        boolean matches = JsonCompare.matches(first, second, new JsonCompare.CompareSettings());
        assertFalse(matches);
    }

    @Test
    public void matchBecauseIgnore() {
        JsonObject first = TestUtils.json(RECIPE, j -> j.addProperty("experience", 100));
        JsonObject second = TestUtils.json(RECIPE);
        var compareSettings = getDefaultCompareSettings();
        compareSettings.ignoreField("experience");
        boolean matches = JsonCompare.matches(first, second, compareSettings);
        assertTrue(matches);
    }

    @Test
    public void shapedNoMatch() {
        String recipe1 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "ici",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    },
                    "k": {
                      "item": "minecraft:carrot"
                    }
                  },
                  "result": "minecraft:iron_ingot"
                }
                """;
        String recipe2 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "ici",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    },
                    "k": {
                      "item": "minecraft:pumpkin"
                    }
                  },
                  "result": "minecraft:iron_ingot"
                }
                """;

        JsonObject first = TestUtils.json(recipe1);
        JsonObject second = TestUtils.json(recipe2);
        JsonObject result = JsonCompare.compareShaped(first, second, DEFAULT_SHAPED_COMPARE_SETTINGS);
        assertNull(result);
    }

    @Test
    public void shapedMatch() {
        String recipe1 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "iii",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    }
                  },
                  "result": "minecraft:iron_ingot"
                }
                """;
        String recipe2 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "iki",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    },
                    "k": {
                      "tag": "forge:raw_materials/iron"
                    }
                  },
                  "result": "minecraft:iron_ingot"
                }
                """;

        JsonObject first = TestUtils.json(recipe1);
        JsonObject second = TestUtils.json(recipe2);
        JsonObject result = JsonCompare.compareShaped(first, second, DEFAULT_SHAPED_COMPARE_SETTINGS);
        assertEquals(first, result);
    }

    @Test
    public void sanitizeImplicitCount() {
        String recipe1 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "iii",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    }
                  },
                  "result": "minecraft:iron_ingot"
                }
                """;
        String recipe2 = """
                {
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "iii",
                    "iii",
                    "iii"
                  ],
                  "key": {
                    "i": {
                      "tag": "forge:raw_materials/iron"
                    }
                  },
                  "result": {
                    "item": "minecraft:iron_ingot",
                    "count": 1
                  }
                }
                """;

        JsonObject first = TestUtils.json(recipe1);
        JsonObject second = TestUtils.json(recipe2);
        var compareSettings = getDefaultShapedCompareSettings();
        compareSettings.setShouldSanitize(true);
        JsonObject result = JsonCompare.compareShaped(first, second, compareSettings);
        assertEquals(first, result);
    }

    @Test
    public void sanitizeImplicitCountNested() {
        String recipe1 = """
                {
                  "type": "create:crushing",
                  "ingredients": [{ "tag": "forge:raw_materials/lead" }],
                  "processingTime": 400,
                  "results": [
                    { "item": "emendatusenigmatica:crushed_lead_ore" },
                    { "chance": 0.75, "item": "create:experience_nugget" }
                  ]
                }
                """;
        String recipe2 = """
                {
                  "type": "create:crushing",
                  "ingredients": [{ "tag": "forge:raw_materials/lead" }],
                  "processingTime": 400,
                  "results": [
                    { "count": 1, "item": "emendatusenigmatica:crushed_lead_ore" },
                    { "chance": 0.75, "count": 1, "item": "create:experience_nugget" }
                  ]
                }
                """;

        JsonObject first = TestUtils.json(recipe1);
        JsonObject second = TestUtils.json(recipe2);
        var compareSettings = getDefaultCompareSettings();
        compareSettings.setShouldSanitize(true);
        boolean result = JsonCompare.matches(first, second, compareSettings);
        assertTrue(result);
    }
}
