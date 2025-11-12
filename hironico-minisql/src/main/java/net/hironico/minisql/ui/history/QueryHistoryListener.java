package net.hironico.minisql.ui.history;

/**
 * Listener interface for monitoring changes to the query history.
 * Implement this interface to receive notifications when queries are added to
 * or removed from the application's query history collection.
 */
public interface QueryHistoryListener {

    /**
     * Called when a new query entry is added to the history.
     *
     * @param query the QueryHistoryEntry that was added to the history
     */
    public void queryAdded(QueryHistoryEntry query);

    /**
     * Called when a query entry is removed from the history.
     *
     * @param query the QueryHistoryEntry that was removed from the history
     */
    public void queryRemoved(QueryHistoryEntry query);
}
