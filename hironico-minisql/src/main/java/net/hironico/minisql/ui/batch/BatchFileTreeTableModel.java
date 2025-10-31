package net.hironico.minisql.ui.batch;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLResultSetTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TreeTableModel for batch files.
 * Provides a hierarchical view of batch files organized by directories.
 * Columns: FileName, Edit, Run, Result, Duration
 */
class BatchFileTreeTableModel extends DefaultTreeTableModel {
    private static final Logger LOG = Logger.getLogger(BatchFileTreeTableModel.class.getName());

    /** Column classes for the tree table model */
    private final Class<?>[] columnClasses = {
            String.class,  // FileName or Directory Name
            JButton.class, // Edit
            JButton.class, // Run
            String.class,  // Result
            String.class     // duration
    };

    /** Database configuration for batch execution */
    private DbConfig dbConfig = null;

    /**
     * Constructs a new BatchFileTreeTableModel with default root node and column headers.
     */
    public BatchFileTreeTableModel() {
        super(new DefaultMutableTreeTableNode("ROOT"),
                Arrays.asList("FileName", "Edit", "Run", "Result", "Duration"));
    }

    /**
     * Gets the class type for the specified column.
     * 
     * @param col the column index
     * @return the Class type for the column
     */
    @Override
    public Class<?> getColumnClass(int col) {
        return columnClasses[col];
    }

    /**
     * Determines if a cell is editable based on the node type and column.
     * Only Edit and Run columns are editable for file nodes (not directories).
     * 
     * @param node the tree node to check
     * @param column the column index to check
     * @return true if the cell is editable, false otherwise
     */
    @Override
    public boolean isCellEditable(Object node, int column) {
        DefaultMutableTreeTableNode treeNode = (DefaultMutableTreeTableNode) node;

        if (!(treeNode.getUserObject() instanceof BatchFileNode fileNode)) {
            return false;
        }

        if (fileNode.isDirectory()) {
            return false;
        }

        // Only Edit and Run columns are editable for files only
        return column == 1 || column == 2;
    }

    /**
     * Gets the value for a specific cell in the tree table.
     * Returns appropriate values based on column and node type.
     * 
     * @param node the tree node
     * @param col the column index
     * @return the value at the specified cell
     */
    @Override
    public Object getValueAt(Object node, int col) {
        DefaultMutableTreeTableNode treeNode = (DefaultMutableTreeTableNode) node;

        if (!(treeNode.getUserObject() instanceof BatchFileNode fileNode)) {
            return null;
        }

        return switch (col) {
            case 0 -> fileNode.getFileName();
            case 1 -> fileNode.isDirectory() ? null : "Edit";   // Button label
            case 2 -> fileNode.isDirectory() ? null : "Run";    // Button label
            case 3 -> fileNode.isDirectory() ? "" : fileNode.getResult();
            case 4 -> fileNode.isDirectory() ? "" : String.format("%d ms", fileNode.getDuration());
            default -> null;
        };
    }

    /**
     * Sets the value for a specific cell in the tree table.
     * Currently only handles setting the result value for file nodes.
     * 
     * @param value the value to set
     * @param node the tree node
     * @param column the column index
     */
    @Override
    public void setValueAt(Object value, Object node, int column) {
        DefaultMutableTreeTableNode treeNode = (DefaultMutableTreeTableNode) node;

        if (!(treeNode.getUserObject() instanceof BatchFileNode fileNode)) {
            return;
        }

        if (column == 3) {
            fileNode.setResult((String) value);

            ArrayList<DefaultMutableTreeTableNode> parentPath = new ArrayList<>();
            DefaultMutableTreeTableNode currentNode = treeNode;
            parentPath.add(0, currentNode);
            while(currentNode.getParent() != null) {
                currentNode = (DefaultMutableTreeTableNode) currentNode.getParent();
                parentPath.add(0, currentNode);
            }

            TreePath tp = new TreePath(parentPath);
            modelSupport.fireChildChanged(tp, column, node);
        }
    }

