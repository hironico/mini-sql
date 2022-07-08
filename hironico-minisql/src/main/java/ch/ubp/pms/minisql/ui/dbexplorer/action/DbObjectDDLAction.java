package ch.ubp.pms.minisql.ui.dbexplorer.action;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.minisql.DbConfigFile;
import ch.ubp.pms.minisql.model.SQLObject;
import ch.ubp.pms.minisql.model.SQLObject.DDLActionEnum;
import ch.ubp.pms.minisql.model.SQLObjectTypeEnum;
import ch.ubp.pms.minisql.model.SQLTable;
import ch.ubp.pms.minisql.model.SQLView;
import ch.ubp.pms.minisql.ui.MainWindow;
import ch.ubp.pms.minisql.ui.QueryPanel;
import ch.ubp.pms.minisql.ui.dbexplorer.SchemaExplorerPanel;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbObjectDDLAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DbObjectDDLAction.class.getName());

    private DDLActionEnum ddlAction;

    public DbObjectDDLAction(DDLActionEnum ddlAction) {
        super(ddlAction.toString(), null);
        this.ddlAction = ddlAction;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        SchemaExplorerPanel explorerPanel = MainWindow.getInstance().getSchemaExcplorerPanel();
        SQLObject obj = explorerPanel.getSelectedSQLObject();
        DbConfig cfg = DbConfigFile.getConfig(explorerPanel.getSelectedConnectionName());
        try {
            String ddl = null;
            switch (SQLObjectTypeEnum.valueOf(obj.type)) {
            case TABLE:
                SQLTable table = new SQLTable(obj.schemaName, obj.name);
                table.loadMetaData(cfg);
                ddl = table.getDDL(ddlAction);
                break;

            case VIEW:
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