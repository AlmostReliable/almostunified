package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeTransformer {

    private final RecipeHandlerFactory factory;
    private final ReplacementMap replacementMap;

    public RecipeTransformer(RecipeHandlerFactory factory, ReplacementMap replacementMap) {
        this.factory = factory;
        this.replacementMap = replacementMap;
    }

    public boolean hasValidType(JsonObject json) {
        if (json.get("type") instanceof JsonPrimitive type) {
            return ResourceLocation.tryParse(type.getAsString()) != null;
        }
        return false;
    }

    /**
     * Transforms a map of recipes. This method will modify the map in-place. Part of the transformation is to unify recipes with the given {@link ReplacementMap}.
     * After unification, recipes will be checked for duplicates. All duplicates will be removed from the map.
     *
     * @param recipes The map of recipes to transform.
     * @return
     */
    public RecipeTransformationResult transformRecipes(Map<ResourceLocation, JsonElement> recipes) {
        AlmostUnified.LOG.warn("Recipe counts: " + recipes.size());
        RecipeTransformationResult rtr = new RecipeTransformationResult();
        Map<ResourceLocation, List<RecipeLink>> byType = groupRecipesByType(recipes);
        byType.forEach((type, recipeLinks) -> {
            Set<RecipeLink.DuplicateLink> duplicates = new HashSet<>(recipeLinks.size());
            for (RecipeLink curRecipe : recipeLinks) {
                transformRecipe(curRecipe);
                if (curRecipe.isTransformed()) {
                    recipes.put(curRecipe.getId(), curRecipe.getTransformed());
                    if (handleDuplicate(curRecipe, recipeLinks)) {
                        duplicates.add(curRecipe.getDuplicateLink());
                    }
                }
                rtr.track(curRecipe); // TODO remove
            }

            for (RecipeLink.DuplicateLink link : duplicates) {
                link.getRecipes().forEach(recipe -> recipes.remove(recipe.getId()));
                recipes.put(link.createNewRecipeId(), link.getMaster().getActual());
            }
        });
        rtr.end();
        AlmostUnified.LOG.warn("Recipe counts afterwards: " + recipes.size());
        return rtr;
    }

    private Map<ResourceLocation, List<RecipeLink>> groupRecipesByType(Map<ResourceLocation, JsonElement> recipes) {
        return recipes
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isJsonObject() && hasValidType(entry.getValue().getAsJsonObject()))
                .map(entry -> new RecipeLink(entry.getKey(), entry.getValue().getAsJsonObject()))
                .collect(Collectors.groupingByConcurrent(RecipeLink::getType));
    }

    private boolean handleDuplicate(RecipeLink curRecipe, List<RecipeLink> recipes) {
        if (curRecipe.getDuplicateLink() != null) {
            AlmostUnified.LOG.error("Duplication already handled for recipe {}", curRecipe.getId());
            return false;
        }

        for (RecipeLink recipeLink : recipes) {
            if (recipeLink == curRecipe) {
                continue;
            }

            if (curRecipe.handleDuplicate(recipeLink)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Transforms a single recipe link. This method will modify the recipe link in-place.
     * {@link RecipeHandlerFactory} will apply multiple transformations onto the recipe.
     * @param recipe The recipe link to transform.
     */
    public void transformRecipe(RecipeLink recipe) {
        try {
            RecipeContextImpl ctx = new RecipeContextImpl(recipe.getOriginal(), replacementMap);
            RecipeTransformationBuilderImpl builder = new RecipeTransformationBuilderImpl();
            factory.fillTransformations(builder, ctx);
            JsonObject result = builder.transform(recipe.getOriginal(), ctx);
            if (result != null) {
                recipe.setTransformed(result);
            }
        } catch (Exception e) {
            AlmostUnified.LOG.warn("Error transforming recipe '{}': {}",
                    recipe.getId(),
                    e.getMessage());
            e.printStackTrace();
        }
    }
}
