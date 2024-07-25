package testmod.tests;

import com.almostreliable.unified.config.PlaceholderConfig;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
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
        var result = placeholderConfig.inflate("c:{type}/{material}");

        assertEquals(6, result.size());
        assertTrue(result.contains(ResourceLocation.tryParse("c:gems/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:gems/gold")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:rods/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:rods/gold")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:raw_materials/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:raw_materials/gold")));
    }
}
