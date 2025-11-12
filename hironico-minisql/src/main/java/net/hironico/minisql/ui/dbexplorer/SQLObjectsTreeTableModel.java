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

/**
 * Tree table model for displaying database objects in a hierarchical structure.
 * Organizes SQL objects (tables, views, procedures, sequences, enums) by type
 * with support for filtering system objects and providing a structured view suitable
 * for database browsing and navigation.
 */
public class SQLObjectsTreeTableModel extends DefaultTreeTableModel {

    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(SQLObjectsTreeTableModel.class.getName());

    /** Root node for displaying tables and related objects */
    private DefaultMutableTreeTableNode tablesNode;

    /** Root node for displaying view objects */
    private DefaultMutableTreeTableNode viewsNode;

    /** Root node for displaying procedure and function objects */
    private DefaultMutableTreeTableNode procsNode;

    /** Root node for displaying sequence objects */
    private DefaultMutableTreeTableNode sequencesNode;

    /** Root node for displaying enum objects */
    private DefaultMutableTreeTableNode enumsNode;

    /** Flag controlling whether system objects are displayed */
    private boolean showSystemObjects = Boolean.FALSE;

    /** Column class definitions for the tree table */
    private final Class<?>[] columnClass = {
            String.class, String.class
    };

    /**
     * Constructs a new SQLObjectsTreeTableModel with default root structure.
     * Creates a root node and initializes all category nodes for organizing SQL objects.
     */
    public SQLObjectsTreeTableModel() {
        super(new DefaultMutableTreeTableNode("ROOT"), Arrays.asList("Name", "Type"));
        addRootNodes();
    }

    /**
     * Gets the class type for the specified column.
     *
     * @param col the column index
     * @return the Class type for the column
     */
    @Override
    public Class<?> getColumnClass(int col) {
        return columnClass[col];
    }

    /**
     * Gets the value at the specified cell in the tree table.
     * Handles different types of nodes: category nodes (Strings) and SQL object nodes.
     * For category nodes, returns the category name in column 0.
     * For SQL object nodes, returns the object name in column 0 and type in column 1.
     *
     * @param node the tree table node
     * @param col the column index (0 = Name, 1 = Type)
     * @return the cell value, or null for invalid cases
     */
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

    /**
     * Adds all root category nodes to the tree structure.
     * Creates the main organizational nodes for different types of SQL objects.
     */
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

    /**
     * Clears all objects from the tree table model.
     * Removes all child nodes from all category nodes.
     */
    public void clear() {
        this.clear(null);
    }

    /**
     * Clears objects of a specific type from the tree table model.
     * Removes child nodes from category nodes based on the filter type.
     * If objectFilter is null, clears all objects from all categories.
     *
     * @param objectFilter the type of objects to clear, or null to clear all
     */
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

    /**
     * Adds a SQL object as a child node to the specified parent category node.
     * Creates a new tree node containing the SQL object and inserts it at the end
     * of the parent's child list, maintaining alphabetical or creation order.
     *
     * @param parent the parent category node
     * @param obj the SQL object to add as a child
     */
    private void addSQLObject(DefaultMutableTreeTableNode parent, SQLObject obj) {
        DefaultMutableTreeTableNode objNode = new DefaultMutableTreeTableNode(obj);
        this.insertNodeInto(objNode, parent, parent.getChildCount());
    }

    /**
     * Sets SQL objects in the tree table model from a list of object information arrays.
     * Each array contains [schema, name, type] information that gets converted
     * to SQLObject instances and organized under appropriate category nodes.
     *
     * @param objects list of string arrays containing SQL object information
     */
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

    /**
     * Determines the appropriate parent category node for a SQL object.
     * Based on the object type and the showSystemObjects flag, decides which
     * root category node should contain this SQL object. System objects
     * are only shown if showSystemObjects is true.
     *
     * @param myObj the SQL object to categorize
     * @return the appropriate parent node, or null if the object should not be displayed
     */
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

    /**
     * Creates a SQLObject instance from an information array.
     * Parses the [schema, name, type] information and converts the type string
     * to the appropriate SQLObjectTypeEnum value.
     *
     * @param infos array containing [schema, name, type] information
     * @return the created SQLObject, or null if the type is not supported
     */
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
