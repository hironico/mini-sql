package net.hironico.minisql.ui.config;

import net.hironico.common.swing.JRoundedPanel;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Panel to display the whole mini sql configuration : ui and connections
 */
public class ConfigPanel extends JRoundedPanel {

    private JXTree treeMenu = null;
    private JScrollPane scrollMenu = null;

    private CardLayout cardLayout = null;
    private JPanel mainPanel = null;

    private final String CARD_GENERAL = "General";
    private final String CARD_CONNECTIONS = "Connections";
    private final String CARD_DRIVERS = "Drivers";

    private GeneralConfigPanel generalConfigPanel = null;
    private DbConfigPanel dbConfigPanel = null;
    private DriverConfigPanel driverConfigPanel = null;

    public ConfigPanel() {
        super();
        initialize();
    }

    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        setLayout(new BorderLayout(5, 0));

        add(getScrollMenu(), BorderLayout.WEST);
        add(getMainPanel(), BorderLayout.CENTER);
    }

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

    private JScrollPane getScrollMenu() {
        if (scrollMenu == null) {
            scrollMenu = new JScrollPane(getTreeMenu());
        }

        return scrollMenu;
    }

    private JXTree getTreeMenu() {
        if (treeMenu == null) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            DefaultMutableTreeNode general = new DefaultMutableTreeNode("General");
            DefaultMutableTreeNode connections = new DefaultMutableTreeNode("Connections");
            DefaultMutableTreeNode drivers = new DefaultMutableTreeNode("Drivers");

            root.add(general);
            root.add(connections);
            root.add(drivers);

            treeMenu = new JXTree(root);
            treeMenu.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            treeMenu.setRootVisible(false);
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
                String cardName = (String)node.getUserObject();
                cardLayout.show(getMainPanel(), cardName);
            });
        }

        return treeMenu;
    }

    private GeneralConfigPanel getGeneralConfigPanel() {
        if (generalConfigPanel == null) {
            generalConfigPanel = new GeneralConfigPanel();
        }

        return generalConfigPanel;
    }

    private DbConfigPanel getDbConfigPanel() {
        if (dbConfigPanel == null) {
            dbConfigPanel = new DbConfigPanel();
        }

        return dbConfigPanel;
    }

    private DriverConfigPanel getDriverConfigPanel() {
        if (driverConfigPanel == null) {
            driverConfigPanel = new DriverConfigPanel();
        }

        return driverConfigPanel;
    }
}
