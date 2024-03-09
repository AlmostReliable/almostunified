package testmod;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.impl.ReplacementMapImpl;
import com.almostreliable.unified.impl.TagMapImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.recipe.RecipeContextImpl;
import com.almostreliable.unified.recipe.RecipeJsonImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

    public static final Gson GSON = new GsonBuilder().create();

    public static final ModPriorities TEST_MOD_PRIORITIES = new ModPrioritiesImpl(
            List.of("testmod", "mekanism", "thermal", "create"),
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

    public static RecipeJson recipe(JsonObject json) {
        return new RecipeJsonImpl(new ResourceLocation("test"), json);
    }

    public static JsonObject json(String json) {
        return GSON.fromJson(json, JsonObject.class);
    }

    public static TagKey<Item> itemTag(String s) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(s));
    }


    public static TagMap<Item> tagMap() {
        return new TagMapImpl.Builder<Item>()
                .put(itemTag("testmod:test_tag"),
                        "minecraft:test_item",
                        "mekanism:test_item",
                        "thermal:test_item",
                        "testmod:test_item")
                .build();
    }

    public static ReplacementMap replacementMap() {
        return new ReplacementMapImpl(TEST_MOD_PRIORITIES, tagMap(), EMPTY_STRATA_LOOKUP, EMPTY_TAG_OWNERSHIPS);
    }

    public static ReplacementMap replacementMap(TagMap<Item> tagMap) {
        return new ReplacementMapImpl(TEST_MOD_PRIORITIES, tagMap, EMPTY_STRATA_LOOKUP, EMPTY_TAG_OWNERSHIPS);
    }

    public static RecipeContext recipeContext() {
        return new RecipeContextImpl(replacementMap());
    }

    public static void assertUnify(RecipeUnifier unifier, String jsonActual, String jsonExpected) {
        var actual = TestUtils.json(jsonActual);

        var recipe = TestUtils.recipe(actual);
        unifier.unifyItems(recipeContext(), recipe);
        assertTrue(recipe.changed());

        var expected = TestUtils.json(jsonExpected);
        assertJson(expected, actual);
    }

    public static void assertNoUnify(RecipeUnifier unifier, String json) {
        var actual = TestUtils.json(json);

        var recipe = TestUtils.recipe(actual);
        unifier.unifyItems(recipeContext(), recipe);
        assertFalse(recipe.changed());

        var expected = TestUtils.json(json);
        assertJson(expected, actual);
    }

    public static void assertJson(JsonObject expected, JsonObject actual) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> expectedMap = GSON.fromJson(expected, type);
        Map<String, Object> actualMap = GSON.fromJson(actual, type);
        var difference = Maps.difference(expectedMap, actualMap);

        if (difference.areEqual()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb
                .append("\nExpected:\t")
                .append(GSON.toJson(expected))
                .append("\nActual:\t\t")
                .append(GSON.toJson(actual))
                .append("\n");
        if (!difference.entriesDiffering().isEmpty()) {
            sb.append("Differences:\n");
            difference.entriesDiffering().forEach((k, v) -> {
                sb.append("\t").append(k).append(": ").append(v).append("\n");
            });
        }

        if (!difference.entriesOnlyOnLeft().isEmpty()) {
            sb.append("Only on left:\n");
            difference.entriesOnlyOnLeft().forEach((k, v) -> {
                sb.append("\t").append(k).append(": ").append(v).append("\n");
            });
        }

        if (!difference.entriesOnlyOnRight().isEmpty()) {
            sb.append("Only on right:\n");
            difference.entriesOnlyOnRight().forEach((k, v) -> {
                sb.append("\t").append(k).append(": ").append(v).append("\n");
            });
        }

        fail(sb.toString());
    }
}
