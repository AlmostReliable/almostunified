package testmod;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.utils.ReplacementMapImpl;
import com.almostreliable.unified.utils.TagMapImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TestUtils {

    public static final ModPriorities TEST_MOD_PRIORITIES = new ModPrioritiesImpl(
            List.of("ae2", "mekanism", "thermal", "create"),
            new HashMap<>()
    );

    public static final ModPriorities EMPTY_MOD_PRIORITIES = new ModPrioritiesImpl(
            List.of(),
            new HashMap<>()
    );

    public static final StoneStrataLookup EMPTY_STRATA_LOOKUP = new StoneStrataLookup() {
        @Override
        public String getStoneStrata(ResourceLocation item) {
            return "";
        }

        @Override
        public boolean isStoneStrataTag(TagKey<Item> tag) {
            return false;
        }
    };

    public static final TagOwnerships EMPTY_TAG_OWNERSHIPS = new TagOwnerships() {

        @Nullable
        @Override
        public TagKey<Item> getOwner(TagKey<Item> referenceTag) {
            return null;
        }

        @Override
        public Collection<TagKey<Item>> getRefs(TagKey<Item> ownerTag) {
            return List.of();
        }

        @Override
        public Set<TagKey<Item>> getRefs() {
            return Set.of();
        }
    };

    public static TagKey<Item> itemTag(String s) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(s));
    }


    public static TagMap<Item> tagMap() {
        return new TagMapImpl.Builder<Item>()
                .put(itemTag("testmod:ingots/osmium"),
                        "minecraft:osmium_ingot",
                        "mekanism:osmium_ingot",
                        "thermal:osmium_ingot")
                .put(itemTag("testmod:raw_materials/cobalt"),
                        "mekanism:cobalt_chunk",
                        "thermal:cobalt_chunk",
                        "create:cobalt_chunk")
                .put(itemTag("testmod:raw_materials/lead"),
                        "mekanism:lead_chunk",
                        "thermal:lead_chunk",
                        "create:lead_chunk")
                .put(itemTag("testmod:ores/aluminum"),
                        "create:aluminum_ore",
                        "thermal:aluminum_ore",
                        "mekanism:aluminum_ore")
                .build();
    }

    public static ReplacementMap replacementMap() {
        return new ReplacementMapImpl(TEST_MOD_PRIORITIES, tagMap(), EMPTY_STRATA_LOOKUP, EMPTY_TAG_OWNERSHIPS);
    }
}
