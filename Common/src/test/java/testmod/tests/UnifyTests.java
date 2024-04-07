package testmod.tests;

import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.impl.TagMapImpl;
import com.almostreliable.unified.impl.UnifyLookupImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import testmod.gametest_core.SimpleGameTest;

import java.util.HashMap;
import java.util.List;

import static testmod.TestUtils.*;

public class UnifyTests {

    private static final ResourceLocation TEST_ID = new ResourceLocation("testmod", "test_recipe");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final ModPriorities MOD_PRIORITIES = new ModPrioritiesImpl(List.of("minecraft",
            "mekanism",
            "thermal",
            "create"), new HashMap<>());

    private static final TagMap<Item> TAG_MAP = new TagMapImpl.Builder<>(BuiltInRegistries.ITEM)
            .put(itemTag("testmod:ingots/iron"),
                    Items.IRON_INGOT)
            .build();

    public static UnifyLookup unifyLookup() {
        return new UnifyLookupImpl(MOD_PRIORITIES, TAG_MAP, EMPTY_STRATA_LOOKUP, EMPTY_TAG_OWNERSHIPS);
    }

    private static JsonObject json(String str) {
        return GSON.fromJson(str, JsonObject.class);
    }

    @SimpleGameTest
    public void test() {
        var rm = unifyLookup();
        var recipe = json("""
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "equipment",
                  "key": {
                    "#": {
                      "item": "minecraft:stick"
                    },
                    "X": {
                      "item": "minecraft:iron_ingot"
                    }
                  },
                  "pattern": [
                    "X",
                    "#",
                    "#"
                  ],
                  "result": {
                    "item": "minecraft:iron_shovel"
                  },
                  "show_notification": true
                }
                """);

    }

    @SimpleGameTest
    public void test2() {

    }

    @SimpleGameTest
    public void test3() {

    }

    @SimpleGameTest
    public void test4() {

    }

    @SimpleGameTest
    public void test5() {

    }

    @SimpleGameTest
    public void test6() {

    }

    @SimpleGameTest
    public void test7() {

    }

    @SimpleGameTest
    public void test8() {

    }

    @SimpleGameTest
    public void test9() {

    }

    @SimpleGameTest
    public void test10() {

    }

    @SimpleGameTest
    public void test11() {

    }

    @SimpleGameTest
    public void test12() {

    }

    @SimpleGameTest
    public void test13() {

    }
}
