package ch.ubp.pms.minisql.ui.dbexplorer;

import ch.ubp.pms.minisql.model.SQLObject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class SQLObjectsTreeTableModel extends DefaultTreeTableModel {
    private static final Logger LOGGER = Logger.getLogger(SQLObjectsTreeTableModel.class.getName());

    private DefaultMutableTreeTableNode tablesNode;
    private DefaultMutableTreeTableNode viewsNode;
    private DefaultMutableTreeTableNode procsNode;

    private Class<?>[] columnClass = {
            String.class, String.class
    };

    public SQLObjectsTreeTableModel() {
        super(new DefaultMutableTreeTableNode("ROOT"), Arrays.asList("Name", "Type"));
        addRootNodes();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return columnClass[col];
    }


    @Override
    public Object getValueAt(Object node, int col) {
        DefaultMutableTreeTableNode objNode = (DefaultMutableTreeTableNode)node;

        if (objNode.getUserObject() instanceof String) {
            return col == 0 ? objNode.getUserObject() : null;
        }

        Object userObject = objNode.getUserObject();
        if (userObject instanceof String) {
            return userObject;
        }

        if (!(userObject instanceof SQLObject)) {
            LOGGER.severe("Unknow user object in the tree table of the object explorer: " + userObject.getClass().getName());
            return null;
        }

        SQLObject sqlObj = (SQLObject)userObject;
        switch(col) {
            case 0:
                return sqlObj.name;

            case 1:
                return sqlObj.type;

            default:
                LOGGER.warning("Invalid column for sql object tree table model: " + col);
                return null;
        }
    }

    private void addRootNodes() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        tablesNode = new DefaultMutableTreeTableNode("Tables");
        viewsNode = new DefaultMutableTreeTableNode("Views");
        procsNode = new DefaultMutableTreeTableNode("Procedures");

        this.insertNodeInto(tablesNode, root, 0);
        this.insertNodeInto(viewsNode, root, 1);
        this.insertNodeInto(procsNode, root, 2);
    }


    public void clear() {
        this.removeNodeFromParent(this.tablesNode);
        this.removeNodeFromParent(this.viewsNode);
        this.removeNodeFromParent(this.procsNode);

        this.addRootNodes();
    }

    private void addSQLObject(DefaultMutableTreeTableNode parent, SQLObject obj) {
        DefaultMutableTreeTableNode objNode = new DefaultMutableTreeTableNode(obj);
        this.insertNodeInto(objNode, parent, parent.getChildCount());
    }

    public void setSQLObjects(List<String[]> objects) {
        if (objects == null) {
            return;
        }

        objects.forEach(sqlObj -> {
            String type = sqlObj[2];
            SQLObject myObj = this.createSQLObject(sqlObj);
            switch(type) {
                case "TABLE":
                    this.addSQLObject(this.tablesNode, myObj);
                    break;

                case "VIEW":
                    this.addSQLObject(this.viewsNode, myObj);
                    break;

                case "PROCEDURE":
                    this.addSQLObject(this.procsNode, myObj);
                    break;

                default:
                    LOGGER.severe("Unknown object type: " + type);
                    break;
            }
        });
    }

    private SQLObject createSQLObject(String[] infos) {
        SQLObject obj = new SQLObject();
        obj.schemaName = infos[0];
        obj.name = infos[1];
        obj.type = infos[2];
        return obj;
    }
}
