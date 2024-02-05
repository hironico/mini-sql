package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class DbObjectSelect1kAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DbObjectSelect1kAction.class.getName());

    private enum DB_VENDOR {
        ORACLE,
        SYBASE,
        POSTGRES
    }
    public static final String NAME = "1k rows";

    private static final String oraSelect = "SELECT * FROM %s.%s WHERE rownum <= 1000";
    private static final String sybSelect = "SELECT top 1000 * FROM %s.%s";
    private static final String pgsqlSelect = "SELECT * FROM %s.%s LIMIT 1000";

    public DbObjectSelect1kAction() {
        super(NAME, "icons8_k_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SQLObject obj = this.getExplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }

        DbConfig cfg = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (cfg == null) {
            return;
        }

        DB_VENDOR dbVendor = this.getDbVendor(cfg);
        if (dbVendor == null) {
            LOGGER.severe("Driver type is not supported. Only Oracle or Sybase/MSSQL (TDS) or Postgresql are supported.");
            JOptionPane.showMessageDialog(MainWindow.getInstance(), "Driver type is not supported. Only Oracle or Sybase/MSSQL (TDS) or Postgresql are supported.");
            return;
        }

        QueryPanel pnl = showNewQueryPanel();

        if (SQLObjectTypeEnum.isTableOrView(obj.type)) {
            String query = this.getSelectQuery(dbVendor);
            query = String.format(query, obj.schemaName, obj.name);
            pnl.setQueryText(query);
            pnl.setDividerLocation(0.25d);
            ExecuteQueryAction.executeQueryAsync(pnl);
        }
    }
    private DB_VENDOR getDbVendor(DbConfig cfg) {
        if (cfg.jdbcUrl.toLowerCase().contains("oracle")) {
            return DB_VENDOR.ORACLE;
        }

        if (cfg.jdbcUrl.toLowerCase().contains("tds")) {
            return DB_VENDOR.SYBASE;
        }

        if (cfg.jdbcUrl.toLowerCase().contains("postgres")) {
            return DB_VENDOR.POSTGRES;
        }

        return null;
    }
    private String getSelectQuery(DB_VENDOR dbVendor) {
        if (dbVendor == null) {
            return null;
        }

        switch (dbVendor) {
            case ORACLE:
                return oraSelect;

            case SYBASE:
                return sybSelect;

            case POSTGRES:
                return pgsqlSelect;

            default:
                return null;
        }
    }
}