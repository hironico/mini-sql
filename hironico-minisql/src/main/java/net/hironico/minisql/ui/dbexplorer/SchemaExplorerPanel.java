package net.hironico.minisql.ui.dbexplorer;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.ObjectListCallable;
import net.hironico.minisql.ctrl.SchemaListCallable;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.config.ShowConfigPanelAction;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

public class SchemaExplorerPanel extends JPanel implements DbConfigFile.DbConfigFileListener {
    private static final Logger LOGGER = Logger.getLogger(SchemaExplorerPanel.class.getName());

    private DbConfig dbConfig = null;

    private JXComboBox cmbConnection = null;
    private JButton btnConnectionConfig = null;
    private DefaultComboBoxModel<DbConfig> cmbConnectionModel = null;
    private JXComboBox cmbSchema = null;
    private DefaultComboBoxModel<String> cmbSchemaModel = null;
    private JScrollPane scrollObjects = null;
    private JXTreeTable treetableObjects = null;
    private SQLObjectsTreeTableModel treetableObjectsModel = null;

    public SchemaExplorerPanel() {
        super();
        initialize();
        DbConfigFile.addListener(this);
    }

    private void refreshSchemas() {
        if (this.dbConfig == null) {
            this.getCmbSchemaModel().removeAllElements();
            return;
        }

        this.setEnabled(false);

        CompletableFuture.supplyAsync(new SchemaListCallable(this.dbConfig))
        .thenAccept(list -> SwingUtilities.invokeLater(() -> {
            DefaultComboBoxModel<String> model = getCmbSchemaModel();
            model.removeAllElements();
            list.forEach(model::addElement);
        })).whenComplete((result, ex) -> {
            SchemaExplorerPanel.this.setEnabled(true);
            if (ex != null) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the database objects.", ex);
            }
        });
    }

    private void refreshObjects(SQLObjectTypeEnum objectTypeFilter) {
        SQLObjectsTreeTableModel model = this.getTreeTableObjectsModel();

        String schemaName = (String)this.getCmbSchema().getSelectedItem();
        if (schemaName == null) {
            model.clear();
            return;
        }

        this.setEnabled(false);
        CompletableFuture.supplyAsync(new ObjectListCallable(this.dbConfig, schemaName, objectTypeFilter))
        .thenAccept(objects -> SwingUtilities.invokeLater(() -> {
            model.clear(objectTypeFilter);
            model.setSQLObjects(objects);
        })).whenComplete((result, ex) -> {
            SchemaExplorerPanel.this.setEnabled(true);
            if (ex != null) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the database objects.", ex);
            }
        });
    }

    public void refreshSelectedObject() {
        TreePath tp = getSelectionPath();
        if (tp == null) {
            return;
        }

        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode)tp.getPathComponent(1);
        String nodeType = (String)node.getUserObject();

        switch(nodeType.toUpperCase()) {
            case "TABLES":
                this.refreshObjects(SQLObjectTypeEnum.TABLE);
                break;

            case "VIEWS":
                this.refreshObjects(SQLObjectTypeEnum.VIEW);
                break;

            case "PROCEDURES":
            case "FUNCTIONS":
                this.refreshObjects(SQLObjectTypeEnum.PROCEDURE);
                this.refreshObjects(SQLObjectTypeEnum.FUNCTION);
                break;

            case "SEQUENCES":
                this.refreshObjects(SQLObjectTypeEnum.SEQUENCE);
                break;

            default:
                LOGGER.warning("Unknown object type to refresh: " + nodeType);
                break;
        }

        LOGGER.info("Should refresh node: " + nodeType);
    }

    public String getSelectedConnectionName() {
        Object item = getCmbConnection().getSelectedItem();
        return item == null ? null : item.toString();
    }

    public SQLObject getSelectedSQLObject() {
        TreePath tp = this.getSelectionPath();
        if (tp == null) {
            return null;
        }
        DefaultMutableTreeTableNode lastNode = (DefaultMutableTreeTableNode)tp.getLastPathComponent();
        Object obj = lastNode.getUserObject();
        return obj instanceof SQLObject ? (SQLObject)obj : null;
    }

    public TreePath getSelectionPath() {
        return getTreeTableObjects().getTreeSelectionModel().getSelectionPath();
    }

    @Override
    public void setEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            super.setEnabled(enabled);
            getCmbSchema().setEnabled(enabled);
            getCmbConnection().setEnabled(enabled);
            getTreeTableObjects().setEnabled(enabled);
        });
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        setBackground(new Color(236, 243, 250));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0d;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(getCmbConnection(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0d;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 5, 0, 0);
        add(getBtnConnectionConfig(), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1.0d;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,0,0,0);
        add(getCmbSchema(), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0d;
        gbc.weighty = 1.0d;
        gbc.gridwidth = 2;
        add(getScrollObjects(), gbc);
    }

    private JXComboBox getCmbConnection() {
        if (this.cmbConnection == null) {
            this.cmbConnection = new JXComboBox(getCmbConnectionModel());
            this.cmbConnection.setEditable(true);

            AutoCompleteDecorator.decorate(cmbConnection);

            this.cmbConnection.addActionListener((evt) -> {
                if (this.cmbConnection.getSelectedIndex() < 0) {
                    return;
                }
                this.dbConfig = (DbConfig)this.cmbConnection.getSelectedItem();
                SchemaExplorerPanel.this.refreshSchemas();
            });
        }

        return this.cmbConnection;
    }

    private JButton getBtnConnectionConfig() {
        if (this.btnConnectionConfig == null) {
            this.btnConnectionConfig = new JButton("");
            this.btnConnectionConfig.setToolTipText("Open config window to manage connections.");
            this.btnConnectionConfig.setBorderPainted(false);
            this.btnConnectionConfig.setContentAreaFilled(false);
            ShowConfigPanelAction action = new ShowConfigPanelAction();
            this.btnConnectionConfig.setPreferredSize(new Dimension(16,16));
            this.btnConnectionConfig.addActionListener(action);
            this.btnConnectionConfig.setIcon(action.getSmallIcon());
        }
        return this.btnConnectionConfig;
    }

    private DefaultComboBoxModel<DbConfig> getCmbConnectionModel() {
        if (this.cmbConnectionModel == null) {
            this.cmbConnectionModel = new DefaultComboBoxModel<>();
            DbConfigFile.getConfigNames().forEach(name -> this.cmbConnectionModel.addElement(DbConfigFile.getConfig(name)));
        }

        return this.cmbConnectionModel;
    }

    private JXComboBox getCmbSchema() {
        if (this.cmbSchema == null) {
            this.cmbSchema= new JXComboBox(getCmbSchemaModel());
            this.cmbSchema.setEditable(true);

            AutoCompleteDecorator.decorate(cmbSchema);

            this.cmbSchema.addActionListener((evt) -> {
                if (getCmbSchema().getSelectedIndex() < 0) {
                    return;
                }

                SchemaExplorerPanel.this.refreshObjects(null);
            });
        }
        return cmbSchema;
    }

    private javax.swing.DefaultComboBoxModel<String> getCmbSchemaModel() {
        if (this.cmbSchemaModel == null) {
            this.cmbSchemaModel = new javax.swing.DefaultComboBoxModel<>();
        }
        return this.cmbSchemaModel;
    }

    private JScrollPane getScrollObjects() {
        if (this.scrollObjects == null) {
            this.scrollObjects = new JScrollPane(getTreeTableObjects());
            this.scrollObjects.setBorder(BorderFactory.createEmptyBorder());
        }

        return scrollObjects;
    }

    private JXTreeTable getTreeTableObjects() {
        if (this.treetableObjects == null) {
            this.treetableObjects = new JXTreeTable(getTreeTableObjectsModel());
            this.treetableObjects.setEditable(false);

            this.treetableObjects.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    this.selectExplorerRibbonTab();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    this.selectExplorerRibbonTab();
                }

                private void selectExplorerRibbonTab() {
                    MainWindow.getInstance().getRibbon().setSelectedRibbonTab("Explorer");
                }
            });
        }
        return treetableObjects;
    }

    private SQLObjectsTreeTableModel getTreeTableObjectsModel() {
        if (this.treetableObjectsModel == null) {
            this.treetableObjectsModel = new SQLObjectsTreeTableModel();
        }

        return this.treetableObjectsModel;
    }

    public void collapseAll() {
        this.getTreeTableObjects().collapseAll();
    }

    public void expandAll() {
        this.getTreeTableObjects().expandAll();
    }

    @Override
    public void configAdded(DbConfig config) {
        this.getCmbConnectionModel().addElement(config);
    }

    @Override
    public void configRemoved(DbConfig config) {
        this.getCmbConnectionModel().removeElement(config);
    }

    public void setShowSystemObjects(boolean showSystemObjects) {
        this.getTreeTableObjectsModel().setShowSystemObjects(showSystemObjects);
    }
}
