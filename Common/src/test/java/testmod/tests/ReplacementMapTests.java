package testmod.tests;

import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.impl.ReplacementMapImpl;
import com.almostreliable.unified.impl.TagMapImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import testmod.TestUtils;
import testmod.gametest_core.SimpleGameTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReplacementMapTests {

    private static final TagMap<Item> TAG_MAP = new TagMapImpl.Builder<Item>()
            .put(TestUtils.itemTag("testmod:ingots/osmium"),
                    "minecraft:osmium_ingot",
                    "mekanism:osmium_ingot",
                    "thermal:osmium_ingot")
            .put(TestUtils.itemTag("testmod:ingots/cobalt"),
                    "minecraft:cobalt_ingot",
                    "thermal:cobalt_ingot")
            .put(TestUtils.itemTag("testmod:ingots/electrum"),
                    "mekanism:electrum_ingot",
                    "create:electrum_ingot",
                    "thermal:electrum_ingot")
            .build();

    @SimpleGameTest
    public void testPreferredItemForTag() {
        ArrayList<String> modList = new ArrayList<>(List.of("ae2", "mekanism", "thermal", "create"));
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                modList,
                new HashMap<>()
        );

        var rm = new ReplacementMapImpl(modPriorities,
                TAG_MAP,
                TestUtils.EMPTY_STRATA_LOOKUP,
                TestUtils.EMPTY_TAG_OWNERSHIPS);

        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")),
                "Osmium ingot from mekanism should be preferred");

        assertNull(rm.getPreferredItemForTag(TestUtils.itemTag("testmod:not_exist/osmium")),
                "Tag not found should return null");

        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/cobalt")),
                "Cobalt ingot from mekanism should be preferred");

        assertNull(rm.getPreferredItemForTag(TestUtils.itemTag("testmod:not_exist/cobalt")),
                "Tag not found should return null");

        // Now we remove mekanism from modList.
        // After that `getPreferredItemForTag` should return the thermal ingot, as AE2 still does not have one.
        modList.remove("mekanism");
        assertEquals(new ResourceLocation("thermal:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")),
                "Osmium ingot from thermal should now be preferred");
        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/cobalt")),
                "Cobalt ingot from thermal should be preferred");
    }

    @SimpleGameTest
    public void testPreferredItemForTagWithOverride() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                Util.make(new HashMap<>(),
                        m -> m.put(TestUtils.itemTag("testmod:ingots/electrum"), "create"))
        );

        var rm = new ReplacementMapImpl(modPriorities,
                TAG_MAP,
                TestUtils.EMPTY_STRATA_LOOKUP,
                TestUtils.EMPTY_TAG_OWNERSHIPS);

        assertEquals(new ResourceLocation("create:electrum_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/electrum")),
                "Electrum ingot from create should be preferred as it is overridden by priorities");

        // but for osmium it's the default behavior
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")),
                "Osmium ingot from mekanism should be preferred");
    }

    @SimpleGameTest
    public void testPreferredTagForItem() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                new HashMap<>()
        );

        var rm = new ReplacementMapImpl(modPriorities,
                TAG_MAP,
                TestUtils.EMPTY_STRATA_LOOKUP,
                TestUtils.EMPTY_TAG_OWNERSHIPS);


        assertEquals(TestUtils.itemTag("testmod:ingots/osmium"),
                rm.getPreferredTagForItem(new ResourceLocation("mekanism:osmium_ingot")));
        assertEquals(TestUtils.itemTag("testmod:ingots/cobalt"),
                rm.getPreferredTagForItem(new ResourceLocation("thermal:cobalt_ingot")));
        assertEquals(TestUtils.itemTag("testmod:ingots/electrum"),
                rm.getPreferredTagForItem(new ResourceLocation("create:electrum_ingot")));

        assertNull(rm.getPreferredTagForItem(new ResourceLocation("not_existing_mod:osmium_ingot")));
    }

    @SimpleGameTest
    public void testReplacementForItem() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                new HashMap<>()
        );

        var rm = new ReplacementMapImpl(modPriorities,
                TAG_MAP,
                TestUtils.EMPTY_STRATA_LOOKUP,
                TestUtils.EMPTY_TAG_OWNERSHIPS);

        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("mekanism:osmium_ingot")));
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("minecraft:osmium_ingot")));
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:osmium_ingot")));

        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:cobalt_ingot")));
        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getReplacementForItem(new ResourceLocation("minecraft:cobalt_ingot")));

        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("create:electrum_ingot")));
        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("mekanism:electrum_ingot")));
        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:electrum_ingot")));
    }

    @SimpleGameTest
    public void testItemInUnifiedIngredient() {
        TagMap<Item> tagMap = new TagMapImpl.Builder<Item>()
                .put(TestUtils.itemTag("minecraft:tools"),
                        "minecraft:iron_sword",
                        "minecraft:iron_pickaxe",
                        "minecraft:iron_shovel")
                .build();
        var rm = new ReplacementMapImpl(TestUtils.EMPTY_MOD_PRIORITIES,
                tagMap,
                TestUtils.EMPTY_STRATA_LOOKUP,
                TestUtils.EMPTY_TAG_OWNERSHIPS);

        Ingredient ingredient = Ingredient.of(Items.IRON_SWORD);

        // Shovel is part of `minecraft:tools` and part of our created tag map
        assertTrue(rm.isItemInUnifiedIngredient(ingredient, Items.IRON_SHOVEL.getDefaultInstance()),
                "SHOVEL is in our created tag map");

        assertFalse(rm.isItemInUnifiedIngredient(ingredient, Items.CARROT.getDefaultInstance()),
                "CARROT is not part of `minecraft:tools`");
    }
}
