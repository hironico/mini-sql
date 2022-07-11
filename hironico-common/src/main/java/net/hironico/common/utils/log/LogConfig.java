package net.hironico.common.utils.log;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("log")
public class LogConfig {

    @JsonProperty("filename")
    public String filename;

    @JsonProperty("level")
    public String level;

    @JsonProperty("pattern")
    public String pattern;

    public void apply() {
        LogConfig.setLogLevel(this.level);

        // log format configure
        System.setProperty("java.util.logging.SimpleFormatter.format", pattern);

        ConsoleHandler logHandler = new ConsoleHandler();
        Logger.getAnonymousLogger().addHandler(logHandler);

        // set file handler if present in config
        setFileHandler();

        // log level
        setLogLevel(level);
    }

    private void setFileHandler() {
        if (this.filename != null) {
            return;
        }

        // 10 files of 10 MB each max.
        try {
            FileHandler fileHandler = new FileHandler(this.filename, 1024 * 1024 * 10, 10);
            Logger.getAnonymousLogger().addHandler(fileHandler);
            Logger.getAnonymousLogger().info("LOG file configured: " + this.filename);
        } catch (IOException | SecurityException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot create the log file handler.", ex);
        }
    }

    /**
     * Use this static method to set the log level of the application dynamically 
     * @param logLevelArg
     */
    public static void setLogLevel(String logLevelArg) {
        try {
            Level level = Level.parse(logLevelArg.toUpperCase());
            if (level == null) {
                Logger.getAnonymousLogger().severe("Invalid log level: " + logLevelArg);
                return;
            }            
            
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setLevel(level);
            for(Handler h : rootLogger.getHandlers()) {
                h.setLevel(level);
            }            
        } catch (IllegalArgumentException iae) {
            Logger.getAnonymousLogger().severe("Invalid log level argument: " + logLevelArg);
        }
    }
}