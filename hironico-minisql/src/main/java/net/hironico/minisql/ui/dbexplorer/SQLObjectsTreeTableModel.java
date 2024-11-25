package net.hironico.minisql.ui.dbexplorer;

import net.hironico.common.utils.StreamUtils;
import net.hironico.minisql.model.SQLObject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.hironico.minisql.model.SQLObjectTypeEnum;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public class SQLObjectsTreeTableModel extends DefaultTreeTableModel {
    private static final Logger LOGGER = Logger.getLogger(SQLObjectsTreeTableModel.class.getName());

    private DefaultMutableTreeTableNode tablesNode;
    private DefaultMutableTreeTableNode viewsNode;
    private DefaultMutableTreeTableNode procsNode;
    private DefaultMutableTreeTableNode sequencesNode;
    private DefaultMutableTreeTableNode enumsNode;

    private boolean showSystemObjects = Boolean.FALSE;

    private final Class<?>[] columnClass = {
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

        if (!(userObject instanceof SQLObject sqlObj)) {
            LOGGER.severe("Unknown user object in the tree table of the object explorer: " + userObject.getClass().getName());
            return null;
        }

        return switch (col) {
            case 0 -> sqlObj.name;
            case 1 -> sqlObj.type;
            default -> {
                LOGGER.warning("Invalid column for common object tree table model: " + col);
                yield null;
            }
        };
    }

    private void addRootNodes() {
        this.addTablesRootNode();
        this.addViewsRootNode();
        this.addProcsRootNode();
        this.addSequencesRootNode();
        this.addEnumsRootNode();
    }

    private void addTablesRootNode() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        tablesNode = new DefaultMutableTreeTableNode("Tables");
        this.insertNodeInto(tablesNode, root, 0);
    }

    private void addViewsRootNode() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        viewsNode = new DefaultMutableTreeTableNode("Views");
        this.insertNodeInto(viewsNode, root, 1);
    }

    private void addProcsRootNode() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        procsNode = new DefaultMutableTreeTableNode("Procedures");
        this.insertNodeInto(procsNode, root, 2);
    }

    private void addSequencesRootNode() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        sequencesNode = new DefaultMutableTreeTableNode("Sequences");
        this.insertNodeInto(sequencesNode, root, 3);
    }

    private void addEnumsRootNode() {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode)getRoot();
        enumsNode = new DefaultMutableTreeTableNode("Enums");
        this.insertNodeInto(enumsNode, root, 3);
    }

    public void clear() {
        this.clear(null);
    }

    public void clear(SQLObjectTypeEnum objectFilter) {
        if (objectFilter == null || objectFilter == SQLObjectTypeEnum.TABLE) {
            List<MutableTreeTableNode> nodes = StreamUtils.stream(this.tablesNode.children()).collect(Collectors.toList());
            nodes.forEach(this::removeNodeFromParent);
        }
        if (objectFilter == null || objectFilter == SQLObjectTypeEnum.VIEW) {
            List<MutableTreeTableNode> nodes = StreamUtils.stream(this.viewsNode.children()).collect(Collectors.toList());
            nodes.forEach(this::removeNodeFromParent);
        }

        if (objectFilter == null || objectFilter == SQLObjectTypeEnum.PROCEDURE) {
            List<MutableTreeTableNode> nodes = StreamUtils.stream(this.procsNode.children()).collect(Collectors.toList());
            nodes.forEach(this::removeNodeFromParent);
        }

        if (objectFilter == null || objectFilter == SQLObjectTypeEnum.SEQUENCE) {
            List<MutableTreeTableNode> nodes = StreamUtils.stream(this.sequencesNode.children()).collect(Collectors.toList());
            nodes.forEach(this::removeNodeFromParent);
        }

        if (objectFilter == null || objectFilter == SQLObjectTypeEnum.ENUM) {
            List<MutableTreeTableNode> nodes = StreamUtils.stream(this.enumsNode.children()).collect(Collectors.toList());
            nodes.forEach(this::removeNodeFromParent);
        }
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
            SQLObject myObj = this.createSQLObject(sqlObj);
            DefaultMutableTreeTableNode myObjNode = this.getSQLObjectParentNode(myObj);
            if (myObjNode != null) {
                this.addSQLObject(myObjNode, myObj);
            }
        });
    }

    private DefaultMutableTreeTableNode getSQLObjectParentNode(SQLObject myObj) {
        if (myObj == null) {
            return null;
        }
        switch(myObj.type) {
            case TABLE:
            case SYNONYM:
                return this.tablesNode;

            case SYSTEM_TABLE:
                if (this.showSystemObjects) {
                    return this.tablesNode;
                }
                break;

            case VIEW:
            case MATERIALIZED_VIEW:
                return this.viewsNode;

            case SYSTEM_VIEW:
                if (this.showSystemObjects) {
                    return this.viewsNode;
                }
                break;

            case PROCEDURE:
            case FUNCTION:
                return this.procsNode;

            case SYSTEM_PROCEDURE:
            case SYSTEM_FUNCTION:
                if (this.showSystemObjects) {
                    return this.procsNode;
                }
                break;

            case SEQUENCE:
                return this.sequencesNode;

            case SYSTEM_SEQUENCE:
                if (this.showSystemObjects) {
                    return this.sequencesNode;
                }
                break;

            case INDEX:
            case SYSTEM_INDEX:
                // intentionally left apart because we display indexes as part of the
                // tables and materialized views structures
                break;

            case ENUM:
                return this.enumsNode;

            default:
                LOGGER.severe("Unsupported object type: " + myObj.type);
                break;
        }

        // default is null means that there is no need to display this sql object
        return null;
    }

    private SQLObject createSQLObject(String[] infos) {
        try {
            SQLObject obj = new SQLObject();
            obj.schemaName = infos[0];
            obj.name = infos[1];
            obj.type = SQLObjectTypeEnum.valueOfStr(infos[2]);
            return obj;
        } catch (IllegalArgumentException iae) {
            // silently return null for unsupported object type
            return null;
        }
    }

    /**
     * Add display support for objects type like SYSTEM_* in the SQLObjectType enum.
     * If set to false (default) system objects are not shown
     * @param showSystemObjects set to true in order to show system tables, views, procedures, functions...
     */
    public void setShowSystemObjects(boolean showSystemObjects) {
        this.showSystemObjects = showSystemObjects;
    }
}
