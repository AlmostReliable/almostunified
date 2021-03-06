package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.JsonCompare;
import com.almostreliable.unified.utils.JsonQuery;
import com.almostreliable.unified.utils.ReplacementMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecipeTransformer {

    private final RecipeHandlerFactory factory;
    private final ReplacementMap replacementMap;
    private final UnifyConfig unifyConfig;
    private final DuplicationConfig duplicationConfig;

    public RecipeTransformer(RecipeHandlerFactory factory, ReplacementMap replacementMap, UnifyConfig unifyConfig, DuplicationConfig duplicationConfig) {
        this.factory = factory;
        this.replacementMap = replacementMap;
        this.unifyConfig = unifyConfig;
        this.duplicationConfig = duplicationConfig;
    }

    public boolean hasValidType(JsonObject json) {
        if (json.get("type") instanceof JsonPrimitive primitive) {
            ResourceLocation type = ResourceLocation.tryParse(primitive.getAsString());
            return type != null && unifyConfig.includeRecipeType(type);
        }
        return false;
    }

    /**
     * Transforms a map of recipes. This method will modify the map in-place. Part of the transformation is to unify recipes with the given {@link ReplacementMap}.
     * After unification, recipes will be checked for duplicates. All duplicates will be removed from the map.
     *
     * @param recipes The map of recipes to transform.
     * @return The result of the transformation.
     */
    public Result transformRecipes(Map<ResourceLocation, JsonElement> recipes) {
        AlmostUnified.LOG.warn("Recipe counts: " + recipes.size());

        Result result = new Result();
        Map<ResourceLocation, List<RecipeLink>> byType = groupRecipesByType(recipes);

        ResourceLocation fcLocation = new ResourceLocation("forge:conditional");
        byType.forEach((type, recipeLinks) -> {
            if (type.equals(fcLocation)) {
                recipeLinks.forEach(recipeLink -> handleForgeConditionals(recipeLink).ifPresent(json -> recipes.put(
                        recipeLink.getId(),
                        json)));
            } else {
                transformRecipes(recipeLinks, recipes::put, recipes::remove);
            }
        });

        byType.values().stream().flatMap(Collection::stream).forEach(result::add);
        AlmostUnified.LOG.warn("Recipe counts afterwards: " + recipes.size());
        return result;
    }

    private Optional<JsonObject> handleForgeConditionals(RecipeLink recipeLink) {
        JsonObject copy = recipeLink.getOriginal().deepCopy();

        if (copy.get("recipes") instanceof JsonArray recipes) {
            for (JsonElement element : recipes) {
                JsonQuery
                        .of(element, "recipe")
                        .asObject()
                        .map(jsonObject -> new RecipeLink(recipeLink.getId(), jsonObject))
                        .ifPresent(temporaryLink -> {
                            unifyRecipe(temporaryLink);
                            if (temporaryLink.isUnified()) {
                                element.getAsJsonObject().add("recipe", temporaryLink.getUnified());
                            }
                        });
            }

            if (!copy.equals(recipeLink.getOriginal())) {
                recipeLink.setUnified(copy);
                return Optional.of(copy);
            }
        }

        return Optional.empty();
    }

    private void transformRecipes(List<RecipeLink> recipeLinks, BiConsumer<ResourceLocation, JsonElement> onAdd, Consumer<ResourceLocation> onRemove) {
        Set<RecipeLink.DuplicateLink> duplicates = new HashSet<>(recipeLinks.size());
        for (RecipeLink curRecipe : recipeLinks) {
            unifyRecipe(curRecipe);
            if (curRecipe.isUnified()) {
                onAdd.accept(curRecipe.getId(), curRecipe.getUnified());
                if (handleDuplicate(curRecipe, recipeLinks)) {
                    duplicates.add(curRecipe.getDuplicateLink());
                }
            }
        }

        for (RecipeLink.DuplicateLink link : duplicates) {
            link.getRecipes().forEach(recipe -> onRemove.accept(recipe.getId()));
            onAdd.accept(link.createNewRecipeId(), link.getMaster().getActual());
        }
    }

    public Map<ResourceLocation, List<RecipeLink>> groupRecipesByType(Map<ResourceLocation, JsonElement> recipes) {
        return recipes
                .entrySet()
                .stream()
                .filter(entry -> includeRecipe(entry.getKey(), entry.getValue()))
                .map(entry -> new RecipeLink(entry.getKey(), entry.getValue().getAsJsonObject()))
                .collect(Collectors.groupingByConcurrent(RecipeLink::getType));
    }

    private boolean includeRecipe(ResourceLocation recipe, JsonElement json) {
        return unifyConfig.includeRecipe(recipe) && json.isJsonObject() && hasValidType(json.getAsJsonObject());
    }

    private boolean handleDuplicate(RecipeLink curRecipe, List<RecipeLink> recipes) {
        if (duplicationConfig.shouldIgnoreRecipe(curRecipe)) {
            return false;
        }

        if (curRecipe.getDuplicateLink() != null) {
            AlmostUnified.LOG.error("Duplication already handled for recipe {}", curRecipe.getId());
            return false;
        }

        JsonCompare.CompareSettings compareSettings = duplicationConfig.getCompareSettings(curRecipe.getType());
        for (RecipeLink recipeLink : recipes) {
            if (!curRecipe.getType().equals(recipeLink.getType())) {
                throw new IllegalStateException(
                        "Recipe types do not match for " + curRecipe.getId() + " and " + recipeLink.getId());
            }

            if (recipeLink == curRecipe || duplicationConfig.shouldIgnoreRecipe(recipeLink)) {
                continue;
            }

            if (curRecipe.handleDuplicate(recipeLink, compareSettings)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Unifies a single recipe link. This method will modify the recipe link in-place.
     * {@link RecipeHandlerFactory} will apply multiple unification's onto the recipe.
     *
     * @param recipe The recipe link to unify.
     */
    public void unifyRecipe(RecipeLink recipe) {
        try {
            RecipeContextImpl ctx = new RecipeContextImpl(recipe.getOriginal(), replacementMap);
            RecipeUnifierBuilderImpl builder = new RecipeUnifierBuilderImpl();
            factory.fillUnifier(builder, ctx);
            JsonObject result = builder.unify(recipe.getOriginal(), ctx);
            if (result != null) {
                recipe.setUnified(result);
            }
        } catch (Exception e) {
            AlmostUnified.LOG.warn("Error unifying recipe '{}': {}", recipe.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    public static class Result {
        private final Multimap<ResourceLocation, RecipeLink> allRecipesByType = HashMultimap.create();
        private final Multimap<ResourceLocation, RecipeLink> unifiedRecipesByType = HashMultimap.create();

        private final Multimap<ResourceLocation, RecipeLink.DuplicateLink> duplicatesByType = HashMultimap.create();

        private void add(RecipeLink link) {
            if (allRecipesByType.containsEntry(link.getType(), link)) {
                throw new IllegalStateException("Already tracking recipe type " + link.getType());
            }

            allRecipesByType.put(link.getType(), link);
            if (link.isUnified()) {
                unifiedRecipesByType.put(link.getType(), link);
            }

            if (link.hasDuplicateLink()) {
                duplicatesByType.put(link.getType(), link.getDuplicateLink());
            }
        }

        public Collection<RecipeLink> getRecipes(ResourceLocation type) {
            return Collections.unmodifiableCollection(allRecipesByType.get(type));
        }

        public Collection<RecipeLink> getUnifiedRecipes(ResourceLocation type) {
            return Collections.unmodifiableCollection(unifiedRecipesByType.get(type));
        }

        public Collection<RecipeLink.DuplicateLink> getDuplicates(ResourceLocation type) {
            return Collections.unmodifiableCollection(duplicatesByType.get(type));
        }

        public int getRecipeCount() {
            return allRecipesByType.size();
        }

        public int getUnifiedRecipeCount() {
            return unifiedRecipesByType.size();
        }

        public int getDuplicatesCount() {
            return duplicatesByType.size();
        }

        public int getDuplicateRecipesCount() {
            return duplicatesByType.values().stream().mapToInt(l -> l.getRecipes().size()).sum();
        }

        public Set<ResourceLocation> getUnifiedRecipeTypes() {
            return unifiedRecipesByType.keySet();
        }
    }
}
