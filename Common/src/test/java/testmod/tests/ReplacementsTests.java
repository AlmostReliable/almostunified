package testmod.tests;

import com.almostreliable.unified.config.PlaceholderConfig;
import com.almostreliable.unified.utils.JsonUtils;

import com.google.gson.JsonObject;
import testmod.gametest_core.SimpleGameTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplacementsTests {

    private static final JsonObject INFLATE_PLACEHOLDERS = JsonUtils.readFromString("""
        {
            "type": [
                "gems",
                "rods",
                "raw_materials"
            ],
            "material": [
                "iron",
                "gold"
            ]
        }
        """, JsonObject.class);

    @SimpleGameTest
    public void testInflate() {
        var placeholderConfig = PlaceholderConfig.SERIALIZER.handleDeserialization(INFLATE_PLACEHOLDERS);
        var result = placeholderConfig.apply("c:{type}/{material}");

        assertEquals(6, result.size());
        assertTrue(result.contains("c:gems/iron"));
        assertTrue(result.contains("c:gems/gold"));
        assertTrue(result.contains("c:rods/iron"));
        assertTrue(result.contains("c:rods/gold"));
        assertTrue(result.contains("c:raw_materials/iron"));
        assertTrue(result.contains("c:raw_materials/gold"));
    }
}
