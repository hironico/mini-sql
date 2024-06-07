package net.hironico.minisql.ui.visualdb;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLTable;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Permet de faire la récupération des méta données des tables
 * passées en paramètre depuis la base de données.
 * @author hironico
 * @since 2.1.0
 */
public class SQLTableLoaderThread implements Callable<List<SQLTable>> {
    private static final Logger LOGGER = Logger.getLogger(SQLTableLoaderThread.class.getName());
    private List<SQLTable> tablesListToDisplay = null;
    private final DbConfig dbConfig;

    public SQLTableLoaderThread(List<SQLTable> tableList, DbConfig dbConfig) {
        super();
        this.tablesListToDisplay = tableList;
        this.dbConfig = dbConfig;
    }

    @Override
    public List<SQLTable> call() {
        for (SQLTable table : tablesListToDisplay) {
            try {
                LOGGER.info("Loading table columns for: " + table.name);
                table.loadMetaData(dbConfig);
                table.color = dbConfig.color;
            } catch (SQLException sqle) {
                LOGGER.log(Level.SEVERE, sqle.getMessage(), sqle);
            }
        }

        return tablesListToDisplay;
    }

}
