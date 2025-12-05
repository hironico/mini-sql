package net.hironico.minisql.ui.config;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.minisql.DbConfigFile;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Main configuration panel that provides access to all MiniSQL application settings.
 * This panel combines multiple configuration categories (General, Connections, Drivers)
 * in a unified interface using a card layout with a tree-based navigation menu.
 */
public class ConfigPanel extends JRoundedPanel {

    /** Tree component used for navigation between different configuration categories */
    private JXTree treeMenu = null;

    /** Scroll pane containing the navigation tree */
    private JScrollPane scrollMenu = null;

    /** Connections parent node in the tree */
    private DefaultMutableTreeNode connectionsNode = null;

    /** Card layout manager for switching between configuration panels */
    private CardLayout cardLayout = null;

    /** Main panel containing the card layout with different configuration sections */
    private JPanel mainPanel = null;

    /** Card identifier for general settings panel */
    private final String CARD_GENERAL = "General";

    /** Card identifier for database connections panel */
    private final String CARD_CONNECTIONS = "Connections";

    /** Card identifier for JDBC drivers panel */
    private final String CARD_DRIVERS = "Drivers";

    /** General configuration panel instance */
    private GeneralConfigPanel generalConfigPanel = null;

    /** Database configuration panel instance */
    private DbConfigPanel dbConfigPanel = null;

    /** Driver configuration panel instance */
    private DriverConfigPanel driverConfigPanel = null;

    /**
     * Constructs a new ConfigPanel with default configuration.
     * Initializes the navigation tree and configuration panels.
     */
    public ConfigPanel() {
        super();
        initialize();
    }

    /**
     * Initializes the configuration panel layout and components.
     * Sets up the main panel with navigation on the left and content area on the right.
     */
    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        setLayout(new BorderLayout(5, 0));

        add(getScrollMenu(), BorderLayout.WEST);
        add(getMainPanel(), BorderLayout.CENTER);
    }

    /**
     * Gets or creates the main panel containing the card layout.
     * Contains all configuration panels and manages switching between them.
     *
     * @return the main JPanel with card layout
     */
    private JPanel getMainPanel() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();
            this.cardLayout = new CardLayout(5, 5);
            this.mainPanel.setLayout(cardLayout);

            this.mainPanel.add(getGeneralConfigPanel(), CARD_GENERAL);
            this.mainPanel.add(getDbConfigPanel(), CARD_CONNECTIONS);
            this.mainPanel.add(getDriverConfigPanel(), CARD_DRIVERS);
        }

        return this.mainPanel;
    }

    /**
     * Gets or creates the scroll pane containing the navigation tree.
     *
     * @return JScrollPane with the navigation tree
     */
    private JScrollPane getScrollMenu() {
        if (scrollMenu == null) {
            scrollMenu = new JScrollPane(getTreeMenu());
        }

        return scrollMenu;
    }

    /**
     * Gets or creates the navigation tree for switching between configuration categories.
     * Creates a tree with three main nodes: General, Connections, and Drivers.
     * Connection names are loaded as child nodes under Connections.
     *
     * @return JXTree navigation component
     */
    private JXTree getTreeMenu() {
        if (treeMenu == null) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            DefaultMutableTreeNode general = new DefaultMutableTreeNode("General");
            connectionsNode = new DefaultMutableTreeNode("Connections");
            DefaultMutableTreeNode drivers = new DefaultMutableTreeNode("Drivers");

            root.add(general);
            root.add(connectionsNode);
            root.add(drivers);

            // Load all connection names as child nodes
            loadConnectionNodes();

            treeMenu = new JXTree(root);
            treeMenu.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            treeMenu.setRootVisible(false);
            treeMenu.setShowsRootHandles(true);
            treeMenu.getSelectionModel().setSelectionPath(new TreePath(general.getPath()));
            treeMenu.addTreeSelectionListener(e -> {
                TreePath tp = getTreeMenu().getSelectionPath();
                if (tp == null) {
                    return;
                }

                if (cardLayout == null) {
                    return;
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
                String nodeName = (String)node.getUserObject();

                // Check if this is a connection child node
                if (node.getParent() == connectionsNode) {
                    // This is a connection name, show connections card and load the config
                    cardLayout.show(getMainPanel(), CARD_CONNECTIONS);
                    getDbConfigPanel().loadSelectedConfig(nodeName);
                } else if (node == connectionsNode) {
                    // This is the Connections parent node, show connections card with empty form
                    cardLayout.show(getMainPanel(), CARD_CONNECTIONS);
                    getDbConfigPanel().clearForm();
                } else {
                    // This is a main category node
                    cardLayout.show(getMainPanel(), nodeName);
                }
            });
        }

        return treeMenu;
    }

    /**
     * Loads all connection names as child nodes under the Connections node.
     */
    private void loadConnectionNodes() {
        if (connectionsNode != null) {
            // Clear existing connection nodes
            connectionsNode.removeAllChildren();

            // Add a node for each connection
            for (String name : DbConfigFile.getConfigNames()) {
                DefaultMutableTreeNode connectionNode = new DefaultMutableTreeNode(name);
                connectionsNode.add(connectionNode);
            }
        }
    }

    /**
     * Refreshes the connection list in the tree.
     * Should be called after adding, deleting, or renaming connections.
     */
    public void refreshConnectionNodes() {
        loadConnectionNodes();
        if (treeMenu != null) {
            DefaultTreeModel model = (DefaultTreeModel) treeMenu.getModel();
            model.reload(connectionsNode);
            // Expand the connections node to show all connections
            treeMenu.expandPath(new TreePath(connectionsNode.getPath()));
        }
    }

    /**
     * Gets or creates the general configuration panel.
     * Contains settings for application-wide configuration options.
     *
     * @return GeneralConfigPanel instance
     */
    private GeneralConfigPanel getGeneralConfigPanel() {
        if (generalConfigPanel == null) {
            generalConfigPanel = new GeneralConfigPanel();
        }

        return generalConfigPanel;
    }

    /**
     * Gets or creates the database configuration panel.
     * Provides interface for managing database connection configurations.
     *
     * @return DbConfigPanel instance
     */
    private DbConfigPanel getDbConfigPanel() {
        if (dbConfigPanel == null) {
            dbConfigPanel = new DbConfigPanel(this);
        }

        return dbConfigPanel;
    }

    /**
     * Gets or creates the driver configuration panel.
     * Allows management of JDBC driver JAR files and classpath configuration.
     *
     * @return DriverConfigPanel instance
     */
    private DriverConfigPanel getDriverConfigPanel() {
        if (driverConfigPanel == null) {
            driverConfigPanel = new DriverConfigPanel();
        }

        return driverConfigPanel;
    }
}
