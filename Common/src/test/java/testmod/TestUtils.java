package testmod;

import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.StoneVariantLookup;
import com.almostreliable.unified.api.TagSubstitutions;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
import com.almostreliable.unified.impl.UnifyLookupImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.recipe.RecipeJsonImpl;
import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.recipe.UnificationHelperImpl;
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

    public static final StoneVariantLookup EMPTY_VARIANT_LOOKUP = new StoneVariantLookup() {
        @Override
        public String getStoneVariant(ResourceLocation item) {
            return "";
        }

        @Override
        public boolean isOreTag(TagKey<Item> tag) {
            return false;
        }
    };

    public static final TagSubstitutions EMPTY_TAG_SUBSTITUTIONS = new TagSubstitutions() {

        @Nullable
        @Override
        public TagKey<Item> getSubstituteTag(TagKey<Item> referenceTag) {
            return null;
        }

        @Override
        public Collection<TagKey<Item>> getReferenceTags(TagKey<Item> substituteTag) {
            return List.of();
        }

        @Override
        public Set<TagKey<Item>> getReferenceTags() {
            return Set.of();
        }
    };

    public static RecipeLink recipe(String jsonStr) {
        var json = json(jsonStr);
        return new RecipeLink(ResourceLocation.parse("test"), json);
    }

    public static RecipeLink recipe(JsonObject json) {
        return new RecipeLink(ResourceLocation.parse("test"), json);
    }

    public static JsonObject json(String json) {
        return GSON.fromJson(json, JsonObject.class);
    }

    public static TagKey<Item> itemTag(String s) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(s));
    }


    public static UnifyLookup unifyLookup() {
        return new UnifyLookupImpl.Builder()
                .put(itemTag("testmod:test_tag"),
                        ResourceLocation.parse("minecraft:test_item"),
                        ResourceLocation.parse("mekanism:test_item"),
                        ResourceLocation.parse("thermal:test_item"),
                        ResourceLocation.parse("testmod:test_item"))
                .build(TEST_MOD_PRIORITIES, EMPTY_VARIANT_LOOKUP, EMPTY_TAG_SUBSTITUTIONS);
    }


    public static UnificationHelper recipeHelper() {
        return new UnificationHelperImpl(unifyLookup());
    }

    public static void assertUnify(RecipeUnifier unifier, String jsonActual, String jsonExpected) {
        var recipe = TestUtils.recipe(jsonActual);
        JsonObject copy = recipe.getOriginal().deepCopy();
        var json = new RecipeJsonImpl(recipe.getId(), copy);
        unifier.unify(recipeHelper(), json);
        assertNotEquals(recipe.getOriginal(), copy);

        var expected = TestUtils.json(jsonExpected);
        assertJson(expected, copy);
    }

    public static void assertNoUnify(RecipeUnifier unifier, String jsonStr) {
        var recipe = TestUtils.recipe(jsonStr);
        JsonObject copy = recipe.getOriginal().deepCopy();
        var json = new RecipeJsonImpl(recipe.getId(), copy);
        unifier.unify(recipeHelper(), json);
        assertEquals(recipe.getOriginal(), copy);

        var expected = TestUtils.json(jsonStr);
        assertJson(expected, copy);
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
