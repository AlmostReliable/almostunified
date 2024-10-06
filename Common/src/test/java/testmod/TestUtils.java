package testmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.almostreliable.unified.api.unification.ModPriorities;
import com.almostreliable.unified.api.unification.StoneVariants;
import com.almostreliable.unified.api.unification.TagSubstitutions;
import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.almostreliable.unified.unification.ModPrioritiesImpl;
import com.almostreliable.unified.unification.UnificationLookupImpl;
import com.almostreliable.unified.unification.recipe.RecipeJsonImpl;
import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.unification.recipe.UnificationHelperImpl;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static final StoneVariants EMPTY_STONE_VARIANTS = new StoneVariants() {
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
        public TagKey<Item> getSubstituteTag(TagKey<Item> replacedTag) {
            return null;
        }

        @Override
        public Collection<TagKey<Item>> getReplacedTags(TagKey<Item> substituteTag) {
            return List.of();
        }

        @Override
        public Set<TagKey<Item>> getReplacedTags() {
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


    public static UnificationLookup unificationLookup() {
        return new UnificationLookupImpl.Builder()
            .put(itemTag("testmod:test_tag"),
                ResourceLocation.parse("minecraft:test_item"),
                ResourceLocation.parse("mekanism:test_item"),
                ResourceLocation.parse("thermal:test_item"),
                ResourceLocation.parse("testmod:test_item"))
            .build(TEST_MOD_PRIORITIES, EMPTY_STONE_VARIANTS, EMPTY_TAG_SUBSTITUTIONS);
    }


    public static UnificationHelper recipeHelper() {
        return new UnificationHelperImpl(new CustomIngredientUnifierRegistryImpl(), unificationLookup());
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

    private static class CustomIngredientUnifierRegistryImpl implements CustomIngredientUnifierRegistry {

        @Override
        public void registerForType(ResourceLocation type, CustomIngredientUnifier customIngredientUnifier) {}

        @Nullable
        @Override
        public CustomIngredientUnifier getCustomIngredientUnifier(ResourceLocation type) {
            return null;
        }
    }
}
