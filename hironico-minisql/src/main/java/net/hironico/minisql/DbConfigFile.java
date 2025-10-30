package net.hironico.minisql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import net.hironico.common.utils.XMLFile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.swing.*;

@JsonRootName("config")
@JacksonXmlRootElement(localName = "config")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbConfigFile {
    private static final Logger LOGGER = Logger.getLogger(DbConfigFile.class.getName());

    protected static List<DbConfig> all = new ArrayList<>();

    protected static Boolean decoratedWindow = Boolean.FALSE;

    protected static List<String> driverJarPaths = new ArrayList<>();

    public interface DbConfigFileListener {
        void configAdded(DbConfig config);
        void configRemoved(DbConfig config);
    }

    private static final List<DbConfigFileListener> listeners = new ArrayList<>();

    private static final DbConfigFile instance = new DbConfigFile();

    private DbConfigFile() {
    }

    public static DbConfigFile getInstance() {
        return instance;
    }

    public static void addListener(DbConfigFileListener listener) {
        if (DbConfigFile.listeners.contains(listener)) {
            return;
        }

        listeners.add(listener);
    }

    public void removeListener(DbConfigFileListener listener) {
        DbConfigFile.listeners.remove(listener);
    }

    private static void fireConfigAdded(DbConfig config) {
        SwingUtilities.invokeLater(() -> DbConfigFile.listeners.forEach(listener -> listener.configAdded(config)));
    }

    private static void fireConfigRemoved(DbConfig config) {
        SwingUtilities.invokeLater(() -> DbConfigFile.listeners.forEach(listener -> listener.configRemoved(config)));
    }

    public static void addConfig(String name) {
        DbConfigFile.addConfig(name, "", "", "");
    }

    private static void addConfig(String name, String url, String user, String password) {
        DbConfig cfg = new DbConfig();
        cfg.jdbcUrl = url;
        cfg.user = user;
        cfg.password = password;
        cfg.name = name;

        all.add(cfg);

        fireConfigAdded(cfg);
    }

    public static DbConfig duplicate(String src, String dest) {
        try {
            DbConfig srcCfg = getConfig(src);
            if (srcCfg == null) {
                return null;
            }

            DbConfig newCfg = (DbConfig)srcCfg.clone();
            newCfg.name = dest;
            all.add(newCfg);

            fireConfigAdded(newCfg);

            return newCfg;
        } catch (CloneNotSupportedException ex) {
            LOGGER.log(Level.SEVERE, "Cannot clone db config.", ex);
            return null;
        }
    }

    public static synchronized DbConfig getConfig(String name) {
        for (DbConfig cfg : DbConfigFile.all) {
            if (cfg.name.equals(name)) {
                return cfg;
            }
        }

        return null;
    }

    public static void removeConfig(String name) {
        DbConfig cfg = getConfig(name);
        all.remove(cfg);
        fireConfigRemoved(cfg);
    }

    public static Collection<String> getConfigNames() {
        List<String> lst = new ArrayList<>();
        for (DbConfig cfg : all) {
            lst.add(cfg.name);
        }
        Collections.sort(lst);

        return lst;
    }

    @JsonProperty("dbConfigList")
    @JacksonXmlElementWrapper(localName = "db-config-list")
    @JacksonXmlProperty(localName = "db-config")
    public List<DbConfig> getAllConfigs() {
        return all;
    }

    public void setAllConfigs(List<DbConfig> configs) {
        LOGGER.info("Setting db config list: " + configs.size() + " configs.");
        DbConfigFile.all = configs;
    }

    @JsonProperty("decorated-window")
    @JacksonXmlProperty(localName = "decorated-window")
    public Boolean getDecoratedWindow() {
        return this.decoratedWindow;
    }

    public void setDecoratedWindow(Boolean decoratedWindow) {
        this.decoratedWindow = decoratedWindow == null ? Boolean.FALSE : decoratedWindow;
    }

    @JsonProperty("driverJarPaths")
    @JacksonXmlElementWrapper(localName = "driver-jar-paths")
    @JacksonXmlProperty(localName = "driver-jar-path")
    public List<String> getDriverJarPaths() {
        return driverJarPaths;
    }

    public void setDriverJarPaths(List<String> driverJarPaths) {
        DbConfigFile.driverJarPaths = driverJarPaths == null ? new ArrayList<>() : driverJarPaths;
        LOGGER.info("Loaded " + DbConfigFile.driverJarPaths.size() + " driver JAR paths.");
    }

    public void addDriverJarPath(String path) {
        if (path != null && !driverJarPaths.contains(path)) {
            driverJarPaths.add(path);
        }
    }

    public void removeDriverJarPath(String path) {
        driverJarPaths.remove(path);
    }

    public static File getConfigFile() {
        return new File(System.getProperty("user.home") + File.separator + "minisql-config.xml");
    }

    public static synchronized void saveConfig() throws Exception {
        File configFile = DbConfigFile.getConfigFile();
        LOGGER.info("Saving config into " + configFile.getAbsolutePath());

        XMLFile.saveAs(configFile, new DbConfigFile());
        LOGGER.info("Successfully saved config.");
    }

    public static synchronized void loadConfig() throws Exception {
        File configFile = DbConfigFile.getConfigFile();
        LOGGER.info("Loading config from: " + configFile.getAbsolutePath());

        if (!configFile.exists()) {
            throw new FileNotFoundException("Config file does not exists. " + configFile.getAbsolutePath());
        }

        FileInputStream fis = new FileInputStream(configFile);
        DbConfigFile.loadConfig(fis);
    }

    public static synchronized void loadConfig(InputStream in) throws IOException {
        XMLFile.load(in, DbConfigFile.class);
        final StringBuffer allNames = new StringBuffer();
        DbConfigFile.all.forEach(cfg -> allNames.append(" ").append(cfg.name));
        LOGGER.info("Found: " + DbConfigFile.all.size() + " DB configurations: " + allNames);
        
        // Automatically load all configured driver JARs
        loadAllDriverJars();
    }
    
    /**
     * Load all configured driver JAR files into the classpath.
     * This method is called automatically when the configuration is loaded.
     */
    private static void loadAllDriverJars() {
        if (driverJarPaths == null || driverJarPaths.isEmpty()) {
            LOGGER.info("No driver JAR paths configured");
            return;
        }
        
        LOGGER.info("Loading " + driverJarPaths.size() + " driver JAR(s) from configuration...");
        
        for (String jarPath : driverJarPaths) {
            try {
                java.io.File jarFile = new java.io.File(jarPath);
                if (!jarFile.exists()) {
                    LOGGER.warning("Driver JAR file not found: " + jarPath);
                    continue;
                }
                
                loadDriverJar(jarFile);
                LOGGER.info("Successfully loaded driver JAR: " + jarFile.getName());
                
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load driver JAR: " + jarPath, ex);
            }
        }
    }
    
    /**
     * Load a single driver JAR file into the classpath.
     * This delegates to DriverConfigPanel's addJarToClasspath functionality.
     */
    private static void loadDriverJar(java.io.File jarFile) throws Exception {
        try {
            // Use reflection to call DriverConfigPanel's method to avoid direct dependency
            Class<?> driverConfigPanelClass = Class.forName("net.hironico.minisql.ui.config.DriverConfigPanel");
            java.lang.reflect.Method method = driverConfigPanelClass.getDeclaredMethod("loadDriverJarStatic", java.io.File.class);
            method.setAccessible(true);
            method.invoke(null, jarFile);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("DriverConfigPanel class not found, cannot load driver JARs");
            throw e;
        }
    }
}
