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

/**
 * Callable implementation that executes SQL queries and returns result sets.
 * Handles both single and batch query execution, processing ResultSets and update counts.
 * Supports query history tracking through listener notifications.
 */
public class QueryResultCallable implements Callable<List<SQLResultSetTableModel>> {

    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(QueryResultCallable.class.getName());

    /** The SQL query to execute */
    private final String sqlQuery;

    /** Database configuration containing connection details */
    private final DbConfig config;

    /** Flag indicating if query should be executed in batch mode */
    private final boolean batchMode;

    /** Set of listeners to notify when queries are executed */
    private final Set<QueryExecutionListener> historyListeners = new HashSet<>();

    /**
     * Constructs a QueryResultCallable for single query execution.
     *
     * @param query the SQL query to execute
     * @param config the database configuration
     */
    public QueryResultCallable(String query, DbConfig config) {
        this(query, config, false);
    }

    /**
     * Constructs a QueryResultCallable with batch mode specification.
     *
     * @param query the SQL query to execute (or batch of queries if batchMode is true)
     * @param config the database configuration
     * @param batchMode true to enable batch processing of multiple statements
     */
    public QueryResultCallable(String query, DbConfig config, boolean batchMode) {
        super();
        this.sqlQuery = query;
        this.config = config;
        this.batchMode = batchMode;
    }

    /**
     * Creates a SQLResultSetTableModel from a ResultSet.
     *
     * @param resultSet the ResultSet containing query results
     * @return a new SQLResultSetTableModel wrapping the ResultSet
     * @throws SQLException if accessing the ResultSet fails
     */
    private SQLResultSetTableModel getResults(ResultSet resultSet) throws SQLException {
        return new SQLResultSetTableModel(resultSet, "Query", sqlQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
    }

    /**
     * Creates a SQLResultSetTableModel for an update count (DDL/DML operations).
     *
     * @param updateCount the number of affected rows from an update operation
     * @return a SQLResultSetTableModel containing the update count information
     * @throws SQLException if creating the model fails
     */
    private SQLResultSetTableModel getResults(int updateCount) throws SQLException {
        SQLResultSetTableModel result = new SQLResultSetTableModel(null, "Update query", sqlQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        String[] columns = { "Updated" };
        result.setColumnIdentifiers(columns);

        String[] row = { "Update count is " + updateCount };

        result.addRow(row);

        return result;
    }

    /**
     * Executes the SQL query asynchronously and returns result models.
     * Supports both single and batch query execution. For batch mode,
     * splits the query on the batch statement separator and executes each
     * statement individually. Handles both SELECT queries (returning ResultSets)
     * and DDL/DML operations (returning update counts).
     *
     * @return list of SQLResultSetTableModel objects containing query results
     * @throws Exception if query execution fails or connection cannot be established
     */
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

    /**
     * Registers a listener to receive notifications when SQL queries are executed.
     *
     * @param listener the QueryExecutionListener to add
     */
    public void addQueryHistoryListener(QueryExecutionListener listener) {
        this.historyListeners.add(listener);
    }

    /**
     * Removes a previously registered query execution listener.
     *
     * @param listener the QueryExecutionListener to remove
     */
    public void removeQueryHistoryListener(QueryExecutionListener listener) {
        this.historyListeners.remove(listener);
    }

    /**
     * Asynchronously notifies all registered listeners that a query was executed.
     * Runs in a separate thread to avoid blocking the main query execution.
     */
    private void fireQueryExecuted() {
        Runnable runNotif = () -> QueryResultCallable.this.historyListeners.forEach(listener -> listener.queryExecuted(QueryResultCallable.this.sqlQuery));

        Thread thread = new Thread(runNotif);
        thread.start();        
    }
}
