package de.hameister.treetable;
 
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
 
public class MyTreeTableSelectionModel extends DefaultTreeSelectionModel {
 
    private static final long serialVersionUID = 1L;

	public MyTreeTableSelectionModel() {
        super();
 
        getListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
            }
        });
    }
 
    public ListSelectionModel getListSelectionModel() {
        return listSelectionModel;
    }

    public Object getSelectedNode() {
        TreePath tp = getSelectionPath();
        return tp == null ? null : tp.getLastPathComponent();
    }
}