package net.hironico.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Language utilities : shortcuts that make life easier.
 */
public class LangUtils {
    private static final Logger LOGGER = Logger.getLogger(LangUtils.class.getName());

    /**
     * Returns true if at least one of the given objects is null.
     */
    public static boolean hasOneNull(Object... objs) {
        for (Object o : objs) {
            if (o == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true is ALL objects are null
     * @param objs objects to tests if null
     * @return true is all objects are null
     */
    public static boolean isAllNull(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllNotNull(Object... objs) {
        for (Object o : objs) {
            if (o == null) {
                return false;
            }
        }

        return true;
    }

    public static List<Class<? extends Object>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            String fileName = resource.getFile();
            LOGGER.info("Found resource: " + fileName);

            if (fileName.startsWith("file:/")) {
                // let's keep the leading / !
                fileName = fileName.substring(5);
            }
            if (fileName.contains(".jar!")) {
                fileName = fileName.split("!")[0];
            }

            fileName = fileName.replace("%20", " ");

            LOGGER.info("Adding resource: " + fileName);

            dirs.add(new File(fileName));
        }
        List<Class<? extends Object>> classes = new ArrayList<>();
        for (File directory : dirs) {
            LOGGER.info("Examine resource : " + directory.getAbsolutePath());
            if (directory.getAbsolutePath().endsWith(".jar")) {                
                classes.addAll(getClassesFromJarFile(directory, packageName, classLoader));
            } else {
                classes.addAll(findClasses(directory, packageName));
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<? extends Object>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<? extends Object>> classes = new ArrayList<>();
        if (!directory.exists()) {
            LOGGER.severe("Cannot find classes in " + directory.getAbsolutePath() + ". Does not exist.");
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private static String fromFileToClassName(final String fileName) {
		return fileName.substring(0, fileName.length() - 6).replaceAll("/|\\\\", "\\.");
	}

	private static List<Class<? extends Object>> getClassesFromJarFile(File path, String packageName, ClassLoader classLoader) {
		List<Class<? extends Object>> classes = new ArrayList<>();
        LOGGER.log(Level.INFO, "getClassesFromJarFile: Getting classes for " + path.getAbsolutePath());
        
		try {
			if (path.canRead()) {
				JarFile jar = new JarFile(path);
				Enumeration<JarEntry> en = jar.entries();
				while (en.hasMoreElements()) {
                    JarEntry entry = en.nextElement();
					if (entry.getName().endsWith("class")) {
                        String className = fromFileToClassName(entry.getName());
                        if (className.startsWith(packageName)) {
                            LOGGER.log(Level.INFO, "\tgetClassesFromJarFile: found " + className);   
                            Class<? extends Object> claz = Class.forName(className, false, classLoader);
                            classes.add(claz);
                        }
					}
                }
                jar.close();
			} else {
                LOGGER.severe("Cannot read file: " + path.getAbsolutePath());
            }
		} catch (Exception e) {
			throw new RuntimeException("Failed to read classes from jar file: " + path, e);
		}

		return classes;
	}
}