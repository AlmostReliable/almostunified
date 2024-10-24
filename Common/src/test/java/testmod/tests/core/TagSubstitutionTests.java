package testmod.tests.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.unification.UnificationEntry;
import com.almostreliable.unified.api.unification.UnificationLookup;

import testmod.gametest_core.SimpleGameTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagSubstitutionTests {

    /**
     * See `tags.json` test config.
     * mod_a:silver_ore and mod_b:silver_ore both have the tag `c:ores/silver`, while mod_c:silver_ore only has the `c:silver_ores` tag.
     * <p>
     * Through the substitution system, we tell AU that `c:ores/silver` should own `c:silver_ores`. Which will result into mod_c:silver_ore is now part of `c:ores/silver`
     */
    @SimpleGameTest
    public void checkSilverSubstitutions() {
        ResourceLocation silverOreId = ResourceLocation.parse("mod_c:silver_ore");
        Item item = BuiltInRegistries.ITEM.get(silverOreId);
        Set<TagKey<Item>> itemTags = item.builtInRegistryHolder().tags().collect(Collectors.toSet());
        TagKey<Item> silverTag = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores/silver"));
        assertTrue(itemTags.contains(silverTag));

        UnificationLookup unificationLookup = AlmostUnified.INSTANCE.getRuntimeOrThrow().getUnificationLookup();
        TagKey<Item> unifyTag = unificationLookup.getRelevantItemTag(silverOreId);
        assertEquals(silverTag, unifyTag);
        UnificationEntry<Item> silverOreEntry = unificationLookup.getTagTargetItem(silverTag);
        assertEquals(silverOreId, silverOreEntry.id());

    }
}
