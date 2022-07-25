package com.almostreliable.unified;

import com.almostreliable.unified.config.Defaults;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMapTests;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class TestUtils {
    public static final String TEST_MOD_1 = "test_mod_1";
    public static final String TEST_MOD_2 = "test_mod_2";
    public static final String TEST_MOD_3 = "test_mod_3";
    public static final String TEST_MOD_4 = "test_mod_4";
    public static final String TEST_MOD_5 = "test_mod_5";
    public static final List<String> TEST_MOD_PRIORITIES = List.of(TEST_MOD_1,
            TEST_MOD_2,
            TEST_MOD_3,
            TEST_MOD_4,
            TEST_MOD_5);
    public static UnifyConfig DEFAULT_UNIFY_CONFIG = new UnifyConfig(Defaults.STONE_STRATA,
            Defaults.MATERIALS,
            Defaults.TAGS,
            TestUtils.TEST_MOD_PRIORITIES,
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>());
    public static final ResourceKey<Registry<Item>> FAKE_ITEM_REGISTRY = FakeResourceKeyRegistry.create("item");
    public static final UnifyTag<Item> BRONZE_ORES_TAG = tag("forge:ores/bronze");
    public static final UnifyTag<Item> INVAR_ORES_TAG = tag("forge:ores/invar");
    public static final UnifyTag<Item> TIN_ORES_TAG = tag("forge:ores/tin");
    public static final UnifyTag<Item> SILVER_ORES_TAG = tag("forge:ores/silver");
    public static final List<UnifyTag<Item>> TEST_ALLOWED_TAGS = List.of(BRONZE_ORES_TAG,
            INVAR_ORES_TAG,
            TIN_ORES_TAG,
            SILVER_ORES_TAG);

    /**
     * ResourceKey is null because otherwise tests can't run because Minecraft is not bootstrapped ...
     *
     * @param name the name of the tag
     * @return a TagKey for the given name
     */
    public static UnifyTag<Item> tag(String name) {
        return UnifyTag.item(new ResourceLocation(name));
    }

    public static ResourceLocation mod1RL(String name) {
        return new ResourceLocation(TEST_MOD_1, name);
    }

    public static ResourceLocation mod2RL(String name) {
        return new ResourceLocation(TEST_MOD_2, name);
    }

    public static ResourceLocation mod3RL(String name) {
        return new ResourceLocation(TEST_MOD_3, name);
    }

    public static ResourceLocation mod4RL(String name) {
        return new ResourceLocation(TEST_MOD_4, name);
    }

    public static ResourceLocation mod5RL(String name) {
        return new ResourceLocation(TEST_MOD_5, name);
    }

    public static RecipeTransformer basicTransformer(Consumer<RecipeHandlerFactory> consumer) {
        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(), DEFAULT_UNIFY_CONFIG);
        RecipeHandlerFactory factory = new RecipeHandlerFactory();
        consumer.accept(factory);
        return new RecipeTransformer(factory, map, DEFAULT_UNIFY_CONFIG, duplicationConfig);
    }

    public static JsonObject json(String json) {
        return new Gson().fromJson(json, JsonObject.class);
    }

    public static JsonObject json(String json, Consumer<JsonObject> consumer) {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        consumer.accept(obj);
        return obj;
    }
}
