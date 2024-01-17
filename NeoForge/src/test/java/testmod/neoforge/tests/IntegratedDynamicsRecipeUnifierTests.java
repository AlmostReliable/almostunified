package testmod.neoforge.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.IntegratedDynamicsRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class IntegratedDynamicsRecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new IntegratedDynamicsRecipeUnifier();

    @SimpleGameTest
    public void testBasin() {
        assertUnify(UNIFIER, """
                {
                  "type": "integrateddynamics:drying_basin",
                  "item": "minecraft:test_item",
                  "fluid": {
                    "fluid": "minecraft:water",
                    "amount": 250
                  },
                  "duration": 100,
                  "result": {
                    "item": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "integrateddynamics:drying_basin",
                  "item": {
                    "tag": "testmod:test_tag"
                  },
                  "fluid": {
                    "fluid": "minecraft:water",
                    "amount": 250
                  },
                  "duration": 100,
                  "result": {
                    "item": "testmod:test_item"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testMechanicalSqueezerItemInItems() {
        assertUnify(UNIFIER, """
                {
                  "type": "integrateddynamics:mechanical_squeezer",
                  "item": "testmod:test_item",
                  "result": {
                    "fluid": {
                      "fluid": "integrateddynamics:menril_resin",
                      "amount": 1000
                    },
                    "items": [
                      {
                        "item": {
                          "tag": "testmod:test_tag",
                          "count": 2
                        }
                      },
                      {
                        "item": "minecraft:test_item",
                        "chance": 0.5
                      },
                      {
                        "item": "testmod:test_item",
                        "chance": 0.5
                      }
                    ]
                  },
                  "duration": 15
                }
                """, """
                {
                  "type": "integrateddynamics:mechanical_squeezer",
                  "item": {
                    "tag": "testmod:test_tag"
                  },
                  "result": {
                    "fluid": {
                      "fluid": "integrateddynamics:menril_resin",
                      "amount": 1000
                    },
                    "items": [
                      {
                        "item": {
                          "item": "testmod:test_item",
                          "count": 2
                        }
                      },
                      {
                        "item": "testmod:test_item",
                        "chance": 0.5
                      },
                      {
                        "item": "testmod:test_item",
                        "chance": 0.5
                      }
                    ]
                  },
                  "duration": 15
                }
                """);
    }
}
