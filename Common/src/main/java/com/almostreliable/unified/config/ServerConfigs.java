package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigs {

    private final UnifyConfig unifyConfig;
    private final DuplicationConfig dupConfig;
    private final DebugConfig debugConfig;

    public static ServerConfigs load() {
        createGitIgnoreIfNotExists();
        UnifyConfig unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());
        return new ServerConfigs(unifyConfig, dupConfig, debugConfig);
    }

    private ServerConfigs(UnifyConfig unifyConfig, DuplicationConfig dupConfig, DebugConfig debugConfig) {
        this.unifyConfig = unifyConfig;
        this.dupConfig = dupConfig;
        this.debugConfig = debugConfig;
    }

    private static void createGitIgnoreIfNotExists() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        if (!(Files.exists(path) && Files.isDirectory(path))) {
            FileUtils.write(
                    AlmostUnifiedPlatform.INSTANCE.getConfigPath(),
                    ".gitignore",
                    sb -> sb.append(DebugConfig.NAME).append(".json").append("\n")
            );
        }
    }

    public UnifyConfig getUnifyConfig() {
        return unifyConfig;
    }

    public DuplicationConfig getDupConfig() {
        return dupConfig;
    }

    public DebugConfig getDebugConfig() {
        return debugConfig;
    }
}
