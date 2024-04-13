package testmod.tests;

import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.impl.UnifyLookupImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import testmod.TestUtils;
import testmod.gametest_core.SimpleGameTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UnifyLookupTests {

    private UnifyLookup createLookup(ModPriorities modPriorities) {
        return new UnifyLookupImpl.Builder()
                .put(TestUtils.itemTag("testmod:ingots/osmium"),
                        new ResourceLocation("minecraft:osmium_ingot"),
                        new ResourceLocation("mekanism:osmium_ingot"),
                        new ResourceLocation("thermal:osmium_ingot"))
                .put(TestUtils.itemTag("testmod:ingots/cobalt"),
                        new ResourceLocation("minecraft:cobalt_ingot"),
                        new ResourceLocation("thermal:cobalt_ingot"))
                .put(TestUtils.itemTag("testmod:ingots/electrum"),
                        new ResourceLocation("mekanism:electrum_ingot"),
                        new ResourceLocation("create:electrum_ingot"),
                        new ResourceLocation("thermal:electrum_ingot"))
                .build(modPriorities, TestUtils.EMPTY_STRATA_LOOKUP, TestUtils.EMPTY_TAG_OWNERSHIPS);
    }

    @SimpleGameTest
    public void testPreferredItemForTag() {
        ArrayList<String> modList = new ArrayList<>(List.of("ae2", "mekanism", "thermal", "create"));
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                modList,
                new HashMap<>()
        );

        var rm = createLookup(modPriorities);


        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from mekanism should be preferred");

        assertNull(rm.getPreferredItemForTag(TestUtils.itemTag("testmod:not_exist/osmium")),
                "Tag not found should return null");

        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/cobalt")).id(),
                "Cobalt ingot from mekanism should be preferred");

        assertNull(rm.getPreferredItemForTag(TestUtils.itemTag("testmod:not_exist/cobalt")),
                "Tag not found should return null");

        // Now we remove mekanism from modList.
        // After that `getPreferredItemForTag` should return the thermal ingot, as AE2 still does not have one.
        modList.remove("mekanism");
        assertEquals(new ResourceLocation("thermal:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from thermal should now be preferred");
        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/cobalt")).id(),
                "Cobalt ingot from thermal should be preferred");
    }

    @SimpleGameTest
    public void testPreferredItemForTagWithOverride() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                Util.make(new HashMap<>(),
                        m -> m.put(TestUtils.itemTag("testmod:ingots/electrum"), "create"))
        );

        var rm = createLookup(modPriorities);

        assertEquals(new ResourceLocation("create:electrum_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/electrum")).id(),
                "Electrum ingot from create should be preferred as it is overridden by priorities");

        // but for osmium it's the default behavior
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getPreferredItemForTag(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from mekanism should be preferred");
    }

    @SimpleGameTest
    public void testPreferredTagForItem() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                new HashMap<>()
        );

        var rm = createLookup(modPriorities);

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

        var rm = createLookup(modPriorities);

        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("mekanism:osmium_ingot")).id());
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("minecraft:osmium_ingot")).id());
        assertEquals(new ResourceLocation("mekanism:osmium_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:osmium_ingot")).id());

        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:cobalt_ingot")).id());
        assertEquals(new ResourceLocation("thermal:cobalt_ingot"),
                rm.getReplacementForItem(new ResourceLocation("minecraft:cobalt_ingot")).id());

        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("create:electrum_ingot")).id());
        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("mekanism:electrum_ingot")).id());
        assertEquals(new ResourceLocation("mekanism:electrum_ingot"),
                rm.getReplacementForItem(new ResourceLocation("thermal:electrum_ingot")).id());
    }

    @SimpleGameTest
    public void testItemInUnifiedIngredient() {
        var rm = new UnifyLookupImpl.Builder()
                .put(TestUtils.itemTag("minecraft:tools"), Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_SHOVEL)
                .build(TestUtils.EMPTY_MOD_PRIORITIES,
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
