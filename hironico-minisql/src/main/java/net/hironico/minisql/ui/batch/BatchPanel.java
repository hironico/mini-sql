package net.hironico.minisql.ui.batch;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ui.MainWindow;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * BatchPanel provides a tree table interface for managing batch files in a folder.
 * The tree table displays files with columns: FileName, Edit, Run, Result.
 * Edit and Run columns are rendered as buttons for user interaction.
 */
public class BatchPanel extends JRoundedPanel {
    private static final Logger LOGGER = Logger.getLogger(BatchPanel.class.getName());
    
    private JXTreeTable treeTable;
    private JToolBar toolbar;
    private JComboBox<String> cmbConfig;
    private BatchFileTreeTableModel model;
    private File lastUsedFolder;
    
    public BatchPanel() {
        super();
        initialize();
    }

    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        setLayout(new BorderLayout());
        
        // Create the tree table model
        model = new BatchFileTreeTableModel();
        
        // Create the tree table
        treeTable = new JXTreeTable(model);
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        
        // Configure column renderers and editors
        configureColumns();

        // add toolbar on top
        add(getToolbar(), BorderLayout.NORTH);

        // Add to panel with scroll pane
        JScrollPane scrollPane = new JScrollPane(treeTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JToolBar getToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.add(getCmbConfig());
        }

        return toolbar;
    }

    private JComboBox<String> getCmbConfig() {
        if (cmbConfig == null) {
            cmbConfig = new JComboBox<>();
            for (String cfg : DbConfigFile.getConfigNames()) {
                cmbConfig.addItem(cfg);
            }
            cmbConfig.addItemListener(e -> {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    setEditorTabTitle();
                    String configName = (String)e.getItem();
                    DbConfig dbConfig = DbConfigFile.getConfig(configName);
                    model.setDbConfig(dbConfig);
                }
            });

            // init model with first db config
            String currentDbConfig = (String)cmbConfig.getSelectedItem();
            model.setDbConfig(DbConfigFile.getConfig(currentDbConfig));
        }

        return cmbConfig;
    }

    private void setEditorTabTitle() {
        String title = String.format("Batch %s", getCmbConfig().getSelectedItem());
        MainWindow.getInstance().setEditorTabTitle(BatchPanel.this, title);
    }

    /**
     * Reset the result for loaded files in this batch
     */
    public void resetResults() {
        model.resetResults();
        treeTable.repaint();
    }

    /**
     * Removes all the files from this batch
     */
    public void clear() {
        model.clear();
        treeTable.repaint();
    }
    
    /**
     * Load files from the specified folder into the tree table.
     * @param folder The folder to load files from
     */
    public void loadDirectory(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            LOGGER.warning("Invalid folder: " + folder);
            return;
        }

        this.lastUsedFolder = folder;

        File[] files = folder.listFiles();
        this.loadFiles(files);
    }

    /**
     * Load the given files into the tree table
     * @param files the array of files to add in the batch execution panel
     */
    public void loadFiles(File[] files) {

        // index the Directories (1st level in the tree)
        BatchFileTreeTableModel model = (BatchFileTreeTableModel) treeTable.getTreeTableModel();

        // for each file add to the directory node depending on its parent
        if (files != null) {
            Arrays.stream(files)
                    .filter(File::isFile)
                    .forEach(model::addFile);
        } else {
            LOGGER.severe("Cannot load files from empty or null file list. Empty directory?");
        }
        treeTable.expandAll();
    }
    
    private void configureColumns() {
        // FileName column (0) - default renderer
        treeTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        
        // Edit column (1) - button renderer and editor
        treeTable.getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
        treeTable.getColumnModel().getColumn(1).setCellEditor(new ButtonEditor(new JCheckBox(), treeTable));
        treeTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        treeTable.getColumnModel().getColumn(1).setMaxWidth(100);
        
        // Run column (2) - button renderer and editor
        treeTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        treeTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), treeTable));
        treeTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        treeTable.getColumnModel().getColumn(2).setMaxWidth(100);
        
        // Result column (3) - default renderer
        treeTable.getColumnModel().getColumn(3).setPreferredWidth(200);
    }

    /**
     * Gets the last used folder when loading a folder's contents or choosing an individual file
     * @return File représentation of the last used folder to load files into this batch panel
     */
    public File getLastUsedFolder() {
        return lastUsedFolder;
    }

    /**
     * Return the selected database config object if there is one selected in the combo box of this panel,
     * @return DbConfig currently selected or null of none available.
     */
    public DbConfig getSelectedDbConfig() {
        if (getCmbConfig().getItemCount() <= 0) {
            LOGGER.warning("No db config in this batch panel. Returning null.");
            return null;
        }
        String dbConfigName = (String)getCmbConfig().getSelectedItem();
        LOGGER.info("Batch panel db config: " + dbConfigName);
        return DbConfigFile.getConfig(dbConfigName);
    }

    public void runAll() {
        BatchFileTreeTableModel model = (BatchFileTreeTableModel) treeTable.getTreeTableModel();
        model.setDbConfig(this.getSelectedDbConfig());
        model.runAll();
    }

    public void updateRibbon() {
        RibbonTab ribbonTab = MainWindow.getInstance().getRibbon().setSelectedRibbonTab("Batch");
        ribbonTab.updateDisplay();
    }
}
