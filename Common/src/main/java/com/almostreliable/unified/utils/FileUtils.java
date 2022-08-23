package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public final class FileUtils {

    private FileUtils() {}

    public static void write(Path path, String fileName, Consumer<StringBuilder> callback) {
        StringBuilder sb = new StringBuilder();
        callback.accept(sb);

        try {
            Files.createDirectories(path);
            Path filePath = path.resolve(fileName);
            Files.writeString(filePath,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            AlmostUnified.LOG.warn("Dump couldn't be written '{}': {}", fileName, e.getMessage());
            e.printStackTrace();
        }
    }
}
