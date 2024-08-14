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
                        ResourceLocation.parse("minecraft:osmium_ingot"),
                        ResourceLocation.parse("mekanism:osmium_ingot"),
                        ResourceLocation.parse("thermal:osmium_ingot"))
                .put(TestUtils.itemTag("testmod:ingots/cobalt"),
                        ResourceLocation.parse("minecraft:cobalt_ingot"),
                        ResourceLocation.parse("thermal:cobalt_ingot"))
                .put(TestUtils.itemTag("testmod:ingots/electrum"),
                        ResourceLocation.parse("mekanism:electrum_ingot"),
                        ResourceLocation.parse("create:electrum_ingot"),
                        ResourceLocation.parse("thermal:electrum_ingot"))
                .build(modPriorities, TestUtils.EMPTY_VARIANT_LOOKUP, TestUtils.EMPTY_TAG_SUBSTITUTIONS);
    }

    @SimpleGameTest
    public void testTagTargetItem() {
        ArrayList<String> modList = new ArrayList<>(List.of("ae2", "mekanism", "thermal", "create"));
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                modList,
                new HashMap<>()
        );

        var rm = createLookup(modPriorities);


        assertEquals(ResourceLocation.parse("mekanism:osmium_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from mekanism should be target");

        assertNull(rm.getTagTargetItem(TestUtils.itemTag("testmod:not_exist/osmium")),
                "Tag not found should return null");

        assertEquals(ResourceLocation.parse("thermal:cobalt_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/cobalt")).id(),
                "Cobalt ingot from mekanism should be target");

        assertNull(rm.getTagTargetItem(TestUtils.itemTag("testmod:not_exist/cobalt")),
                "Tag not found should return null");

        // Now we remove mekanism from modList.
        // After that `getTagTargetItem` should return the thermal ingot, as AE2 still does not have one.
        modList.remove("mekanism");
        assertEquals(ResourceLocation.parse("thermal:osmium_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from thermal should now be target");
        assertEquals(ResourceLocation.parse("thermal:cobalt_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/cobalt")).id(),
                "Cobalt ingot from thermal should be target");
    }

    @SimpleGameTest
    public void testTagTargetItemWithOverride() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                Util.make(new HashMap<>(),
                        m -> m.put(TestUtils.itemTag("testmod:ingots/electrum"), "create"))
        );

        var rm = createLookup(modPriorities);

        assertEquals(ResourceLocation.parse("create:electrum_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/electrum")).id(),
                "Electrum ingot from create should be target as it is overridden by priorities");

        // but for osmium it's the default behavior
        assertEquals(ResourceLocation.parse("mekanism:osmium_ingot"),
                rm.getTagTargetItem(TestUtils.itemTag("testmod:ingots/osmium")).id(),
                "Osmium ingot from mekanism should be target");
    }

    @SimpleGameTest
    public void testRelevantItemTag() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                new HashMap<>()
        );

        var rm = createLookup(modPriorities);

        assertEquals(TestUtils.itemTag("testmod:ingots/osmium"),
                rm.getRelevantItemTag(ResourceLocation.parse("mekanism:osmium_ingot")));
        assertEquals(TestUtils.itemTag("testmod:ingots/cobalt"),
                rm.getRelevantItemTag(ResourceLocation.parse("thermal:cobalt_ingot")));
        assertEquals(TestUtils.itemTag("testmod:ingots/electrum"),
                rm.getRelevantItemTag(ResourceLocation.parse("create:electrum_ingot")));

        assertNull(rm.getRelevantItemTag(ResourceLocation.parse("not_existing_mod:osmium_ingot")));
    }

    @SimpleGameTest
    public void testReplacementForItem() {
        ModPrioritiesImpl modPriorities = new ModPrioritiesImpl(
                List.of("ae2", "mekanism", "thermal", "create"),
                new HashMap<>()
        );

        var rm = createLookup(modPriorities);

        assertEquals(ResourceLocation.parse("mekanism:osmium_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("mekanism:osmium_ingot")).id());
        assertEquals(ResourceLocation.parse("mekanism:osmium_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("minecraft:osmium_ingot")).id());
        assertEquals(ResourceLocation.parse("mekanism:osmium_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("thermal:osmium_ingot")).id());

        assertEquals(ResourceLocation.parse("thermal:cobalt_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("thermal:cobalt_ingot")).id());
        assertEquals(ResourceLocation.parse("thermal:cobalt_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("minecraft:cobalt_ingot")).id());

        assertEquals(ResourceLocation.parse("mekanism:electrum_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("create:electrum_ingot")).id());
        assertEquals(ResourceLocation.parse("mekanism:electrum_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("mekanism:electrum_ingot")).id());
        assertEquals(ResourceLocation.parse("mekanism:electrum_ingot"),
                rm.getItemReplacement(ResourceLocation.parse("thermal:electrum_ingot")).id());
    }

    @SimpleGameTest
    public void testItemInUnifiedIngredient() {
        var rm = new UnifyLookupImpl.Builder()
                .put(TestUtils.itemTag("c:tools"), Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_SHOVEL)
                .build(TestUtils.EMPTY_MOD_PRIORITIES,
                        TestUtils.EMPTY_VARIANT_LOOKUP,
                        TestUtils.EMPTY_TAG_SUBSTITUTIONS
                );

        Ingredient ingredient = Ingredient.of(Items.IRON_SWORD);

        // Shovel is part of `minecraft:tools` and part of our created tag map
        assertTrue(rm.isItemInUnifiedIngredient(ingredient, Items.IRON_SHOVEL.getDefaultInstance()),
                "SHOVEL is in our created tag map");

        assertFalse(rm.isItemInUnifiedIngredient(ingredient, Items.CARROT.getDefaultInstance()),
                "CARROT is not part of `minecraft:tools`");
    }
}
