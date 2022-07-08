package de.hameister.treetable;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.tree.TreePath;

public class MyTreeTable extends JTable {

    private static final long serialVersionUID = 1L;

    private MyTreeTableCellRenderer tree;
    private MyTreeTableSelectionModel treeTableSelectionModel;
    private MyAbstractTreeTableModel treeTableModel;

    public MyTreeTable(MyAbstractTreeTableModel treeTableModel) {
        super();

        // JTree erstellen.
        tree = new MyTreeTableCellRenderer(this, treeTableModel);

        // Modell setzen.
        super.setModel(new MyTreeTableModelAdapter(treeTableModel, tree));

        // Gleichzeitiges Selektieren fuer Tree und Table.
        treeTableSelectionModel = new MyTreeTableSelectionModel();
        tree.setSelectionModel(treeTableSelectionModel); //For the tree
        setSelectionModel(treeTableSelectionModel.getListSelectionModel()); //For the table

        // Renderer fuer den Tree.
        setDefaultRenderer(MyTreeTableModel.class, tree);
        // Editor fuer die TreeTable
        setDefaultEditor(MyTreeTableModel.class, new MyTreeTableCellEditor(tree, this));

        // Kein Grid anzeigen.
        setShowGrid(false);

        // Keine Abstaende.
        setIntercellSpacing(new Dimension(0, 0));

        this.treeTableModel = treeTableModel;
    }

    public MyAbstractTreeTableModel getTreeTableModel() {
        return this.treeTableModel;
    }

    public MyTreeTableSelectionModel getTreeTableSelectionModel() {
        return this.treeTableSelectionModel;
    }

    public Object getSelectedNode() {
        MyTreeTableSelectionModel selectionModel = getTreeTableSelectionModel();
        return selectionModel.getSelectedNode();
    }

    public TreePath getSelectionPath() {
        return tree.getSelectionPath();
    }

    public void expandPath(TreePath tp) {
        tree.expandPath(tp);
    }

    public void collapsePath(TreePath tp) {
        tree.collapsePath(tp);
    }
}