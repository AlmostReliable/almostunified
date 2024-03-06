package testmod.tests;

import com.almostreliable.unified.config.ReplacementsConfig;
import net.minecraft.resources.ResourceLocation;
import testmod.gametest_core.SimpleGameTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplacementsTests {
    @SimpleGameTest
    public void testInflate() {
        var replacements = new ReplacementsConfig(Map.of("material",
                Set.of("iron", "gold"),
                "type",
                Set.of("gems", "rods", "raw_materials")));

        var result = replacements.inflate("c:{type}/{material}");
        assertEquals(6, result.size());
        assertTrue(result.contains(ResourceLocation.tryParse("c:gems/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:gems/gold")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:rods/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:rods/gold")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:raw_materials/iron")));
        assertTrue(result.contains(ResourceLocation.tryParse("c:raw_materials/gold")));
    }
}
