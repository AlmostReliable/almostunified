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

    public void dump() {
        StringBuilder stringBuilder = new StringBuilder();
        dumpOverview(stringBuilder);
        stringBuilder.append("\n\n# Transformed Recipes:\n");
        dumpTransformedRecipes(stringBuilder, true);
        write(stringBuilder, AlmostUnifiedPlatform.INSTANCE.getLogPath(), "dump.txt");
    }

    private void dumpOverview(StringBuilder stringBuilder) {
        stringBuilder
                .append("# Last execution: ")
                .append(format.format(new Date(startTime)))
                .append("\n")
                .append("# Overview:\n")
                .append("\t- Transformed: ")
                .append(result.getUnifiedRecipeCount())
                .append("\n")
                .append("\t- Total Recipes: ")
                .append(result.getRecipeCount())
                .append("\n")
                .append("\t- Elapsed Time: ")
                .append(getTotalTime())
                .append("ms")
                .append("\n\n")
                .append("# Summary: \n");

        getSortedUnifiedRecipeTypes().forEach(type -> {
            int unifiedSize = result.getUnifiedRecipes(type).size();
            int allSize = result.getRecipes(type).size();

            stringBuilder
                    .append(StringUtils.rightPad(type.toString(), 45))
                    .append(" = ")
                    .append(StringUtils.leftPad(String.valueOf(unifiedSize), 5))
                    .append(" (")
                    .append(allSize)
                    .append(")\n");
        });
    }

    private Stream<ResourceLocation> getSortedUnifiedRecipeTypes() {
        return result.getUnifiedRecipeTypes().stream().sorted(Comparator.comparing(ResourceLocation::toString));
    }

    private void dumpTransformedRecipes(StringBuilder stringBuilder, boolean full) {
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
//        result.forEachTransformedRecipe((type, recipes) -> {
//            stringBuilder.append(type.toString()).append(" {\n");
//            recipes.entrySet()
//                    .stream()
//                    .sorted(Comparator.comparing(o -> o.getKey().toString()))
//                    .forEach((e) -> {
//                        stringBuilder.append("\t- ").append(e.getKey().toString()).append("\n");
//                        if (full) {
//                            stringBuilder
//                                    .append("\t\t    Original: ")
//                                    .append(e.getValue().originalRecipe().toString())
//                                    .append("\n");
//                            stringBuilder
//                                    .append("\t\t Transformed: ")
//                                    .append(e.getValue().transformedRecipe().toString())
//                                    .append("\n\n");
//                        }
//                    });
//            stringBuilder.append("}\n\n");
//        });
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
