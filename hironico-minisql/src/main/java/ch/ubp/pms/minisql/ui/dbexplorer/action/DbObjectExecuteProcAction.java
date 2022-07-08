package ch.ubp.pms.minisql.ui.dbexplorer.action;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.minisql.DbConfigFile;
import ch.ubp.pms.minisql.ctrl.MetadataResultCallable;
import ch.ubp.pms.minisql.model.SQLObject;
import ch.ubp.pms.minisql.model.SQLObjectTypeEnum;
import ch.ubp.pms.minisql.model.SQLResultSetTableModel;
import ch.ubp.pms.minisql.ui.MainWindow;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbObjectExecuteProcAction extends AbstractDbExplorerAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DbObjectRefreshAction.class.getName());

    private static final String NAME = "Execute proc";

    public DbObjectExecuteProcAction() {
        super(NAME, "icons8_start_64px");
    }

    public void actionPerformed(ActionEvent evt) {
        DbConfig configToUse = DbConfigFile.getConfig(MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedConnectionName());
        if (configToUse == null) {
            return;
        }

        SQLObject obj = MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }

        if (obj.type.equalsIgnoreCase(SQLObjectTypeEnum.PROCEDURE.toString())) {
            generateExecuteSQL(obj, configToUse);
        }
    }

    private void generateExecuteSQL(SQLObject obj, DbConfig configToUse) {
        MetadataResultCallable call = new MetadataResultCallable(obj, configToUse);
        Future<List<SQLResultSetTableModel>> result = MainWindow.executorService.submit(call);

        try {
            result.get();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot get procedure meta data.", ex);
        }

    }
}