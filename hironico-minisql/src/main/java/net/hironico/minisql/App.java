package net.hironico.minisql;

import javax.swing.*;

import com.formdev.flatlaf.util.SystemInfo;
import net.hironico.common.swing.ComponentMover;
import net.hironico.common.swing.ComponentResizer;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.common.utils.DynamicFileLoader;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Objects;
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

                LOGGER.fine(String.format("Manifest found: %s", manifest));
                manifest.getEntries().keySet().forEach(k -> LOGGER.fine(String.format("> %s", k)));

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

        FilenameFilter filter = (dir1, name) -> name.startsWith("ojdbc") && name.endsWith(".jar");

        for (String name : Objects.requireNonNull(dir.list(filter))) {
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
        SwingUtilities.invokeLater(() -> {
            try {
                // now try to setup the LnF
                UIManager.setLookAndFeel(new FlatLightLaf());

                if( SystemInfo.isLinux ) {
                    // enable custom window decorations
                    JFrame.setDefaultLookAndFeelDecorated( false );
                    JDialog.setDefaultLookAndFeelDecorated( false );
                }

                if( SystemInfo.isMacOS ) {
                    // enable screen menu bar
                    // (moves menu bar from JFrame window to top of screen)
                    System.setProperty( "apple.laf.useScreenMenuBar", "true" );

                    // application name used in screen menu bar
                    // (in first menu after the "apple" menu)
                    System.setProperty( "apple.awt.application.name", "My Application" );

                    // appearance of window title bars
                    // possible values:
                    //   - "system": use current macOS appearance (light or dark)
                    //   - "NSAppearanceNameAqua": use light appearance
                    //   - "NSAppearanceNameDarkAqua": use dark appearance
                    // (must be set on main thread and before AWT/Swing is initialized;
                    //  setting it on AWT thread does not work)
                    System.setProperty( "apple.awt.application.appearance", "system" );
                }

                UIManager.put( "TabbedPane.selectedBackground", Color.white );
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Unable to set the windows look and feel...");
            }

            mainWindow = MainWindow.getInstance();

            mainWindow.setVisible(true);
            mainWindow.setPreferredSize(new Dimension(1024,768));
            mainWindow.setSize(1024, 768);

            // add functionalities to undecorated window
            if (mainWindow.isUndecorated()) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle screenBounds = env.getMaximumWindowBounds();

                ComponentResizer cr = new ComponentResizer();
                cr.setMinimumSize(new Dimension(1024, 768));
                cr.setMaximumSize(new Dimension(screenBounds.width, screenBounds.height));
                cr.registerComponent(mainWindow);
                cr.setSnapSize(new Dimension(10, 10));

                ComponentMover cm = new ComponentMover(mainWindow, mainWindow.getRibbon());
                mainWindow.getRibbon().addMouseListener(new MouseAdapter() {
                    final Rectangle oldBounds = new Rectangle(0, 0, 1024, 768);

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() >= 2) {
                            int width = mainWindow.getSize().width;
                            int height = mainWindow.getSize().height;
                            int x = mainWindow.getX();
                            int y = mainWindow.getY();

                            if (width < screenBounds.width && height < screenBounds.height) {
                                this.oldBounds.x = x;
                                this.oldBounds.y = y;
                                this.oldBounds.width = width;
                                this.oldBounds.height = height;
                                mainWindow.setBounds(screenBounds);
                            } else {
                                mainWindow.setBounds(oldBounds);
                            }
                        }
                    }
                });
            }
        });
    }

    public static void main(String[] args) {

        // log format configure
        String logFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-7s %2$s %5$s%6$s%n";
        System.setProperty("java.util.logging.SimpleFormatter.format", logFormat);

        try {
            addDriversToClasspath();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot add jar files to classpath.", ex);
        }

        // have the name of the application in the macos menu if applicable
        System.setProperty("apple.awt.application.name", "Mini SQL");

        // load config file
        App.loadConfig();

        // kick off GUI
        App.startGui();

    }

}