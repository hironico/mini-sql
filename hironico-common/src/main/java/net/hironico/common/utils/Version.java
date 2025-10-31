package net.hironico.common.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for retrieving the version from the JAR manifest.
 */
public class Version {
    private static String version = null;

    private static final Logger LOGGER = Logger.getLogger(Version.class.getName());

    /**
     * Retrieves the version string from the JAR manifest.
     * @return the version string, or "unknown!" if not found
     */
    public static String getVersion() {
        // scann classpath only opce.
        if (Version.version != null) {
            return Version.version;
        }

        version = "unknown!";
        try {
            Enumeration<URL> resources = Version.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
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
}
