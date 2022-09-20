package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.JsonCompare;
import com.almostreliable.unified.utils.JsonQuery;
import com.almostreliable.unified.utils.RecipeTypePropertiesLogger;
import com.almostreliable.unified.utils.ReplacementMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecipeTransformer {

    private final RecipeHandlerFactory factory;
    private final ReplacementMap replacementMap;
    private final UnifyConfig unifyConfig;
    private final DuplicationConfig duplicationConfig;

    private final RecipeTypePropertiesLogger propertiesLogger = new RecipeTypePropertiesLogger();

    public RecipeTransformer(RecipeHandlerFactory factory, ReplacementMap replacementMap, UnifyConfig unifyConfig, DuplicationConfig duplicationConfig) {
        this.factory = factory;
        this.replacementMap = replacementMap;
        this.unifyConfig = unifyConfig;
        this.duplicationConfig = duplicationConfig;
    }

    public boolean hasValidRecipeType(JsonObject json) {
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
        AlmostUnified.LOG.warn("Recipe count: " + recipes.size());

        ClientRecipeTracker.RawBuilder tracker = new ClientRecipeTracker.RawBuilder();
        Result result = new Result();
        Map<ResourceLocation, List<RecipeLink>> byType = groupRecipesByType(recipes);

        ResourceLocation fcLocation = new ResourceLocation("forge:conditional");
        byType.forEach((type, recipeLinks) -> {
            if (type.equals(fcLocation)) {
                recipeLinks.forEach(recipeLink -> handleForgeConditionals(recipeLink).ifPresent(json -> recipes.put(
                        recipeLink.getId(),
                        json)));
            } else {
                transformRecipes(recipeLinks, recipes, tracker);
            }
            result.addAll(recipeLinks);
        });

        AlmostUnified.LOG.warn("Recipe count afterwards: " + recipes.size());
        Map<ResourceLocation, JsonObject> compute = tracker.compute();
        recipes.putAll(compute);
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

    /**
     * Transforms a list of recipes. Part of the transformation is to unify recipes with the given {@link ReplacementMap}.
     * After unification, recipes will be checked for duplicates.
     * All duplicates will be removed from <b>{@code Map<ResourceLocation, JsonElement> allRecipes}</b>,
     * while unified recipes will replace the original recipes in the map.
     * <p>
     * This method will also add the recipes to the given {@link ClientRecipeTracker.RawBuilder}
     *
     * @param recipeLinks The list of recipes to transform.
     * @param allRecipes  The map of all existing recipes.
     * @param tracker     The tracker to add the recipes to.
     */
    private void transformRecipes(List<RecipeLink> recipeLinks, Map<ResourceLocation, JsonElement> allRecipes, ClientRecipeTracker.RawBuilder tracker) {
        var unified = unifyRecipes(recipeLinks, (r) -> allRecipes.put(r.getId(), r.getUnified()));
        var duplicates = handleDuplicates(duplicationConfig.isStrictMode() ? recipeLinks : unified, recipeLinks);
        duplicates
                .stream()
                .flatMap(d -> d.getRecipesWithoutMaster().stream())
                .forEach(r -> allRecipes.remove(r.getId()));
        unified.forEach(tracker::add);
    }

    public Map<ResourceLocation, List<RecipeLink>> groupRecipesByType(Map<ResourceLocation, JsonElement> recipes) {
        return recipes
                .entrySet()
                .stream()
                .filter(entry -> includeRecipe(entry.getKey(), entry.getValue()))
                .map(entry -> new RecipeLink(entry.getKey(), entry.getValue().getAsJsonObject()))
                .collect(Collectors.groupingByConcurrent(RecipeLink::getType));
    }

    /**
     * Checks if a recipe should be included in the transformation.
     *
     * @param recipe The recipe to check.
     * @param json   The recipe's json. Will be used to check if the recipe has a valid type.
     * @return True if the recipe should be included, false otherwise.
     */
    private boolean includeRecipe(ResourceLocation recipe, JsonElement json) {
        return unifyConfig.includeRecipe(recipe) && json.isJsonObject() && hasValidRecipeType(json.getAsJsonObject());
    }

    /**
     * Compares a list of recipes against another list for duplicates.
     *
     * @param recipeLinks    The list of recipes
     * @param linksToCompare The list of recipes to compare against
     * @return A list of {@link RecipeLink.DuplicateLink}s containing all duplicates.
     */
    private Set<RecipeLink.DuplicateLink> handleDuplicates(Collection<RecipeLink> recipeLinks, List<RecipeLink> linksToCompare) {
        Set<RecipeLink.DuplicateLink> duplicates = new HashSet<>(recipeLinks.size());
        for (RecipeLink recipeLink : recipeLinks) {
            if (handleDuplicate(recipeLink, linksToCompare) && recipeLink.getDuplicateLink() != null) {
                duplicates.add(recipeLink.getDuplicateLink());
            }
        }
        return duplicates;
    }

    private boolean handleDuplicate(RecipeLink curRecipe, List<RecipeLink> recipes) {
        if (duplicationConfig.shouldIgnoreRecipe(curRecipe)) {
            return false;
        }

        JsonCompare.CompareSettings compareSettings = duplicationConfig.getCompareSettings(curRecipe.getType());
        boolean foundDuplicate = false;
        for (RecipeLink recipeLink : recipes) {
            if (!curRecipe.getType().equals(recipeLink.getType())) {
                throw new IllegalStateException(
                        "Recipe types do not match for " + curRecipe.getId() + " and " + recipeLink.getId());
            }

            if (recipeLink == curRecipe || duplicationConfig.shouldIgnoreRecipe(recipeLink)) {
                continue;
            }

            foundDuplicate |= curRecipe.handleDuplicate(recipeLink, compareSettings);
        }

        return foundDuplicate;
    }

    /**
     * Unifies a list of recipes. On unification, {@code Consumer<RecipeLink>} will be called
     *
     * @param recipeLinks The list of recipes to unify.
     * @param onUnified   A consumer that will be called on each unified recipe.
     * @return A list of unified recipes.
     */
    private Set<RecipeLink> unifyRecipes(List<RecipeLink> recipeLinks, Consumer<RecipeLink> onUnified) {
        Set<RecipeLink> unified = new HashSet<>(recipeLinks.size());
        for (RecipeLink recipeLink : recipeLinks) {
            unifyRecipe(recipeLink);
            if (recipeLink.isUnified()) {
                onUnified.accept(recipeLink);
                unified.add(recipeLink);
            }
        }
        return unified;
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
            propertiesLogger.log(recipe.getType(), recipe.getOriginal(), builder.getKeys());
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

        private void addAll(Collection<RecipeLink> links) {
            links.forEach(this::add);
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
