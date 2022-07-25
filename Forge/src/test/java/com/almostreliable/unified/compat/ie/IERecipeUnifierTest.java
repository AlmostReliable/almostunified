package com.almostreliable.unified.compat.ie;

import com.almostreliable.unified.TestUtils;
import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.IERecipeUnifier;
import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.utils.JsonQuery;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IERecipeUnifierTest {
    public final Gson gson = new Gson();

    private final ResourceLocation defaultRecipeId = new ResourceLocation("default_test_recipe");
    private final String simpleAlloyRecipe = """
            {
                "type": "immersiveengineering:alloy",
                "time": 200,
                "result": { "count": 2, "base_ingredient": { "tag": "forge:ingots/electrum" } },
                "input0": { "tag": "forge:ingots/gold" },
                "input1": { "tag": "forge:ingots/silver" }
            }
            """;

    @Test
    public void notMatching() {
        RecipeTransformer transformer = TestUtils.basicTransformer(f -> f.registerForMod(ModConstants.IE,
                new IERecipeUnifier()));
        JsonObject alloy = gson.fromJson(simpleAlloyRecipe, JsonObject.class);
        RecipeLink recipe = new RecipeLink(defaultRecipeId, alloy);
        transformer.unifyRecipe(recipe);
        assertFalse(recipe.isUnified(), "Nothing to transform, so it should be false");
    }

    @Test
    public void resultTagMatches() {
        RecipeTransformer transformer = TestUtils.basicTransformer(f -> f.registerForMod(ModConstants.IE,
                new IERecipeUnifier()));
        JsonObject alloy = gson.fromJson(simpleAlloyRecipe, JsonObject.class);
        alloy
                .getAsJsonObject("result")
                .getAsJsonObject("base_ingredient")
                .addProperty("tag", TestUtils.BRONZE_ORES_TAG.location().toString());
        RecipeLink recipe = new RecipeLink(defaultRecipeId, alloy);
        transformer.unifyRecipe(recipe);

        assertNotEquals(recipe.getUnified(), alloy, "Result should be different");
        assertNotNull(recipe.getUnified(), "Result should not be null");
        assertNull(JsonQuery.of(recipe.getUnified(), "result/base_ingredient/tag"), "Tag key should be removed");
        assertEquals(JsonQuery.of(recipe.getUnified(), "result/base_ingredient/item").asString(),
                TestUtils.mod1RL("bronze_ore").toString(),
                "Result should be bronze_ore");
    }

    @Test
    public void resultItemMatches() {
        RecipeTransformer transformer = TestUtils.basicTransformer(f -> f.registerForMod(ModConstants.IE,
                new IERecipeUnifier()));
        JsonObject alloy = gson.fromJson(simpleAlloyRecipe, JsonObject.class);
        alloy.getAsJsonObject("result").getAsJsonObject("base_ingredient").remove("tag");
        alloy
                .getAsJsonObject("result")
                .getAsJsonObject("base_ingredient")
                .addProperty("item", TestUtils.mod3RL("bronze_ore").toString());
        RecipeLink recipe = new RecipeLink(defaultRecipeId, alloy);
        transformer.unifyRecipe(recipe);

        assertNotEquals(recipe.getUnified(), alloy, "Result should be different");
        assertNotNull(recipe.getUnified(), "Result should not be null");
        assertEquals(JsonQuery.of(recipe.getUnified(), ("result/base_ingredient/item")).asString(),
                TestUtils.mod1RL("bronze_ore").toString(),
                "Transformer should replace bronze_ore from mod3 with bronze_ore from mod1");
    }

    @Test
    public void inputAlloyItemMatches() {
        RecipeTransformer transformer = TestUtils.basicTransformer(f -> f.registerForMod(ModConstants.IE,
                new IERecipeUnifier()));
        JsonObject alloy = gson.fromJson(simpleAlloyRecipe, JsonObject.class);
        alloy.getAsJsonObject("result").getAsJsonObject("base_ingredient").remove("tag");
        alloy
                .getAsJsonObject("result")
                .getAsJsonObject("base_ingredient")
                .addProperty("item", TestUtils.mod3RL("bronze_ore").toString());
        RecipeLink recipe = new RecipeLink(defaultRecipeId, alloy);
        transformer.unifyRecipe(recipe);

        assertNotEquals(recipe.getUnified(), alloy, "Result should be different");
        assertNotNull(recipe.getUnified(), "Result should not be null");
        assertEquals(JsonQuery.of(recipe.getUnified(), ("result/base_ingredient/item")).asString(),
                TestUtils.mod1RL("bronze_ore").toString(),
                "Transformer should replace bronze_ore from mod3 with bronze_ore from mod1");
    }
}
