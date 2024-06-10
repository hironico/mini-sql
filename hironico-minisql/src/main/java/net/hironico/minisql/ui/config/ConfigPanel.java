package net.hironico.minisql.ui.config;

import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Panel to display the whole mini sql configuration : ui and connections
 */
public class ConfigPanel extends JPanel {

    private JXTree treeMenu = null;
    private JScrollPane scrollMenu = null;

    private CardLayout cardLayout = null;
    private JPanel mainPanel = null;

    private final String CARD_GENERAL = "General";
    private final String CARD_CONNECTIONS = "Connections";

    private GeneralConfigPanel generalConfigPanel = null;
    private DbConfigPanel dbConfigPanel = null;

    public ConfigPanel() {
        super();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

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

            root.add(general);
            root.add(connections);

            treeMenu = new JXTree(root);
            treeMenu.setRootVisible(false);
            treeMenu.getSelectionModel().setSelectionPath(new TreePath(general.getPath()));
            treeMenu.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
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
                }
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
}
