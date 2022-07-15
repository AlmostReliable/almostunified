package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class RecipeDumper {
    private final RecipeTransformationResult result;
    private final DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public RecipeDumper(RecipeTransformationResult result) {
        this.result = result;
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
                .append("# Last execution: ").append(format.format(new Date(result.getStartTime())))
                .append("\n")
                .append("# Overview:\n")
                .append("\t- Transformed: ").append(result.getTransformedCount())
                .append("\n")
                .append("\t- Total Recipes: ").append(result.getRecipeCount())
                .append("\n")
                .append("\t- Elapsed Time: ").append(result.getTotalTime()).append("ms")
                .append("\n\n")
                .append("# Summary: \n");

        result.forEachTransformedRecipe((type, recipes) -> {
            stringBuilder
                    .append(StringUtils.rightPad(type.toString(), 45))
                    .append(" = ")
                    .append(StringUtils.leftPad(String.valueOf(recipes.size()), 5))
                    .append(" (")
                    .append(result.getAllEntriesByType(type).size())
                    .append(")\n");
        });
    }

    private void dumpTransformedRecipes(StringBuilder stringBuilder, boolean full) {
        result.forEachTransformedRecipe((type, recipes) -> {
            stringBuilder.append(type.toString()).append(" {\n");
            recipes.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getKey().toString()))
                    .forEach((e) -> {
                        stringBuilder.append("\t- ").append(e.getKey().toString()).append("\n");
                        if (full) {
                            stringBuilder
                                    .append("\t\t    Original: ")
                                    .append(e.getValue().originalRecipe().toString())
                                    .append("\n");
                            stringBuilder
                                    .append("\t\t Transformed: ")
                                    .append(e.getValue().transformedRecipe().toString())
                                    .append("\n\n");
                        }
                    });
            stringBuilder.append("}\n\n");
        });
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
}
