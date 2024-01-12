package testmod.tests;

import com.almostreliable.unified.utils.json.JsonCursor;
import com.almostreliable.unified.utils.json.JsonWalker;
import com.almostreliable.unified.utils.json.impl.JsonCursorImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import testmod.gametest_core.SimpleGameTest;

public class JsonWalkerTests {

    private static final Gson GSON = new Gson();

    @SimpleGameTest
    public void test() {
        String strJson = """
                {
                    "type": "mekanism:combining",
                    "extraInput": { "ingredient": { "tag": "forge:cobblestone/normal" } },
                    "mainInput": { "amount": 8, "ingredient": { "tag": "forge:raw_materials/osmium" } },
                    "output": { "item": "mekanism:osmium_ore" }
                }
                """;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject json = GSON.fromJson(strJson, JsonObject.class);
        System.out.println("Before: ");
        System.out.println(gson.toJson(json));

        JsonWalker walker = new JsonWalker(json);
        walker.walk(this::printIt);


        System.out.println("\nAfter: ");
        System.out.println(walker.hasChanged());
        System.out.println(gson.toJson(json));
    }

    private void printIt(JsonCursor cursor) {
        if (cursor.isPrimitive()) {
            cursor.set(new JsonPrimitive("new value"));
            return;
        }

        cursor.walk(this::printIt);
    }
}
