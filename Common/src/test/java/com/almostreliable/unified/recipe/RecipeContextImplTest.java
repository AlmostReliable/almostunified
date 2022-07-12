package com.almostreliable.unified.recipe;

import com.almostreliable.unified.TestUtils;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMapTests;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RecipeContextImplTest {
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
                TestUtils.TEST_MOD_PRIORITIES);
//        RecipeContextImpl context = new RecipeContextImpl(new ResourceLocation("test"), json, map);
//        JsonElement result = context.createResultReplacement(json.getAsJsonObject("output"));
//        assertNull(result);
    }
}
