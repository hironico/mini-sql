package net.hironico.minisql.ui.history;

/**
 * Listener interface for receiving notifications when SQL queries are executed.
 * Implement this interface to receive callbacks when SQL queries are run through
 * the MiniSQL application, enabling components to track query execution history.
 */
public interface QueryExecutionListener {

    /**
     * Called when a SQL query has been executed.
     *
     * @param query the SQL query that was executed
     */
    public void queryExecuted(String query);
}
