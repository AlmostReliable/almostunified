package testmod.neoforge.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.ModernIndustrializationRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertNoUnify;
import static testmod.TestUtils.assertUnify;

public class ModernIndustrializationRecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new ModernIndustrializationRecipeUnifier();

    @SimpleGameTest
    public void test() {
        assertUnify(UNIFIER, """
                {
                  "type": "modern_industrialization:assembler",
                  "eu": 8,
                  "duration": 200,
                  "item_inputs": {
                    "item": "minecraft:test_item",
                    "amount": 8
                  },
                  "fluid_inputs": {
                    "fluid": "minecraft:lava",
                    "amount": 1000
                  },
                  "item_outputs": [
                    {
                      "item": "minecraft:test_item",
                      "amount": 1
                    }
                  ]
                }
                """, """
                {
                   "type": "modern_industrialization:assembler",
                   "eu": 8,
                   "duration": 200,
                   "item_inputs": {
                     "tag": "testmod:test_tag",
                     "amount": 8
                   },
                   "fluid_inputs": {
                     "fluid": "minecraft:lava",
                     "amount": 1000
                   },
                   "item_outputs": [
                     {
                       "item": "testmod:test_item",
                       "amount": 1
                     }
                   ]
                 }
                """);
    }

    @SimpleGameTest
    public void testArrays() {
        assertUnify(UNIFIER, """
                {
                  "type": "modern_industrialization:mixer",
                  "duration": 100,
                  "eu": 2,
                  "item_inputs": [
                    {
                      "amount": 1,
                      "item": "minecraft:test_item"
                    },
                    {
                      "amount": 1,
                      "item": "testmod:test_item"
                    }
                  ],
                  "item_outputs": [
                    {
                      "amount": 2,
                      "item": "minecraft:test_item"
                    }
                  ]
                }
                """, """
                {
                  "type": "modern_industrialization:mixer",
                  "duration": 100,
                  "eu": 2,
                  "item_inputs": [
                    {
                      "amount": 1,
                      "tag": "testmod:test_tag"
                    },
                    {
                      "amount": 1,
                      "tag": "testmod:test_tag"
                    }
                  ],
                  "item_outputs": [
                    {
                      "amount": 2,
                      "item": "testmod:test_item"
                    }
                  ]
                }
                """);
    }

    @SimpleGameTest
    public void testNot() {
        assertNoUnify(UNIFIER, """
                {
                  "type": "modern_industrialization:assembler",
                  "eu": 8,
                  "duration": 200,
                  "item_inputs": {
                    "tag": "c:plates/steel",
                    "amount": 8
                  },
                  "fluid_inputs": {
                    "fluid": "minecraft:lava",
                    "amount": 1000
                  },
                  "item_outputs": [
                    {
                      "item": "modern_industrialization:trash_can",
                      "amount": 1
                    }
                  ]
                }
                """);
    }
}
