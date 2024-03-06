package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigs {

    private final UnifyConfig unifyConfig;
    private final ReplacementsConfig replacementsConfig;
    private final TagConfig tagConfig;
    private final DuplicationConfig dupConfig;
    private final DebugConfig debugConfig;

    public static ServerConfigs load() {
        createGitIgnoreIfNotExists();
        UnifyConfig unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        ReplacementsConfig replacementsConfig = Config.load(ReplacementsConfig.NAME,
                new ReplacementsConfig.Serializer());
        TagConfig tagConfig = Config.load(TagConfig.NAME, new TagConfig.Serializer());
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());
        return new ServerConfigs(unifyConfig, replacementsConfig, tagConfig, dupConfig, debugConfig);
    }

    private ServerConfigs(UnifyConfig unifyConfig, ReplacementsConfig replacementsConfig, TagConfig tagConfig, DuplicationConfig dupConfig, DebugConfig debugConfig) {
        this.unifyConfig = unifyConfig;
        this.replacementsConfig = replacementsConfig;
        this.tagConfig = tagConfig;
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

    public ReplacementsConfig getReplacementsConfig() {
        return replacementsConfig;
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

    public TagConfig getTagConfig() {
        return tagConfig;
    }
}
