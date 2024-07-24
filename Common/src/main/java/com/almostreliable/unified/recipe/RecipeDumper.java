package com.almostreliable.unified.recipe;

import com.almostreliable.unified.utils.FileUtils;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeDumper {
    private final RecipeTransformer.Result result;
    private final long startTime;
    private final long endTime;
    private final DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public RecipeDumper(RecipeTransformer.Result result, long startTime, long endTime) {
        this.result = result;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void dump(boolean dumpOverview, boolean dumpUnify, boolean dumpDuplicate) {
        String last = "# Last execution: " + format.format(new Date(startTime));

        if (dumpOverview) {
            FileUtils.writeLog("overview_dump.txt", sb -> {
                sb.append(last).append("\n");
                dumpOverview(sb);
            });
        }

        if (dumpUnify) {
            FileUtils.writeLog("unify_dump.txt", sb -> {
                sb.append(last).append("\n");
                dumpUnifyRecipes(sb);
            });
        }

        if (dumpDuplicate) {
            FileUtils.writeLog("duplicates_dump.txt", sb -> {
                sb.append(last).append("\n");
                dumpDuplicates(sb);
            });
        }

    }

    private void dumpDuplicates(StringBuilder stringBuilder) {
        getSortedUnifiedRecipeTypes().forEach(type -> {
            Collection<RecipeLink.DuplicateLink> duplicates = result
                    .getDuplicates(type)
                    .stream()
                    .sorted(Comparator.comparing(l -> l.getMaster().getId().toString()))
                    .toList();
            if (duplicates.isEmpty()) return;

            stringBuilder.append(duplicates
                    .stream()
                    .map(this::createDuplicatesDump)
                    .collect(Collectors.joining("", type + " {\n", "}\n\n")));
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

    private void dumpOverview(StringBuilder stringBuilder) {
        stringBuilder
                .append("# Overview:\n")
                .append("\t- Unified: ")
                .append(result.getUnifiedRecipeCount())
                .append("\n")
                .append("\t- Duplicates: ")
                .append(result.getDuplicatesCount())
                .append(" (Individual: ")
                .append(result.getDuplicateRecipesCount())
                .append(")\n")
                .append("\t- Total Recipes: ")
                .append(result.getRecipeCount())
                .append("\n")
                .append("\t- Elapsed Time: ")
                .append(getTotalTime())
                .append("ms")
                .append("\n\n")
                .append("# Summary: \n");

        stringBuilder
                .append(rf("Recipe type", 45))
                .append(" | ")
                .append(lf("Unifies", 10))
                .append(" | ")
                .append(lf("Duplicates", 10))
                .append(" | ")
                .append(lf("All", 5))
                .append("\n")
                .append(StringUtils.repeat("-", 45 + 10 + 10 + 5 + 9))
                .append("\n");

        getSortedUnifiedRecipeTypes().forEach(type -> {
            int unifiedSize = result.getUnifiedRecipes(type).size();
            int allSize = result.getRecipes(type).size();
            int duplicatesSize = result.getDuplicates(type).size();
            int individualDuplicatesSize = result
                    .getDuplicates(type)
                    .stream()
                    .mapToInt(l -> l.getRecipes().size())
                    .sum();

            String dStr = String.format("%s (%s)", lf(duplicatesSize, 3), lf(individualDuplicatesSize, 3));
            stringBuilder
                    .append(rf(type, 45))
                    .append(" | ")
                    .append(lf(unifiedSize, 10))
                    .append(" | ")
                    .append(lf(duplicatesSize == 0 ? " " : dStr, 10))
                    .append(" | ")
                    .append(lf(allSize, 5))
                    .append("\n");
        });
    }

    private void dumpUnifyRecipes(StringBuilder stringBuilder) {
        getSortedUnifiedRecipeTypes().forEach(type -> {
            stringBuilder.append(type.toString()).append(" {\n");

            getSortedUnifiedRecipes(type).forEach(recipe -> {
                stringBuilder
                        .append("\t- ")
                        .append(recipe.getId())
                        .append("\n")
                        .append("\t\t    Original: ")
                        .append(recipe.getOriginal().toString())
                        .append("\n")
                        .append("\t\t Transformed: ")
                        .append(recipe.getUnified() == null ? "NOT UNIFIED" : recipe.getUnified().toString())
                        .append("\n\n");
            });

            stringBuilder.append("}\n\n");
        });
    }

    private String rf(Object v, int size) {
        return StringUtils.rightPad(v.toString(), size);
    }

    private String lf(Object v, int size) {
        return StringUtils.leftPad(v.toString(), size);
    }

    private Stream<ResourceLocation> getSortedUnifiedRecipeTypes() {
        return result.getUnifiedRecipeTypes().stream().sorted(Comparator.comparing(ResourceLocation::toString));
    }

    private Stream<RecipeLink> getSortedUnifiedRecipes(ResourceLocation type) {
        return result.getUnifiedRecipes(type).stream().sorted(Comparator.comparing(r -> r.getId().toString()));
    }

    private long getTotalTime() {
        return endTime - startTime;
    }
}
