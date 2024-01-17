package testmod.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.AdAstraRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertNoUnify;
import static testmod.TestUtils.assertUnify;

public class AdAstraRecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new AdAstraRecipeUnifier();

    @SimpleGameTest
    public void test() {
        assertUnify(UNIFIER, """
                {
                  "type": "ad_astra:compressing",
                  "cookingtime": 800,
                  "energy": 20,
                  "ingredient": {
                    "item": "minecraft:test_item"
                  },
                  "result": {
                    "count": 9,
                    "id": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "ad_astra:compressing",
                  "cookingtime": 800,
                  "energy": 20,
                  "ingredient": {
                    "tag": "testmod:test_tag"
                  },
                  "result": {
                    "count": 9,
                    "id": "testmod:test_item"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testNot() {
        assertNoUnify(UNIFIER, """
                {
                  "type": "ad_astra:compressing",
                  "cookingtime": 800,
                  "energy": 20,
                  "ingredient": {
                    "tag": "ad_astra:calorite_blocks"
                  },
                  "result": {
                    "count": 9,
                    "id": "ad_astra:calorite_plate"
                  }
                }
                """);
    }
}
