package testmod.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class GregTechModernRecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new GregTechModernRecipeUnifier();

    @SimpleGameTest
    public void test() {
        assertUnify(UNIFIER, """
                {
                  "type": "gtceu:extruder",
                  "duration": 646,
                  "inputs": {
                    "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "testmod:test_item"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      },
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "minecraft:test_item"
                          }
                        },
                        "chance": 0.0,
                        "tierChanceBoost": 0.0
                      },
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "minecraft:ender_pearl"
                          }
                        },
                        "chance": 0.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                  },
                  "outputs": {
                    "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 2,
                          "ingredient": {
                            "item": "minecraft:test_item"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      },
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 2,
                          "ingredient": {
                            "tag": "testmod:test_tag"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                  },
                  "tickInputs": {
                    "eu": [
                      {
                        "content": 180,
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ],
                    "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "minecraft:test_item"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                  },
                  "tickOutputs": {
                    "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "tag": "testmod:test_tag"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                  }
                }
                """, """
                {
                   "type": "gtceu:extruder",
                   "duration": 646,
                   "inputs": {
                     "item": [
                       {
                         "content": {
                           "type": "gtceu:sized",
                           "count": 1,
                           "ingredient": {
                             "tag": "testmod:test_tag"
                           }
                         },
                         "chance": 1.0,
                         "tierChanceBoost": 0.0
                       },
                       {
                         "content": {
                           "type": "gtceu:sized",
                           "count": 1,
                           "ingredient": {
                             "tag": "testmod:test_tag"
                           }
                         },
                         "chance": 0.0,
                         "tierChanceBoost": 0.0
                       },
                       {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "minecraft:ender_pearl"
                          }
                        },
                        "chance": 0.0,
                        "tierChanceBoost": 0.0
                      }
                     ]
                   },
                   "outputs": {
                     "item": [
                       {
                         "content": {
                           "type": "gtceu:sized",
                           "count": 2,
                           "ingredient": {
                             "item": "testmod:test_item"
                           }
                         },
                         "chance": 1.0,
                         "tierChanceBoost": 0.0
                       },
                       {
                         "content": {
                           "type": "gtceu:sized",
                           "count": 2,
                           "ingredient": {
                             "item": "testmod:test_item"
                           }
                         },
                         "chance": 1.0,
                         "tierChanceBoost": 0.0
                       }
                     ]
                   },
                   "tickInputs": {
                     "eu": [
                       {
                         "content": 180,
                         "chance": 1.0,
                         "tierChanceBoost": 0.0
                       }
                     ],
                     "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "tag": "testmod:test_tag"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                   },
                   "tickOutputs": {
                    "item": [
                      {
                        "content": {
                          "type": "gtceu:sized",
                          "count": 1,
                          "ingredient": {
                            "item": "testmod:test_item"
                          }
                        },
                        "chance": 1.0,
                        "tierChanceBoost": 0.0
                      }
                    ]
                  }
                 }
                """);
    }
}
