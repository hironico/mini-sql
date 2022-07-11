package net.hironico.minisql;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.common.utils.DynamicFileLoader;
import com.formdev.flatlaf.FlatLightLaf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("restriction")
public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static String version = null;

    public static MainWindow mainWindow;

    public static String getVersion() {

        // scann classpath only opce.
        if (App.version != null) {
            return App.version;
        }

        version = "unknown!";
        try {
            Enumeration<URL> resources = App.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {

                Manifest manifest = new Manifest(resources.nextElement().openStream());

                System.out.println("Manifest found: " + manifest.toString());
                manifest.getEntries().keySet().forEach(k -> {
                    System.out.println("> " + k);
                });

                // check that this is your manifest and do what you need or get the next one
                Attributes attribs = manifest.getMainAttributes();
                if (attribs != null) {
                    version = attribs.getValue("Hironico-Mini-SQL");
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read the version from the manifest.", ex);
        }
        return version;
    }

    private static void addDriversToClasspath() throws Exception {
        // maintenant charger les drivers dans les JARs séparés.
        File dir = new File(".");
        LOGGER.info("Startup directory: " + dir.getAbsolutePath());

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("ojdbc") && name.endsWith(".jar");
            }
        };

        for (String name : dir.list(filter)) {
            File jarFile = new File(dir.getAbsolutePath() + File.separator + name);
            LOGGER.info("Including file into classpath: " + jarFile.getAbsolutePath());
            DynamicFileLoader.addFile(jarFile);
        }
    }

    private static void loadConfig() {
        try {
            DbConfigFile.loadConfig();
        } catch (FileNotFoundException fne) {
            LOGGER.warning("Configuration file was not found. Never mind...");
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Error while loading config from file.", t);
        }
    }

    private static void startGui() {
        Runnable starter = new Runnable() {

            @Override
            public void run() {

                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Unable to set the windows look and feel...");
                }

                mainWindow = MainWindow.getInstance();

                mainWindow.setVisible(true);
            }
        };

        SwingUtilities.invokeLater(starter);
    }

    public static void main(String[] args) {

        // log format configure
        String logFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-7s %5$s%6$s%n";
        System.setProperty("java.util.logging.SimpleFormatter.format", logFormat);

        try {
            addDriversToClasspath();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot add jar files to classpath.", ex);
        }

        // load config file
        App.loadConfig();

        // kick off GUI
        App.startGui();

    }

}