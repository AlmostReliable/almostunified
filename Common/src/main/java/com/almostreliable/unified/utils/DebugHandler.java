package com.almostreliable.unified.utils;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.unification.recipe.RecipeTransformer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;

import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DebugHandler {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DUPLICATES = "duplicates.txt";
    private static final String OVERVIEW = "overview.txt";
    private static final String RECIPES_AFTER = "recipes_after.txt";
    private static final String RECIPES_BEFORE = "recipes_before.txt";
    private static final String TAGS = "tags.txt";
    private static final String UNIFICATION = "unification.txt";

    private final DebugConfig config;
    private final String lastRun;
    private final int recipesBefore;

    private long startTime;
    private long endTime;
    @Nullable private RecipeTransformer.Result transformerResult;

    private DebugHandler(int recipesBefore, DebugConfig config) {
        this.config = config;
        this.lastRun = "# Last run: " + DATE_FORMAT.format(new Date(System.currentTimeMillis()));
        this.recipesBefore = recipesBefore;
    }

    public static DebugHandler onRunStart(Map<ResourceLocation, JsonElement> recipes, UnificationLookup unificationLookup, DebugConfig config) {
        DebugHandler handler = new DebugHandler(recipes.size(), config);
        handler.dumpTags(unificationLookup);
        handler.dumpRecipes(RECIPES_BEFORE, recipes);
        return handler;
    }

    public void measure(Supplier<RecipeTransformer.Result> transformerSupplier) {
        startTime = System.currentTimeMillis();
        transformerResult = transformerSupplier.get();
        endTime = System.currentTimeMillis();
    }

    public void onRunEnd(Map<ResourceLocation, JsonElement> recipes) {
        Preconditions.checkArgument(startTime > 0, "startTime not set");
        Preconditions.checkArgument(endTime > 0, "endTime not set");
        Preconditions.checkNotNull(transformerResult, "transformerResult not set");

        dumpRecipes(RECIPES_AFTER, recipes);
        dumpOverview(recipes.size());
        dumpUnification();
        dumpDuplicates();
    }

    private void dumpTags(UnificationLookup unificationLookup) {
        if (!config.shouldDumpTags()) return;

        int maxLength = getMaxLength(unificationLookup.getTags(), t -> t.location().toString().length());

        FileUtils.writeDebugLog(TAGS, sb -> sb
            .append(lastRun).append("\n")
            .append(unificationLookup
                .getTags()
                .stream()
                .map(t -> rf(t.location(), maxLength) + " => " +
                          unificationLookup
                              .getTagEntries(t)
                              .stream()
                              .map(entry -> entry.id().toString())
                              .sorted()
                              .collect(Collectors.joining(", ")) + "\n"
                )
                .sorted()
                .collect(Collectors.joining())
            ));
    }

    private void dumpRecipes(String fileName, Map<ResourceLocation, JsonElement> recipes) {
        if (!config.shouldDumpRecipes()) return;

        int maxLength = getMaxLength(recipes.keySet(), id -> id.toString().length());

        FileUtils.writeDebugLog(fileName, sb -> sb
            .append(lastRun).append("\n")
            .append(recipes
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> rf(e.getKey(), maxLength) + " => " + e.getValue().toString())
                .collect(Collectors.joining("\n")))
        );
    }

    private void dumpOverview(int recipesAfter) {
        if (!config.shouldDumpOverview()) return;
        assert transformerResult != null;

        int maxLength = getMaxLength(transformerResult.getUnifiedRecipeTypes(), t -> t.toString().length());

        FileUtils.writeDebugLog(OVERVIEW, sb -> {
            sb
                .append(lastRun).append("\n")
                .append("# Statistics:\n")
                .append("- Unified Recipes: ")
                .append(transformerResult.getUnifiedRecipeCount())
                .append("\n")
                .append("- Duplicate Recipes: ")
                .append(transformerResult.getDuplicatesCount())
                .append(" (Individual: ")
                .append(transformerResult.getDuplicateRecipesCount())
                .append(")\n")
                .append("- Recipes Before: ")
                .append(recipesBefore)
                .append("\n")
                .append("- Recipes After: ")
                .append(recipesAfter)
                .append("\n")
                .append("- Elapsed Time: ")
                .append(endTime - startTime)
                .append(" ms")
                .append("\n\n")
                .append("# Summary:\n")
                .append(rf("Recipe type", maxLength))
                .append(" | ")
                .append(lf("Unified", 10))
                .append(" | ")
                .append(lf("Duplicates", 10))
                .append(" | ")
                .append(lf("All", 5))
                .append("\n")
                .append(StringUtils.repeat("-", maxLength + 10 + 10 + 5 + 9))
                .append("\n");

            getSortedUnifiedRecipeTypes().forEach(type -> {
                int unifiedSize = transformerResult.getUnifiedRecipes(type).size();
                int allSize = transformerResult.getRecipes(type).size();
                int duplicatesSize = transformerResult.getDuplicates(type).size();
                int individualDuplicatesSize = transformerResult
                    .getDuplicates(type)
                    .stream()
                    .mapToInt(l -> l.getRecipes().size())
                    .sum();

                String dStr = String.format("%s (%s)", lf(duplicatesSize, 3), lf(individualDuplicatesSize, 3));
                sb
                    .append(rf(type, maxLength))
                    .append(" | ")
                    .append(lf(unifiedSize, 10))
                    .append(" | ")
                    .append(lf(duplicatesSize == 0 ? " " : dStr, 10))
                    .append(" | ")
                    .append(lf(allSize, 5))
                    .append("\n");
            });
        });
    }

    private void dumpUnification() {
        if (!config.shouldDumpUnification()) return;

        FileUtils.writeDebugLog(UNIFICATION, sb -> {
            sb.append(lastRun).append("\n");
            getSortedUnifiedRecipeTypes().forEach(type -> {
                sb.append(type.toString()).append(" {\n");

                getSortedUnifiedRecipes(type).forEach(recipe -> {
                    sb
                        .append("\t- ")
                        .append(recipe.getId())
                        .append("\n")
                        .append("\t\t    Original: ")
                        .append(recipe.getOriginal())
                        .append("\n")
                        .append("\t\t Transformed: ")
                        .append(recipe.getUnified() == null ? "NOT UNIFIED" : recipe.getUnified().toString())
                        .append("\n\n");
                });

                sb.append("}\n\n");
            });
        });
    }

    private void dumpDuplicates() {
        if (!config.shouldDumpDuplicates()) return;
        assert transformerResult != null;

        FileUtils.writeDebugLog(DUPLICATES, sb -> {
            sb.append(lastRun).append("\n");
            getSortedUnifiedRecipeTypes().forEach(type -> {
                Collection<RecipeLink.DuplicateLink> duplicates = transformerResult
                    .getDuplicates(type)
                    .stream()
                    .sorted(Comparator.comparing(l -> l.getMaster().getId().toString()))
                    .toList();
                if (duplicates.isEmpty()) return;

                sb.append(duplicates
                    .stream()
                    .map(this::createDuplicatesDump)
                    .collect(Collectors.joining("", type + " {\n", "}\n\n")));
            });
        });
    }

    private String createDuplicatesDump(RecipeLink.DuplicateLink link) {
        return link
            .getRecipes()
            .stream()
            .sorted(Comparator.comparing(r -> r.getId().toString()))
            .map(r -> "\t\t- " + r.getId() + "\n")
            .collect(Collectors.joining("", String.format("\t%s\n", link.getMaster().getId().toString()), "\n"));
    }

    private static <T> int getMaxLength(Collection<T> collection, ToIntFunction<T> function) {
        return collection.stream().mapToInt(function).max().orElse(0);
    }

    private static String rf(Object v, int size) {
        return StringUtils.rightPad(v.toString(), size);
    }

    private String lf(Object v, int size) {
        return StringUtils.leftPad(v.toString(), size);
    }

    private Stream<ResourceLocation> getSortedUnifiedRecipeTypes() {
        Preconditions.checkNotNull(transformerResult);
        return transformerResult
            .getUnifiedRecipeTypes()
            .stream()
            .sorted(Comparator.comparing(ResourceLocation::toString));
    }

    private Stream<RecipeLink> getSortedUnifiedRecipes(ResourceLocation type) {
        Preconditions.checkNotNull(transformerResult);
        return transformerResult
            .getUnifiedRecipes(type)
            .stream()
            .sorted(Comparator.comparing(r -> r.getId().toString()));
    }
}
