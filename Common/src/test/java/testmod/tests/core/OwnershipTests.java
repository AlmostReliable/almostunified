package testmod.tests.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import testmod.gametest_core.SimpleGameTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OwnershipTests {

    /**
     * See `tags.json` test config.
     * mod_a:silver_ore and mod_b:silver_ore both have the tag `forge:ores/silver`, while mod_c:silver_ore only has the `forge:silver_ores` tag.
     * <p>
     * Through the ownership system, we tell AU that `forge:ores/silver` should own `forge:silver_ores`. Which will result into mod_c:silver_ore is now part of `forge:ores/silver`
     */
    @SimpleGameTest
    public void checkSilverOwnerships() {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("mod_c:silver_ore"));
        Set<TagKey<Item>> itemTags = item.builtInRegistryHolder().tags().collect(Collectors.toSet());
        assertTrue(itemTags.contains(TagKey.create(Registries.ITEM, new ResourceLocation("forge:ores/silver"))));
    }
}
