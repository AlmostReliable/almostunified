package com.almostreliable.unified.core;

import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.unification.recipe.RecipeLinkFactory;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ConditionalRecipeLinkFactory implements RecipeLinkFactory {

    private final ConditionalOps<JsonElement> conOps;
    private final Codec<Optional<Unit>> codec;
    private final boolean cache;

    public ConditionalRecipeLinkFactory(ConditionalOps<JsonElement> conOps, boolean cache) {
        this.conOps = conOps;
        this.codec = ConditionalOps.createConditionalCodec(Unit.CODEC);
        this.cache = cache;
    }

    @Nullable
    @Override
    public RecipeLink create(Identifier id, JsonObject originalRecipe) {
        var conditions = originalRecipe.get(ConditionalOps.DEFAULT_CONDITIONS_KEY);
        if (conditions == null) {
            return RecipeLink.of(id, originalRecipe);
        }

        try {
            boolean conditionsMet = codec.parse(conOps, originalRecipe).getOrThrow(JsonParseException::new).isPresent();
            if (cache) {
                var conJson = new JsonObject();
                conJson.addProperty("type", RecipeLoadCondition.ID);
                conJson.addProperty(RecipeLoadCondition.CONDITIONS_MET, conditionsMet);
                conJson.add("original_conditions", conditions);

                var arr = new JsonArray();
                arr.add(conJson);
                originalRecipe.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, arr);
            }

            if (!conditionsMet) {
                return null;
            }

            return RecipeLink.of(id, originalRecipe);
        } catch (IllegalArgumentException | JsonParseException e) {
            // If an exception is caught, mark the recipe link as invalid, so Almost Unified doesn't track it.
            // NeoForge should report a problem with the recipe the usual way.
            return null;
        }
    }
}
