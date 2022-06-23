package com.almostreliable.unified.utils;

import com.almostreliable.unified.TestUtils;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReplacementMapTests {

    @Test
    public void getPreferredItemByTag() {
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(),
                TestUtils.TEST_ALLOWED_TAGS,
                TestUtils.TEST_MOD_PRIORITIES);
        assertEquals(map.getPreferredItemByTag(TestUtils.BRONZE_ORES_TAG), TestUtils.mod1RL("bronze_ore"));
        assertNotEquals(map.getPreferredItemByTag(TestUtils.BRONZE_ORES_TAG), TestUtils.mod2RL("bronze_ore"));
        assertEquals(map.getPreferredItemByTag(TestUtils.INVAR_ORES_TAG), TestUtils.mod1RL("invar_ore"));
        assertNotEquals(map.getPreferredItemByTag(TestUtils.INVAR_ORES_TAG), TestUtils.mod2RL("invar_ore"));
        assertEquals(map.getPreferredItemByTag(TestUtils.TIN_ORES_TAG), TestUtils.mod3RL("tin_ore"));
        assertNotEquals(map.getPreferredItemByTag(TestUtils.TIN_ORES_TAG), TestUtils.mod4RL("tin_ore"));
        assertEquals(map.getPreferredItemByTag(TestUtils.SILVER_ORES_TAG), TestUtils.mod3RL("silver_ore"));
        assertNotEquals(map.getPreferredItemByTag(TestUtils.SILVER_ORES_TAG), TestUtils.mod4RL("silver_ore"));
    }

    @Test
    public void getPreferredItemByTag_ReversePriority() {
        // We reverse the order. See `testTagMap` for the mapping.
        List<String> reverse = Lists.reverse(TestUtils.TEST_MOD_PRIORITIES);
        ReplacementMap reverseMap = new ReplacementMap(TagMapTests.testTagMap(), TestUtils.TEST_ALLOWED_TAGS, reverse);
        assertEquals(reverseMap.getPreferredItemByTag(TestUtils.BRONZE_ORES_TAG), TestUtils.mod3RL("bronze_ore"));
        assertEquals(reverseMap.getPreferredItemByTag(TestUtils.INVAR_ORES_TAG), TestUtils.mod4RL("invar_ore"));
        assertEquals(reverseMap.getPreferredItemByTag(TestUtils.TIN_ORES_TAG), TestUtils.mod4RL("tin_ore"));
        assertEquals(reverseMap.getPreferredItemByTag(TestUtils.SILVER_ORES_TAG), TestUtils.mod5RL("silver_ore"));
    }

    @Test
    public void getPreferredTag() {
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(),
                TestUtils.TEST_ALLOWED_TAGS,
                TestUtils.TEST_MOD_PRIORITIES);

        assertEquals(map.getPreferredTag(TestUtils.mod1RL("bronze_ore")), TestUtils.BRONZE_ORES_TAG);
        assertNull(map.getPreferredTag(new ResourceLocation("minecraft:diamond")), "We don't have a tag for diamond");
    }
}
