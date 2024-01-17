package testmod.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.SmithingRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class SmithingRecipeUnifierTest {

    public static final RecipeUnifier UNIFIER = new SmithingRecipeUnifier();

    @SimpleGameTest
    public void testTrim() {
        assertUnify(UNIFIER, """
                {
                  "type": "minecraft:smithing_trim",
                  "addition": {
                    "item": "minecraft:test_item"
                  },
                  "base": {
                    "item": "minecraft:test_item"
                  },
                  "template": {
                    "item": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "minecraft:smithing_trim",
                  "addition": {
                    "tag": "testmod:test_tag"
                  },
                  "base": {
                    "tag": "testmod:test_tag"
                  },
                  "template": {
                    "tag": "testmod:test_tag"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testTransform() {
        assertUnify(UNIFIER, """
                {
                  "type": "minecraft:smithing_transform",
                  "addition": {
                    "item": "minecraft:test_item"
                  },
                  "base": {
                    "item": "minecraft:test_item"
                  },
                  "result": {
                    "item": "minecraft:test_item"
                  },
                  "template": {
                    "item": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "minecraft:smithing_transform",
                  "addition": {
                    "tag": "testmod:test_tag"
                  },
                  "base": {
                    "tag": "testmod:test_tag"
                  },
                  "result": {
                    "item": "testmod:test_item"
                  },
                  "template": {
                    "tag": "testmod:test_tag"
                  }
                }
                """);
    }
}
