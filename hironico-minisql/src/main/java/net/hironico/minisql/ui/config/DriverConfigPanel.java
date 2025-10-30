package net.hironico.minisql.ui.config;

import net.hironico.minisql.DbConfigFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel to manage JDBC driver JAR files.
 * Allows users to browse for JAR files, add them to the classpath,
 * and remove them from the configuration.
 */
public class DriverConfigPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(DriverConfigPanel.class.getName());
    
    // Static class loader to accumulate all driver JARs
    private static URLClassLoader driverClassLoader = null;
    
    private DefaultListModel<String> driverListModel;
    private JList<String> driverList;
    private JButton btnBrowse;
    private JButton btnRemove;
    private JButton btnAddToClasspath;
    private JLabel lblStatus;
    
    public DriverConfigPanel() {
        super();
        initialize();
        loadDriversFromConfig();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("JDBC Driver JAR Files");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);
        
        // Create list panel with scroll pane
        driverListModel = new DefaultListModel<>();
        driverList = new JList<>(driverListModel);
        driverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driverList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(driverList);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        btnBrowse = new JButton("Browse...");
        btnBrowse.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        btnBrowse.addActionListener(e -> browseForDriver());
        
        btnRemove = new JButton("Remove");
        btnRemove.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        btnRemove.addActionListener(e -> removeSelectedDriver());
        btnRemove.setEnabled(false);
        
        btnAddToClasspath = new JButton("Add to Classpath");
        btnAddToClasspath.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        btnAddToClasspath.addActionListener(e -> addSelectedToClasspath());
        btnAddToClasspath.setEnabled(false);
        
        buttonPanel.add(btnBrowse);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(btnRemove);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(btnAddToClasspath);
        buttonPanel.add(Box.createVerticalGlue());
        
        add(buttonPanel, BorderLayout.EAST);
        
        // Create status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.BLUE);
        statusPanel.add(lblStatus);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Add list selection listener
        driverList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = !driverList.isSelectionEmpty();
                btnRemove.setEnabled(hasSelection);
                btnAddToClasspath.setEnabled(hasSelection);
            }
        });
    }
    
    private void loadDriversFromConfig() {
        driverListModel.clear();
        java.util.List<String> drivers = DbConfigFile.getInstance().getDriverJarPaths();
        if (drivers != null) {
            for (String driver : drivers) {
                driverListModel.addElement(driver);
            }
        }
    }
    
    private void browseForDriver() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select JDBC Driver JAR File");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR Files (*.jar)", "jar");
        chooser.setFileFilter(filter);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            for (File file : selectedFiles) {
                String path = file.getAbsolutePath();
                
                // Check if already in list
                if (!driverListModel.contains(path)) {
                    driverListModel.addElement(path);
                    DbConfigFile.getInstance().addDriverJarPath(path);
                    setStatus("Added: " + file.getName());
                    LOGGER.info("Added driver JAR: " + path);
                } else {
                    setStatus("Already in list: " + file.getName());
                }
            }
            
            // Save configuration
            try {
                DbConfigFile.saveConfig();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to save configuration", ex);
                setStatus("Error saving configuration", Color.RED);
            }
        }
    }
    
    private void removeSelectedDriver() {
        String selectedDriver = driverList.getSelectedValue();
        if (selectedDriver != null) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Remove driver from configuration?\n" + selectedDriver,
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                driverListModel.removeElement(selectedDriver);
                DbConfigFile.getInstance().removeDriverJarPath(selectedDriver);
                setStatus("Removed: " + new File(selectedDriver).getName());
                LOGGER.info("Removed driver JAR: " + selectedDriver);
                
                // Save configuration
                try {
                    DbConfigFile.saveConfig();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to save configuration", ex);
                    setStatus("Error saving configuration", Color.RED);
                }
            }
        }
    }
    
    private void addSelectedToClasspath() {
        String selectedDriver = driverList.getSelectedValue();
        if (selectedDriver != null) {
            File jarFile = new File(selectedDriver);
            
            if (!jarFile.exists()) {
                setStatus("File not found: " + jarFile.getName(), Color.RED);
                JOptionPane.showMessageDialog(
                    this,
                    "JAR file not found:\n" + selectedDriver,
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            try {
                addJarToClasspath(jarFile);
                setStatus("Successfully added to classpath: " + jarFile.getName(), new Color(0, 128, 0));
                LOGGER.info("Added to classpath: " + selectedDriver);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Driver JAR successfully added to classpath:\n" + jarFile.getName() +
                    "\n\nNote: The driver will be available for new connections.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to add JAR to classpath", ex);
                setStatus("Error loading JAR: " + ex.getMessage(), Color.RED);
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to add JAR to classpath:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Static method to load a driver JAR file into the classpath.
     * This is called by DbConfigFile during application startup to automatically load configured drivers.
     */
    private static void loadDriverJarStatic(File jarFile) throws Exception {
        addJarToClasspathStatic(jarFile);
    }
    
    /**
     * Dynamically add a JAR file to the classpath.
     * This creates/extends a custom URLClassLoader that can load JDBC drivers at runtime.
     */
    private void addJarToClasspath(File jarFile) throws Exception {
        addJarToClasspathStatic(jarFile);
    }
    
    /**
     * Static implementation of JAR loading logic.
     * This is the actual implementation that both instance and static methods use.
     */
    private static void addJarToClasspathStatic(File jarFile) throws Exception {
        URL jarUrl = jarFile.toURI().toURL();
        
        // Create or extend the driver class loader
        if (driverClassLoader == null) {
            // First time: create new class loader with system class loader as parent
            driverClassLoader = new URLClassLoader(
                new URL[]{jarUrl},
                ClassLoader.getSystemClassLoader()
            );
            LOGGER.info("Created new driver class loader with: " + jarFile.getName());
        } else {
            // Subsequent times: create new class loader including all previous URLs plus the new one
            URL[] existingUrls = driverClassLoader.getURLs();
            URL[] newUrls = new URL[existingUrls.length + 1];
            System.arraycopy(existingUrls, 0, newUrls, 0, existingUrls.length);
            newUrls[existingUrls.length] = jarUrl;
            
            // Close the old class loader if possible
            try {
                driverClassLoader.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not close old class loader", e);
            }
            
            // Create new class loader with all URLs
            driverClassLoader = new URLClassLoader(
                newUrls,
                ClassLoader.getSystemClassLoader()
            );
            LOGGER.info("Extended driver class loader with: " + jarFile.getName() + 
                       " (now contains " + newUrls.length + " JARs)");
        }
        
        // Set as context class loader for the current thread
        Thread.currentThread().setContextClassLoader(driverClassLoader);
        
        // Try to load and register any JDBC drivers in the JAR
        loadJdbcDriversFromJar(jarFile);
    }
    
    /**
     * Attempt to find and register JDBC drivers from the JAR file.
     */
    private static void loadJdbcDriversFromJar(File jarFile) {
        try {
            // Common JDBC driver class names to try
            String[] commonDrivers = {
                "org.postgresql.Driver",
                "com.mysql.jdbc.Driver",
                "com.mysql.cj.jdbc.Driver",
                "oracle.jdbc.driver.OracleDriver",
                "oracle.jdbc.OracleDriver",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "org.mariadb.jdbc.Driver",
                "org.h2.Driver",
                "org.sqlite.JDBC",
                "net.sourceforge.jtds.jdbc.Driver"
            };
            
            int loadedCount = 0;
            for (String driverClass : commonDrivers) {
                try {
                    Class<?> driver = Class.forName(driverClass, true, driverClassLoader);
                    LOGGER.info("Successfully loaded JDBC driver: " + driverClass);
                    loadedCount++;
                } catch (ClassNotFoundException e) {
                    // This driver is not in this JAR, ignore
                }
            }
            
            if (loadedCount > 0) {
                LOGGER.info("Loaded " + loadedCount + " JDBC driver(s) from " + jarFile.getName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while trying to load JDBC drivers from JAR", e);
        }
    }
    
    /**
     * Get the driver class loader containing all loaded driver JARs.
     * Use this class loader when loading JDBC driver classes.
     * Example: Class.forName("com.driver.Driver", true, DriverConfigPanel.getDriverClassLoader())
     */
    public static ClassLoader getDriverClassLoader() {
        return driverClassLoader != null ? driverClassLoader : ClassLoader.getSystemClassLoader();
    }
    
    private void setStatus(String message) {
        setStatus(message, Color.BLUE);
    }
    
    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }
}
