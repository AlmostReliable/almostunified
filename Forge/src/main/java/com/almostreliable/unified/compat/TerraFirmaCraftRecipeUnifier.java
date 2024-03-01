package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.List;

public class TerraFirmaCraftRecipeUnifier implements RecipeUnifier {

    public static final String ITEM_OUTPUT = "item_output";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(
                // barrel_sealed, vat
                RecipeConstants.INPUT_ITEM,
                // blast_furnace, bloomery
                RecipeConstants.CATALYST,
                // casting
                RecipeConstants.MOLD,
                // advanced shapeless crafting
                RecipeConstants.PRIMARY_INGREDIENT,
                // glassworking
                RecipeConstants.BATCH,
                // power_loom
                RecipeConstants.INPUTS,
                RecipeConstants.SECONDARY_INPUT,
                // welding
                RecipeConstants.FIRST_INPUT,
                RecipeConstants.SECOND_INPUT
        ).forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(
                json,
                RecipeConstants.VALUE,
                RecipeConstants.BASE,
                RecipeConstants.INGREDIENT,
                RecipeConstants.BASE_INGREDIENT
        )));

        List.of(
                // beamhouse, advanced shapeless crafting, fleshing_machine, grist_mill, thresher
                RecipeConstants.RESULT,
                // filling
                RecipeConstants.RESULTS,
                // barrel_sealed, mixing_bowl, vat
                RecipeConstants.OUTPUT_ITEM,
                // chisel, scraping
                RecipeConstants.EXTRA_DROP,
                // heating, oven
                RecipeConstants.RESULT_ITEM,
                // pot
                ITEM_OUTPUT,
                // power_loom, thresher
                RecipeConstants.SECONDARIES,
                // extra products shapeless crafting
                RecipeConstants.EXTRA_PRODUCTS
        ).forEach(key -> builder.put(key, this::createResultReplacement));
    }

    @Nullable
    private JsonElement createResultReplacement(JsonElement json, RecipeContext ctx) {
        return ctx.createResultReplacement(
                json,
                false,
                RecipeConstants.ITEM,
                RecipeConstants.STACK,
                RecipeConstants.OUTPUT,
                RecipeConstants.BASE_INGREDIENT
        );
    }
}
