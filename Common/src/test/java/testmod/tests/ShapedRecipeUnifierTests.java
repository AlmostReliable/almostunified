package testmod.tests;

import com.almostreliable.unified.api.unification.bundled.ShapedRecipeUnifier;

import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class ShapedRecipeUnifierTests {

    @SimpleGameTest
    public void test() {
        assertUnify(ShapedRecipeUnifier.INSTANCE, """
            {
              "type": "minecraft:crafting_shaped",
              "category": "equipment",
              "key": {
                "#": {
                  "item": "minecraft:stick"
                },
                "X": {
                  "item": "minecraft:test_item"
                }
              },
              "pattern": [
                "XXX",
                " # ",
                " # "
              ],
              "result": {
                "item": "minecraft:test_item"
              },
              "show_notification": true
            }
            """, """
            {
              "type": "minecraft:crafting_shaped",
              "category": "equipment",
              "key": {
                "#": {
                  "item": "minecraft:stick"
                },
                "X": {
                  "tag": "testmod:test_tag"
                }
              },
              "pattern": [
                "XXX",
                " # ",
                " # "
              ],
              "result": {
                "item": "testmod:test_item"
              },
              "show_notification": true
            }
            """);
    }
}
