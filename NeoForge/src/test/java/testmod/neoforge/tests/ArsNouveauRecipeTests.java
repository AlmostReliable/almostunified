package testmod.neoforge.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.ArsNouveauRecipeUnifier;
import testmod.TestUtils;
import testmod.gametest_core.SimpleGameTest;

public class ArsNouveauRecipeTests {

    public static final RecipeUnifier UNIFIER = new ArsNouveauRecipeUnifier();

    @SimpleGameTest
    public void testPedestalItems() {
        TestUtils.assertUnify(UNIFIER, """
                {
                  "type": "ars_nouveau:enchanting_apparatus",
                  "keepNbtOfReagent": false,
                  "output": {
                    "item": "ars_nouveau:jar_of_light"
                  },
                  "pedestalItems": [
                    {
                      "item": "minecraft:glowstone"
                    },
                    {
                      "item": "testmod:test_item"
                    },
                    {
                      "tag": "c:glass"
                    }
                  ],
                  "reagent": [
                    {
                      "item": "minecraft:glass_bottle"
                    }
                  ],
                  "sourceCost": 0
                }
                """, """
                {
                  "type": "ars_nouveau:enchanting_apparatus",
                  "keepNbtOfReagent": false,
                  "output": {
                    "item": "ars_nouveau:jar_of_light"
                  },
                  "pedestalItems": [
                    {
                      "item": "minecraft:glowstone"
                    },
                    {
                      "tag": "testmod:test_tag"
                    },
                    {
                      "tag": "c:glass"
                    }
                  ],
                  "reagent": [
                    {
                      "item": "minecraft:glass_bottle"
                    }
                  ],
                  "sourceCost": 0
                }
                """);
    }

    @SimpleGameTest
    public void testPedestalItemsNested() {
        TestUtils.assertUnify(UNIFIER, """
                {
                  "type": "ars_nouveau:reactive_enchantment",
                  "pedestalItems": [
                    {
                      "item": {
                        "item": "testmod:test_item"
                      }
                    },
                    {
                      "item": {
                        "item": "minecraft:test_item"
                      }
                    },
                    {
                      "item": {
                        "tag": "c:storage_blocks/source"
                      }
                    }
                  ],
                  "sourceCost": 3000
                }
                """, """
                {
                  "type": "ars_nouveau:reactive_enchantment",
                  "pedestalItems": [
                    {
                      "item": {
                        "tag": "testmod:test_tag"
                      }
                    },
                    {
                      "item": {
                        "tag": "testmod:test_tag"
                      }
                    },
                    {
                      "item": {
                        "tag": "c:storage_blocks/source"
                      }
                    }
                  ],
                  "sourceCost": 3000
                }
                """);
    }


    @SimpleGameTest
    public void testInputItemsNested() {
        TestUtils.assertUnify(UNIFIER, """
                {
                  "type": "ars_nouveau:glyph",
                  "count": 1,
                  "exp": 27,
                  "inputItems": [
                    {
                      "item": {
                        "item": "ars_nouveau:abjuration_essence"
                      }
                    },
                    {
                      "item": {
                        "item": "minecraft:test_item"
                      }
                    }
                  ],
                  "output": "ars_nouveau:glyph_bounce"
                }
                """, """
                {
                  "type": "ars_nouveau:glyph",
                  "count": 1,
                  "exp": 27,
                  "inputItems": [
                    {
                      "item": {
                        "item": "ars_nouveau:abjuration_essence"
                      }
                    },
                    {
                      "item": {
                        "tag": "testmod:test_tag"
                      }
                    }
                  ],
                  "output": "ars_nouveau:glyph_bounce"
                }
                """);
    }
}
