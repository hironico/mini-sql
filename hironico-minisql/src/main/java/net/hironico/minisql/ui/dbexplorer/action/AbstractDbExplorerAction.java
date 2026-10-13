package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.utils.MongoUtils;

import javax.swing.JOptionPane;

/**
 * Base class for ribbon actions of the Db Explorer Panel
 */
public abstract class AbstractDbExplorerAction extends AbstractRibbonAction {

    /**
     * Creates a new action with name and icon
     * @param name is the name of the action acting as a label for the ui
     * @param iconName name of the icon file to use for ribbon button. can be null
     */
    public AbstractDbExplorerAction(String name, String iconName) {
        super(name, iconName);
    }

    /**
     *
     * @return the Schema explorer panel from the main window
     */
    protected SchemaExplorerPanel getExplorerPanel() {
        return MainWindow.getInstance().getSchemaExplorerPanel();
    }

    /**
     * Display a new query panel tab in the editors tab
     * @return the newly created QueryPanel
     */
    protected QueryPanel showNewQueryPanel() {
        String title = this.getExplorerPanel().getSelectedConnectionName();

        QueryPanel pnl = new QueryPanel();
        DbConfig cfg = DbConfigFile.getConfig(title);
        pnl.setConfig(cfg);

        MainWindow.getInstance().addNewEditorTab(pnl, title);

        return pnl;
    }

    /**
     * Shows a message and returns true when the given config targets MongoDB, for actions
     * that only support relational databases.
     * @param cfg the config of the selected connection
     * @return true if the action must be aborted because the connection is a MongoDB one
     */
    protected boolean rejectIfMongoDB(DbConfig cfg) {
        if (cfg == null || !cfg.isMongoDB()) {
            return false;
        }
        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                "This action is not supported for MongoDB connections.",
                "Not supported", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * @return true if the object is a MongoDB collection or view that can be queried
     */
    protected static boolean isQueryableMongoObject(SQLObject obj) {
        return SQLObjectTypeEnum.isCollection(obj.type) || obj.type == SQLObjectTypeEnum.VIEW;
    }

    /**
     * Opens a new query panel, sets the given query text and executes it.
     * @param query the query to execute
     */
    protected void showAndExecuteQuery(String query) {
        QueryPanel pnl = showNewQueryPanel();
        pnl.setQueryText(query);
        pnl.setDividerLocation(0.25d);
        ExecuteQueryAction.executeQueryAsync(pnl);
    }

    /**
     * @return the mongosh query selecting the first 1000 documents of the given collection or view
     */
    public static String getMongoSelect1kQuery(String collectionName) {
        return MongoUtils.getCollectionExpression(collectionName) + ".aggregate([{ \"$limit\": 1000 }])";
    }

    /**
     * @return the mongosh query counting the documents of the given collection or view
     */
    public static String getMongoCountQuery(String collectionName) {
        return MongoUtils.getCollectionExpression(collectionName) + ".countDocuments({})";
    }

}