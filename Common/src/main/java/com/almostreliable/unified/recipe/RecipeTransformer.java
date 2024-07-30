package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.RecipeUnifierRegistry;
import com.almostreliable.unified.api.UnifyHandler;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.api.UnifySettings;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.config.DuplicateConfig;
import com.almostreliable.unified.recipe.unifier.RecipeUnifierRegistryImpl;
import com.almostreliable.unified.utils.JsonCompare;
import com.almostreliable.unified.utils.JsonQuery;
import com.almostreliable.unified.utils.RecipeTypePropertiesLogger;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecipeTransformer {

    private final RecipeUnifierRegistry factory;
    private final Collection<? extends UnifyHandler> unifyHandlers;
    private final DuplicateConfig duplicateConfig;
    private final RecipeTypePropertiesLogger propertiesLogger = new RecipeTypePropertiesLogger();

    public RecipeTransformer(RecipeUnifierRegistry factory, Collection<? extends UnifyHandler> unifyHandlers, DuplicateConfig duplicateConfig) {
        this.factory = factory;
        this.unifyHandlers = unifyHandlers;
        this.duplicateConfig = duplicateConfig;
    }

    /**
     * Transforms a map of recipes. This method will modify the map in-place. Part of the transformation is to unify recipes with the given {@link UnifyLookup}.
     * After unification, recipes will be checked for duplicates. All duplicates will be removed from the map.
     *
     * @param recipes            The map of recipes to transform.
     * @param skipClientTracking Whether to skip client tracking for the recipes.
     * @return The result of the transformation.
     */
    public Result transformRecipes(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        Stopwatch transformationTimer = Stopwatch.createStarted();
        AlmostUnified.LOGGER.info("Recipe count: {}", recipes.size());

        ClientRecipeTracker.RawBuilder tracker = skipClientTracking ? null : new ClientRecipeTracker.RawBuilder();
        Result result = new Result();
        Map<ResourceLocation, List<RecipeLink>> byType = groupRecipesByType(recipes);

        // TODO: remove
        ResourceLocation fcLocation = ResourceLocation.parse("forge:conditional");
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

        AlmostUnified.LOGGER.info(
                "Recipe count afterwards: {} (done in {})",
                recipes.size(),
                transformationTimer.stop()
        );

        unifyHandlers.forEach(UnifySettings::clearCache);
        duplicateConfig.clearCache();

        if (tracker != null) recipes.putAll(tracker.compute());
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
     * Transforms a list of recipes. Part of the transformation is to unify recipes with the given {@link UnifyLookup}.
     * After unification, recipes will be checked for duplicates.
     * All duplicates will be removed from <b>{@code Map<ResourceLocation, JsonElement> allRecipes}</b>,
     * while unified recipes will replace the original recipes in the map.
     * <p>
     * This method will also add the recipes to the given {@link ClientRecipeTracker.RawBuilder}
     *
     * @param recipeLinks The list of recipes to transform.
     * @param allRecipes  The map of all existing recipes.
     * @param tracker     The tracker to add the recipes to. Can be null in a server only environment.
     */
    private void transformRecipes(List<RecipeLink> recipeLinks, Map<ResourceLocation, JsonElement> allRecipes, @Nullable ClientRecipeTracker.RawBuilder tracker) {
        var unified = unifyRecipes(recipeLinks, r -> allRecipes.put(r.getId(), r.getUnified()));
        var duplicates = handleDuplicates(duplicateConfig.isStrictMode() ? recipeLinks : unified, recipeLinks);
        duplicates
                .stream()
                .flatMap(d -> d.getRecipesWithoutMaster().stream())
                .forEach(r -> allRecipes.remove(r.getId()));
        if (tracker != null) unified.forEach(tracker::add);
    }

    public Map<ResourceLocation, List<RecipeLink>> groupRecipesByType(Map<ResourceLocation, JsonElement> recipes) {
        return recipes
                .entrySet()
                .stream()
                .map(entry -> new RecipeLink(entry.getKey(), entry.getValue().getAsJsonObject()))
                .sorted(Comparator.comparing(entry -> entry.getId().toString()))
                .collect(Collectors.groupingByConcurrent(RecipeLink::getType));
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
        if (duplicateConfig.shouldIgnoreRecipe(curRecipe)) {
            return false;
        }

        JsonCompare.CompareSettings compareSettings = duplicateConfig.getCompareSettings(curRecipe.getType());
        boolean foundDuplicate = false;
        for (RecipeLink recipeLink : recipes) {
            if (!curRecipe.getType().equals(recipeLink.getType())) {
                throw new IllegalStateException(
                        "Recipe types do not match for " + curRecipe.getId() + " and " + recipeLink.getId());
            }

            if (recipeLink == curRecipe || duplicateConfig.shouldIgnoreRecipe(recipeLink)) {
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
    private LinkedHashSet<RecipeLink> unifyRecipes(List<RecipeLink> recipeLinks, Consumer<RecipeLink> onUnified) {
        LinkedHashSet<RecipeLink> unified = new LinkedHashSet<>(recipeLinks.size());
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
     * {@link RecipeUnifierRegistryImpl} will apply multiple unification's onto the recipe.
     *
     * @param recipe The recipe link to unify.
     */
    public void unifyRecipe(RecipeLink recipe) {
        try {
            JsonObject recipeCopy = recipe.getOriginal().deepCopy();
            RecipeJson json = new RecipeJsonImpl(recipe.getId(), recipeCopy);

            for (var handler : unifyHandlers) {
                if (!handler.shouldIncludeRecipe(recipe)) {
                    continue;
                }

                RecipeContextImpl ctx = new RecipeContextImpl(handler);
                RecipeUnifier unifier = factory.getRecipeUnifier(recipe);
                unifier.unify(ctx, json);
            }

            if (!recipe.getOriginal().equals(recipeCopy)) {
                recipe.setUnified(recipeCopy);
            }

            propertiesLogger.log(recipe.getType(), recipe.getOriginal());
        } catch (Exception e) {
            AlmostUnified.LOGGER.error("Error unifying recipe '{}'", recipe.getId(), e);
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
