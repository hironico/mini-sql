package net.hironico.minisql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import net.hironico.common.utils.XMLFile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonRootName("config")
@JacksonXmlRootElement(localName = "config")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbConfigFile {
    private static final Logger LOGGER = Logger.getLogger(DbConfigFile.class.getName());

    protected static List<DbConfig> all = new ArrayList<>();

    private DbConfigFile() {
    }

    public static DbConfig addConfig(String name) {
        return DbConfigFile.addConfig(name, "", "", "");
    }

    private static DbConfig addConfig(String name, String url, String user, String password) {
        DbConfig cfg = new DbConfig();
        cfg.jdbcUrl = url;
        cfg.user = user;
        cfg.password = password;
        cfg.name = name;

        all.add(cfg);

        return cfg;
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
        };

        return null;
    }

    public static void removeConfig(String name) {
        all.remove(getConfig(name));
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
        DbConfigFile.all.forEach(cfg -> {
            allNames.append(" ").append(cfg.name);
        });
        LOGGER.info("Found: " + DbConfigFile.all.size() + " DB configurations: " + allNames.toString());
    }
}