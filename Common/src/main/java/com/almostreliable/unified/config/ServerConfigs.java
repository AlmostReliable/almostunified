package com.almostreliable.unified.config;

public class ServerConfigs {

    private final UnifyConfig unifyConfig;
    private final DuplicationConfig dupConfig;
    private final DebugConfig debugConfig;

    public static ServerConfigs load() {
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
