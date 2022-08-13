package com.almostreliable.unified.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class FileUtils {

    public static void write(Path path, String filename, Consumer<StringBuilder> callback) {
        StringBuilder sb = new StringBuilder();
        callback.accept(sb);

        try {
            Files.createDirectories(path);
            Path filePath = path.resolve(filename);
            Files.writeString(filePath,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
