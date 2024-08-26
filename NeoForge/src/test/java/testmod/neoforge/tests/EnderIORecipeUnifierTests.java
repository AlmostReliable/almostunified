package testmod.neoforge.tests;

import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.unification.EnderIORecipeUnifier;

import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertNoUnify;
import static testmod.TestUtils.assertUnify;

public class EnderIORecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new EnderIORecipeUnifier();

    @SimpleGameTest
    public void test() {
        assertUnify(UNIFIER, """
            {
              "type": "enderio:grinding_ball",
              "chance": 1.65,
              "durability": 40000,
              "grinding": 1.2,
              "item": "minecraft:test_item",
              "power": 0.8
            }
            """, """
            {
              "type": "enderio:grinding_ball",
              "chance": 1.65,
              "durability": 40000,
              "grinding": 1.2,
              "item": "testmod:test_item",
              "power": 0.8
            }
            """);
    }

    @SimpleGameTest
    public void testNot() {
        assertNoUnify(UNIFIER, """
            {
              "type": "enderio:grinding_ball",
              "chance": 1.65,
              "durability": 40000,
              "grinding": 1.2,
              "item": "enderio:copper_alloy_grinding_ball",
              "power": 0.8
            }
            """);
    }
}