    /**
     * Clears all nodes from the tree table model.
     * Removes all child nodes from the root.
     */
    public void clear() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();
        while (root.getChildCount() > 0) {
            removeNodeFromParent((DefaultMutableTreeTableNode) root.getChildAt(0));
        }
    }

    /**
     * Resets the results and timing information for all file nodes.
     * Clears result strings and resets execution timestamps.
     */
    public void resetResults() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();
        
        // Iterate through all folder nodes
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeTableNode folderNode = (DefaultMutableTreeTableNode) root.getChildAt(i);
            
            // Iterate through all file nodes in this folder
            for (int j = 0; j < folderNode.getChildCount(); j++) {
                DefaultMutableTreeTableNode fileNode = (DefaultMutableTreeTableNode) folderNode.getChildAt(j);
                
                if (fileNode.getUserObject() instanceof BatchFileNode batchFileNode) {
                    if (!batchFileNode.isDirectory()) {
                        // Reset the result to empty string and the user object value as well
                        setValueAt("", fileNode, 3);
                        setValueAt("", fileNode, 4);

                        batchFileNode.setResult("");
                        batchFileNode.setStarted(0);
                        batchFileNode.setEnded(0);
                    }
                }
            }
        }
    }

    /**
     * Adds a file to the appropriate folder node in the tree table.
     * Creates a folder node if it doesn't exist for the file's parent directory.
     * 
     * @param file the file to add to the tree table
     */
    public void addFile(File file) {
        DefaultMutableTreeTableNode dirNode = findFolderNode(file);
        BatchFileNode batchFileNode = new BatchFileNode(file);
        insertNodeInto(new DefaultMutableTreeTableNode(batchFileNode), dirNode, dirNode.getChildCount());
    }

    /**
     * Finds or creates a folder node for the specified file's parent directory.
     * Searches existing folder nodes and creates a new one if not found.
     * 
     * @param file the file whose parent directory node is needed
     * @return the folder node for the file's parent directory
     */
    private DefaultMutableTreeTableNode findFolderNode(File file) {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();
        for(int index = 0; index < root.getChildCount(); index++) {
            DefaultMutableTreeTableNode dirNode = (DefaultMutableTreeTableNode)root.getChildAt(index);
            BatchFileNode fileNode = (BatchFileNode) dirNode.getUserObject();
            if (fileNode.getFile().equals(file.getParentFile())) {
                return dirNode;
            }
        }

        DefaultMutableTreeTableNode dirNode = new DefaultMutableTreeTableNode();
        dirNode.setUserObject(new BatchFileNode(file.getParentFile()));
        insertNodeInto(dirNode, root, root.getChildCount());

        return dirNode;
    }

    /**
     * Gets the current database configuration.
     * 
     * @return the DbConfig instance, or null if not set
     */
    public DbConfig getDbConfig() {
        return dbConfig;
    }

    /**
     * Sets the database configuration for batch execution.
     * 
     * @param dbConfig the database configuration to set
     */
    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Executes all batch files in the tree table.
     * Iterates through all file nodes and triggers asynchronous execution.
     */
    public void runAll() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) getRoot();
        // Iterate through all folder nodes
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeTableNode folderNode = (DefaultMutableTreeTableNode) root.getChildAt(i);

            // Iterate through all file nodes in this folder
            for (int j = 0; j < folderNode.getChildCount(); j++) {
                DefaultMutableTreeTableNode fileNode = (DefaultMutableTreeTableNode) folderNode.getChildAt(j);

                if (fileNode.getUserObject() instanceof BatchFileNode batchFileNode) {
                    if (!batchFileNode.isDirectory()) {
                        TreePath tp = new TreePath(getPathToRoot(fileNode));
                        batchFileNode.setResult("Wait...");
                        modelSupport.firePathChanged(tp);

                        // launch run and store into global status
                        Future<List<SQLResultSetTableModel>> fut = batchFileNode.run(this.getDbConfig());
                        updateWhenFinished(fut, batchFileNode, tp);
                    }
                }
            }
        }
    }

    /**
     * Updates the batch file node when execution is completed.
     * Monitors the Future result and updates the node with execution results.
     * 
     * @param fut the Future representing the execution result
     * @param batchFileNode the batch file node to update
     * @param pathToRoot the tree path to the node for UI updates
     */
    public void updateWhenFinished(Future<List<SQLResultSetTableModel>> fut, BatchFileNode batchFileNode, TreePath pathToRoot) {
        Thread thread = new Thread(() -> {
            try {
                if (fut != null) {
                    // maybe we can store the results table model for debugging later in the GUI to display the details
                    List<SQLResultSetTableModel> results = fut.get();
                    batchFileNode.setEnded(System.currentTimeMillis());
                    String strResult = String.format("OK. %d results.", results.size());
                    batchFileNode.setResult(strResult);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.log(Level.SEVERE, "ERROR while running batch of SQL:", e);
                batchFileNode.setResult(String.format("ERROR: %s", e.getMessage()));
            } finally {
                // force display of results
                modelSupport.fireTreeStructureChanged(pathToRoot);
            }
        });
        thread.start();
    }
}
