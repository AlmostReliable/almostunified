package com.almostreliable.unified.util;

import com.almostreliable.unified.TestUtils;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonCompareTest {
    public static String recipe = """
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

    @Test
    public void simpleCompareFirst() {
        JsonObject first = TestUtils.json(recipe, j -> j.addProperty("experience", 0.2));
        JsonObject second = TestUtils.json(recipe); // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.LowerRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(second, result);
    }

    @Test
    public void simpleCompareSecond() {
        JsonObject first = TestUtils.json(recipe, j -> j.addProperty("experience", 0.05));
        JsonObject second = TestUtils.json(recipe); // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.LowerRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(first, result);
    }

    @Test
    public void compareHigherWins() {
        JsonObject first = TestUtils.json(recipe, j -> j.addProperty("experience", 0.05));
        JsonObject second = TestUtils.json(recipe); // 0.1 experience  // 0.1 experience

        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.HigherRule());
        JsonObject result = JsonCompare.compare(rules, first, second);
        assertEquals(second, result);
    }

    @Test
    public void compareMulti() {
        JsonObject a = TestUtils.json(recipe, j -> {
            j.addProperty("experience", 0.1);
            j.addProperty("cookingtime", 100);
        });
        JsonObject b = TestUtils.json(recipe, j -> j.addProperty("experience", 0.1));
        JsonObject c = TestUtils.json(recipe, j -> {
            j.addProperty("experience", 0.1);
            j.addProperty("cookingtime", 50);
        });
        JsonObject d = TestUtils.json(recipe, j -> j.addProperty("experience", 0.2));
        JsonObject e = TestUtils.json(recipe, j -> j.addProperty("experience", 0.2));
        JsonObject f = TestUtils.json(recipe, j -> j.addProperty("experience", 0.1));
        JsonObject g = TestUtils.json(recipe, j -> {
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
        JsonObject first = TestUtils.json(recipe);
        JsonObject second = TestUtils.json(recipe);
        boolean matches = JsonCompare.matches(first, second, List.of());
        assertTrue(matches);
    }

    @Test
    public void noMatch() {
        JsonObject first = TestUtils.json(recipe, j -> j.addProperty("experience", 100));
        JsonObject second = TestUtils.json(recipe);
        boolean matches = JsonCompare.matches(first, second, List.of());
        assertFalse(matches);
    }

    @Test
    public void matchBecauseIgnore() {
        JsonObject first = TestUtils.json(recipe, j -> j.addProperty("experience", 100));
        JsonObject second = TestUtils.json(recipe);
        boolean matches = JsonCompare.matches(first, second, List.of("experience"));
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
        JsonObject result = JsonCompare.compareShaped(first, second, List.of("pattern", "key"));
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
        JsonObject result = JsonCompare.compareShaped(first, second, List.of("pattern", "key"));
        assertEquals(first, result);
    }
}
