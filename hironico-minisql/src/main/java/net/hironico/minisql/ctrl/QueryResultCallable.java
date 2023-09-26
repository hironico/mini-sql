package net.hironico.minisql.ctrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.history.QueryExecutionListener;

public class QueryResultCallable implements Callable<List<SQLResultSetTableModel>> {

    private static final Logger LOGGER = Logger.getLogger(QueryResultCallable.class.getName());

    private final String sqlQuery;
    private final DbConfig config;
    private final boolean batchMode;

    private final Set<QueryExecutionListener> historyListeners = new HashSet<>();

    public QueryResultCallable(String query, DbConfig config) {
        this(query, config, false);
    }

    public QueryResultCallable(String query, DbConfig config, boolean batchMode) {
        super();
        this.sqlQuery = query;
        this.config = config;
        this.batchMode = batchMode;
    }

    private SQLResultSetTableModel getResults(ResultSet resultSet) throws SQLException {
        return new SQLResultSetTableModel(resultSet, "Query", sqlQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
    }

    private SQLResultSetTableModel getResults(int updateCount) throws SQLException {
        SQLResultSetTableModel result = new SQLResultSetTableModel(null, "Update query", sqlQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        String[] columns = { "Updated" };
        result.setColumnIdentifiers(columns);

        String[] row = { "Update count is " + updateCount };

        result.addRow(row);

        return result;
    }

    @Override
    public List<SQLResultSetTableModel> call() throws Exception {

        List<SQLResultSetTableModel> modelResults = new ArrayList<>();

        try (Connection con = config.getConnection()) {

            String[] sqlCommands = { sqlQuery };

            if (this.batchMode) {
                sqlCommands = this.sqlQuery.split(config.batchStatementSeparator);
            }

            for (String oneSqlCommand : sqlCommands) {

                Statement stmt = con.createStatement();
                boolean hasResultSet = stmt.execute(oneSqlCommand);
                int updateCount = stmt.getUpdateCount();

                do {
                    SQLResultSetTableModel oneResult = null;
                    if (hasResultSet) {
                        oneResult = getResults(stmt.getResultSet());
                    } else if (updateCount >= 0) {
                        oneResult = getResults(updateCount);
                    }

                    if (oneResult != null) {
                        modelResults.add(oneResult);
                    }

                    SQLWarning warn = stmt.getWarnings();
                    while (warn != null) {
                        LOGGER.warning(warn.getLocalizedMessage());
                        warn = warn.getNextWarning();
                    }

                    hasResultSet = stmt.getMoreResults();
                    updateCount = stmt.getUpdateCount();
                } while (hasResultSet || updateCount >= 0);

                stmt.close();
            }

        } catch (SQLException | ClassNotFoundException sqle) {
            throw new Exception(sqle);
        } finally {
            this.fireQueryExecuted();
        }

        LOGGER.finer("Found " + modelResults.size() + " resultsets.");
        return modelResults;
    }

    public void addQueryHistoryListener(QueryExecutionListener listener) {
        this.historyListeners.add(listener);
    }

    public void removeQueryHistoryListener(QueryExecutionListener listener) {
        this.historyListeners.remove(listener);
    }

    /**
     * Asynch notification of the query being executed.
     */
    private void fireQueryExecuted() {
        Runnable runNotif = () -> QueryResultCallable.this.historyListeners.forEach(listener -> listener.queryExecuted(QueryResultCallable.this.sqlQuery));

        Thread thread = new Thread(runNotif);
        thread.start();        
    }
}