package com.almostreliable.unified.recipe;

import com.almostreliable.unified.TestUtils;
import com.almostreliable.unified.utils.JsonQuery;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMapTests;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RecipeContextImplTest {
    private static String testJson = """
            {
                "input": [
                    {
                        "tag": "tag_replace_me"
                    },
                    [
                        {
                            "item": "item_replace_me"
                        },
                        {
                            "item": "item_replace_me"
                        },
                        [
                            {
                                "item": "item_replace_me"
                            },
                            {
                                "tag": "tag_replace_me"
                            }
                        ]
                    ]
                ],
                "results": {
                    "item": "item_replace_me"
                }
            }""";

    public static String mekaTest = """
            {
                "type": "mekanism:combining",
                "mainInput": { "amount": 8, "ingredient": { "tag": "forge:raw_materials/tin" } },
                "extraInput": { "ingredient": { "tag": "forge:cobblestone/normal" } },
                "output": { "item": "mekanism:tin_ore" }
            }
            """;

    @Test
    public void depthReplace_MekaTest() {
        JsonObject json = new Gson().fromJson(mekaTest, JsonObject.class);
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(),
                TestUtils.TEST_ALLOWED_TAGS,
                TestUtils.TEST_MOD_PRIORITIES);
        RecipeContextImpl context = new RecipeContextImpl(new ResourceLocation("test"), json, map);
        JsonElement result = context.replaceResultOld(json.getAsJsonObject("output"));
        assertNull(result);
    }

    @Test
    public void depthReplace_NothingReplaced() {
        JsonObject json = new Gson().fromJson(testJson, JsonObject.class);
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(),
                TestUtils.TEST_ALLOWED_TAGS,
                TestUtils.TEST_MOD_PRIORITIES);
        RecipeContextImpl context = new RecipeContextImpl(new ResourceLocation("test"), json, map);
        JsonElement result = context.depthReplace(json,
                "not_existing",
                "item",
                primitive -> new JsonPrimitive("dont_find_this"));
        assertNull(result);
    }

    @Test
    public void depthReplace_Items() {
        JsonObject json = new Gson().fromJson(testJson, JsonObject.class);
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(),
                TestUtils.TEST_ALLOWED_TAGS,
                TestUtils.TEST_MOD_PRIORITIES);
        RecipeContextImpl context = new RecipeContextImpl(new ResourceLocation("test"), json, map);
        JsonElement result = context.depthReplace(json,
                "item",
                "item",
                primitive -> new JsonPrimitive("item_was_replaced"));
        assertNotNull(result);
        assertEquals(JsonQuery.of(result, "input/0/item").asString(), "item_was_replaced");
        assertEquals(JsonQuery.of(result, "input/1/1/item").asString(), "item_was_replaced");
        assertEquals(JsonQuery.of(result, "input/1/2/0/item").asString(), "item_was_replaced");
        assertEquals(JsonQuery.of(result, "result/item").asString(), "item_was_replaced");
    }
}
