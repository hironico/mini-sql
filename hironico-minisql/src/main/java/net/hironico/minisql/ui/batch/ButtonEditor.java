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
 * Button editor for tree table cells.
 */
class ButtonEditor extends DefaultCellEditor {
    private static final Logger LOGGER = Logger.getLogger(ButtonEditor.class.getName());

    private final JButton button;
    private final JXTreeTable treeTable;
    private String label;
    private boolean isPushed;
    private int editingRow;

    public ButtonEditor(JCheckBox checkBox, JXTreeTable treeTable) {
        super(checkBox);
        this.treeTable = treeTable;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        editingRow = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            performAction();
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

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

    private void handleEdit(BatchFileNode fileNode) {
        File file = fileNode.getFile();
        LOGGER.info("Edit action for file: " + file.getName());

        ShowQueryPanelAction openQueryEditorAction = new ShowQueryPanelAction();
        openQueryEditorAction.actionPerformed(null);

        QueryPanel queryPanel = openQueryEditorAction.getLastOpenedQueryPanel();
        queryPanel.loadFile(file);
    }

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