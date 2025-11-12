package net.hironico.minisql.ui.batch;

import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.ShowQueryPanelAction;
import net.hironico.minisql.ui.editor.QueryPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Button cell editor for tree table cells that enables action buttons in batch file display.
 * This editor handles "Edit" and "Run" button clicks for batch file nodes, allowing
 * users to open files in the query editor or execute SQL files directly from the batch panel.
 */
class ButtonEditor extends DefaultCellEditor {
    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(ButtonEditor.class.getName());

    /** The button component used for editing */
    private final JButton button;

    /** Reference to the tree table this editor is used with */
    private final JXTreeTable treeTable;

    /** Current label text for the button */
    private String label;

    /** Flag indicating if the button has been pushed */
    private boolean isPushed;

    /** The row currently being edited */
    private int editingRow;

    /**
     * Constructs a new ButtonEditor with the specified checkbox and tree table.
     *
     * @param checkBox the checkbox component used for default editing behavior
     * @param treeTable the tree table this editor will work with
     */
    public ButtonEditor(JCheckBox checkBox, JXTreeTable treeTable) {
        super(checkBox);
        this.treeTable = treeTable;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    /**
     * Gets the component for editing the cell with a button.
     *
     * @param table the JTable instance
     * @param value the current cell value
     * @param isSelected true if the cell is selected
     * @param row the row index
     * @param column the column index
     * @return the JButton component for editing
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        editingRow = row;
        return button;
    }

    /**
     * Gets the current value of the cell editor and performs action if button was pushed.
     *
     * @return the cell value
     */
    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            performAction();
        }
        isPushed = false;
        return label;
    }

    /**
     * Stops cell editing and resets the pushed flag.
     *
     * @return true to indicate editing should stop
     */
    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    /**
     * Performs the appropriate action based on the button label.
     * Retrieves the file node associated with the edited row and
     * executes either an edit or run action.
     */
    private void performAction() {
        // Get the file node from the tree table
        int modelRow = treeTable.convertRowIndexToModel(editingRow);
        Object node = treeTable.getPathForRow(modelRow).getLastPathComponent();

        if (!(node instanceof DefaultMutableTreeTableNode treeNode)) {
            return;
        }

        if (!(treeNode.getUserObject() instanceof BatchFileNode fileNode)) {
            return;
        }

        // Perform action based on button label
        SwingUtilities.invokeLater(() -> {
            if ("Edit".equals(label)) {
                handleEdit(fileNode);
            } else if ("Run".equals(label)) {
                handleRun(fileNode);
            }
        });
    }

    /**
     * Handles the "Edit" action by opening the file in a new query panel.
     *
     * @param fileNode the batch file node to edit
     */
    private void handleEdit(BatchFileNode fileNode) {
        File file = fileNode.getFile();
        LOGGER.info("Edit action for file: " + file.getName());

        ShowQueryPanelAction openQueryEditorAction = new ShowQueryPanelAction();
        openQueryEditorAction.actionPerformed(null);

        QueryPanel queryPanel = openQueryEditorAction.getLastOpenedQueryPanel();
        queryPanel.loadFile(file);
    }

    /**
     * Handles the "Run" action by executing the SQL file and updating the result in the tree table.
     *
     * @param fileNode the batch file node to execute
     */
    private void handleRun(BatchFileNode fileNode) {
        LOGGER.info("Run action for file: " + fileNode.getFileName());

        BatchFileTreeTableModel model = (BatchFileTreeTableModel) treeTable.getTreeTableModel();

        TreePath treePath = treeTable.getPathForRow(editingRow);
        Object treeNode = treePath.getLastPathComponent();

        if (! (treeNode instanceof DefaultMutableTreeTableNode treeTableNode)) {
            LOGGER.severe("Last path component is not a DefaultMutableTreeTableNode.");
            return;
        }

        if (! (treeTableNode.getUserObject() instanceof BatchFileNode batchFileNode)) {
            LOGGER.severe("Tree node user object i snot a BatchFileNode." + treeNode.getClass().getName());
            return;
        }

        if (batchFileNode.isDirectory()) {
            LOGGER.severe("Running run action on a directory !");
            return;
        }

        model.setValueAt("Wait...", treeNode, 3);
        treeTable.repaint();

        Future<List<SQLResultSetTableModel>> fut = batchFileNode.run(model.getDbConfig());
        model.updateWhenFinished(fut, batchFileNode, treePath);
    }
}
