package com.almostreliable.unified;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.unification.recipe.RecipeLinkFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

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
    public RecipeLink create(ResourceLocation id, JsonObject recipe) {
        var conditions = recipe.get(ConditionalOps.DEFAULT_CONDITIONS_KEY);
        if (conditions == null) {
            return RecipeLink.of(id, recipe);
        }

        try {
            boolean conditionsMet = codec.parse(conOps, recipe).getOrThrow(JsonParseException::new).isPresent();
            if (cache) {
                var conJson = new JsonObject();
                conJson.addProperty("type", RecipeLoadCondition.ID);
                conJson.addProperty(RecipeLoadCondition.CONDITIONS_MET, conditionsMet);
                conJson.add("original_conditions", conditions);

                var arr = new JsonArray();
                arr.add(conJson);
                recipe.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, arr);
            }

            if (!conditionsMet) {
                return null;
            }

            return RecipeLink.of(id, recipe);
        } catch (IllegalArgumentException | JsonParseException e) {
            // Do we silent here? So the neoforge handling will throw the correct exception.
            // We should not do this.
            return null;
        }
    }
}
