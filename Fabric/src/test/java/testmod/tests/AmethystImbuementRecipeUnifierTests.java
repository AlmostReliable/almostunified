package testmod.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.AmethystImbuementRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class AmethystImbuementRecipeUnifierTests {
    public static final RecipeUnifier UNIFIER = new AmethystImbuementRecipeUnifier();

    @SimpleGameTest
    public void testImbuing() {
        assertUnify(UNIFIER, """
                {
                  "type": "amethyst_imbuement:imbuing",
                  "imbueA": {
                	"item": "testmod:test_item"
                  },
                  "imbueB": {
                	"item": "testmod:test_item"
                  },
                  "imbueC": {
                	"item": "testmod:test_item"
                  },
                  "imbueD": {
                	"item": "testmod:test_item"
                  },
                  "craftA": {
                    "item": "testmod:test_item"
                  },
                  "craftB": {
                	"item": "testmod:test_item"
                  },
                  "craftC": {
                    "item": "testmod:test_item"
                  },
                  "craftD": {
                	"item": "testmod:test_item"
                  },
                  "craftE": {
                    "item": "testmod:test_item"
                  },
                  "craftF": {
                	"item": "testmod:test_item"
                  },
                  "craftG": {
                    "item": "testmod:test_item"
                  },
                  "craftH": {
                	"item": "testmod:test_item"
                  },
                  "craftI": {
                    "item": "testmod:test_item"
                  },
                  "title": "Witches Orb",
                  "cost": 19,
                  "resultA": "minecraft:test_item",
                  "countA": 1
                }
                """, """
                {
                  "type": "amethyst_imbuement:imbuing",
                  "imbueA": {
                	"tag": "testmod:test_tag"
                  },
                  "imbueB": {
                	"tag": "testmod:test_tag"
                  },
                  "imbueC": {
                	"tag": "testmod:test_tag"
                  },
                  "imbueD": {
                	"tag": "testmod:test_tag"
                  },
                  "craftA": {
                    "tag": "testmod:test_tag"
                  },
                  "craftB": {
                	"tag": "testmod:test_tag"
                  },
                  "craftC": {
                    "tag": "testmod:test_tag"
                  },
                  "craftD": {
                	"tag": "testmod:test_tag"
                  },
                  "craftE": {
                    "tag": "testmod:test_tag"
                  },
                  "craftF": {
                	"tag": "testmod:test_tag"
                  },
                  "craftG": {
                    "tag": "testmod:test_tag"
                  },
                  "craftH": {
                	"tag": "testmod:test_tag"
                  },
                  "craftI": {
                    "tag": "testmod:test_tag"
                  },
                  "title": "Witches Orb",
                  "cost": 19,
                  "resultA": "testmod:test_item",
                  "countA": 1
                }
                """);
    }
}
