package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.model.SQLView;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbObjectDDLAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DbObjectDDLAction.class.getName());

    private SQLObject.DDLActionEnum ddlAction;

    public DbObjectDDLAction(SQLObject.DDLActionEnum ddlAction) {
        super(ddlAction.toString(), null);
        this.ddlAction = ddlAction;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        SchemaExplorerPanel explorerPanel = MainWindow.getInstance().getSchemaExplorerPanel();
        SQLObject obj = explorerPanel.getSelectedSQLObject();
        DbConfig cfg = DbConfigFile.getConfig(explorerPanel.getSelectedConnectionName());
        try {
            String ddl = null;
            switch (obj.type) {
            case TABLE:
            case SYSTEM_TABLE:
                SQLTable table = new SQLTable(obj.schemaName, obj.name);
                table.loadMetaData(cfg);
                ddl = table.getDDL(ddlAction);
                break;

            case VIEW:
            case SYSTEM_VIEW:
                SQLView view = new SQLView(obj.schemaName, obj.name);
                view.loadMetaData(cfg);
                ddl = view.getDDL(ddlAction);
                break;

            default:
                LOGGER.info("DDL generation not supported for this kind of object: " + obj.type);
                break;
            }

            if (ddl != null) {
                String query = "--\n-- Please review the code below before executing the query.\n--\n" + ddl;
                QueryPanel pnl = showNewQueryPanel();
                pnl.setQueryText(query);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Problem while generating the DDL.", ex);
        }
    }
}