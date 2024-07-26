package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.config.DebugConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public final class FileUtils {

    private FileUtils() {}

    public static void createGitIgnore() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            write(path, ".gitignore", sb -> sb.append(DebugConfig.NAME).append(".json").append("\n"));
        }
    }

    public static void writeDebugLog(String fileName, Consumer<StringBuilder> callback) {
        write(AlmostUnifiedPlatform.INSTANCE.getDebugLogPath(), fileName, callback);
    }

    private static void write(Path path, String fileName, Consumer<StringBuilder> callback) {
        StringBuilder sb = new StringBuilder();
        callback.accept(sb);

        try {
            Files.createDirectories(path);
            Path filePath = path.resolve(fileName);
            Files.writeString(
                    filePath,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            AlmostUnified.LOGGER.warn("Couldn't write to file '{}'.", fileName, e);
        }
    }
}
