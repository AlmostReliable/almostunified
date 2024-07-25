package com.almostreliable.unified.utils;

import com.almostreliable.unified.BuildConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.DeleteAction;
import org.apache.logging.log4j.core.appender.rolling.action.IfFileName;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class CustomLogger {

    private static final String BACKUP_FILE = BuildConfig.MOD_ID + "-backup.log.gz";
    private static final String FILE = BuildConfig.MOD_ID + ".log";
    private static final String LOG_PATH = "logs/" + BuildConfig.MOD_ID;

    private CustomLogger() {}

    public static Logger create() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        PathCondition[] conditions = { IfFileName.createNameCondition(BACKUP_FILE, null) };
        var deleteAction = DeleteAction.createDeleteAction(LOG_PATH, false, 1, false, null, conditions, null, config);
        var strategy = DefaultRolloverStrategy.newBuilder().withCustomActions(new Action[]{ deleteAction }).build();

        var layout = PatternLayout
                .newBuilder()
                .withConfiguration(config)
                .withCharset(StandardCharsets.UTF_8)
                .withPattern("[%d{HH:mm:ss.SSS}] [%level]: %minecraftFormatting{%msg{nolookup}}{strip}%n%xEx")
                .build();

        var fileAppender = RollingRandomAccessFileAppender
                .newBuilder()
                .withAppend(true)
                .withFileName(LOG_PATH + "/" + FILE)
                .withFilePattern(LOG_PATH + "/" + BACKUP_FILE)
                .withStrategy(strategy)
                .withPolicy(new Policy())
                .setName(BuildConfig.MOD_NAME + " File")
                .setLayout(layout)
                .setConfiguration(config)
                .build();

        fileAppender.start();

        LoggerConfig loggerConfig = new LoggerConfig(BuildConfig.MOD_NAME, null, false);
        loggerConfig.addAppender(fileAppender, null, null);

        Optional.ofNullable(config.getAppenders().get("Console")) // latest.log for neoforge
                .ifPresent(a -> loggerConfig.addAppender(a, null, null));
        Optional.ofNullable(config.getAppenders().get("SysOut")) // latest.log for fabric
                .ifPresent(a -> loggerConfig.addAppender(a, null, null));
        Optional.ofNullable(config.getAppenders().get("ServerGuiConsole")) // game console
                .ifPresent(a -> loggerConfig.addAppender(a, null, null));

        config.addLogger(BuildConfig.MOD_NAME, loggerConfig);
        return LogManager.getLogger(BuildConfig.MOD_NAME);
    }

    private static class Policy implements TriggeringPolicy {

        private boolean reset = true;

        @Override
        public void initialize(RollingFileManager manager) {}

        @Override
        public boolean isTriggeringEvent(LogEvent logEvent) {
            if (reset) {
                reset = false;
                return true;
            }
            return false;
        }
    }
}
