package testmod.neoforge.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.ArsNouveauRecipeUnifier;
import testmod.TestUtils;
import testmod.gametest_core.SimpleGameTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testmod.TestUtils.recipeContext;

public class ArsNouveauRecipeTests {

    public static final RecipeUnifier UNIFIER = new ArsNouveauRecipeUnifier();

    @SimpleGameTest
    public void testPedestalItems() {
        var actual = TestUtils.json("""
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
                      "tag": "forge:glass"
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

        var recipe = TestUtils.recipe(actual);
        UNIFIER.unifyItems(recipeContext(), recipe);
        assertTrue(recipe.changed());

        var expected = TestUtils.json("""
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
                      "tag": "forge:glass"
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
        assertEquals(expected, actual);
    }

    @SimpleGameTest
    public void testPedestalItemsNested() {
        var actual = TestUtils.json("""
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
                        "tag": "forge:storage_blocks/source"
                      }
                    }
                  ],
                  "sourceCost": 3000
                }
                """);

        var recipe = TestUtils.recipe(actual);
        UNIFIER.unifyItems(recipeContext(), recipe);
        assertTrue(recipe.changed());

        var expected = TestUtils.json("""
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
                        "tag": "forge:storage_blocks/source"
                      }
                    }
                  ],
                  "sourceCost": 3000
                }
                """);
        assertEquals(expected, actual);
    }


    @SimpleGameTest
    public void testInputItemsNested() {
        var actual = TestUtils.json("""
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
                """);

        var recipe = TestUtils.recipe(actual);
        UNIFIER.unifyItems(recipeContext(), recipe);
        assertTrue(recipe.changed());

        var expected = TestUtils.json("""
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
        assertEquals(expected, actual);
    }
}