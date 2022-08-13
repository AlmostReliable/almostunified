package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
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
            StringBuilder overviewBuilder = new StringBuilder();
            overviewBuilder.append(last).append("\n");
            dumpOverview(overviewBuilder);
            write(overviewBuilder, AlmostUnifiedPlatform.INSTANCE.getLogPath(), "overview_dump.txt");
        }

        if (dumpUnify) {
            StringBuilder unifyStringBuilder = new StringBuilder();
            unifyStringBuilder.append(last).append("\n");
            dumpUnifyRecipes(unifyStringBuilder, true);
            write(unifyStringBuilder, AlmostUnifiedPlatform.INSTANCE.getLogPath(), "unify_dump.txt");
        }

        if (dumpDuplicate) {
            StringBuilder duplicatesStringBuilder = new StringBuilder();
            duplicatesStringBuilder.append(last).append("\n");
            dumpDuplicates(duplicatesStringBuilder);
            write(duplicatesStringBuilder, AlmostUnifiedPlatform.INSTANCE.getLogPath(), "duplicates_dump.txt");
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

            stringBuilder.append(type).append(" {\n");
            duplicates.forEach(link -> {
                stringBuilder
                        .append("\t")
                        .append(link.getMaster().getId())
                        .append(" (Renamed to: ")
                        .append(link.createNewRecipeId())
                        .append(")\n");
                link.getRecipes().stream().sorted(Comparator.comparing(r -> r.getId().toString())).forEach(recipe -> {
                    stringBuilder.append("\t\t- ").append(recipe.getId()).append("\n");
                });
                stringBuilder.append("\n");
            });
            stringBuilder.append("}\n\n");
        });
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

    private void dumpUnifyRecipes(StringBuilder stringBuilder, boolean full) {
        getSortedUnifiedRecipeTypes().forEach(type -> {
            stringBuilder.append(type.toString()).append(" {\n");

            getSortedUnifiedRecipes(type).forEach(recipe -> {
                stringBuilder.append("\t- ").append(recipe.getId()).append("\n");
                if (full) {
                    stringBuilder
                            .append("\t\t    Original: ")
                            .append(recipe.getOriginal().toString())
                            .append("\n");
                    stringBuilder
                            .append("\t\t Transformed: ")
                            .append(recipe.getUnified().toString())
                            .append("\n\n");
                }
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

    private void write(StringBuilder stringBuilder, Path path, String fileName) {
        try {
            Files.createDirectories(path);
            Path filePath = path.resolve(fileName);
            Files.writeString(filePath,
                    stringBuilder.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getTotalTime() {
        return endTime - startTime;
    }


}
