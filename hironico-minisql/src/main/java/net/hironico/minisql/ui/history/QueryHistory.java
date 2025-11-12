package net.hironico.minisql.ui.history;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Central manager for SQL query execution history.
 * This singleton class handles the storage and retrieval of executed SQL queries,
 * manages listeners for history changes, and coordinates persistence operations.
 * Acts as the main facade for accessing and managing the application's query history.
 */
@JacksonXmlRootElement(localName = "common-history")
public class QueryHistory implements QueryExecutionListener {

    /** Singleton instance of the query history manager */
    private static final QueryHistory instance = new QueryHistory();

    /** Persistent storage handler for query history data */
    private static final QueryHistoryFile historyFile = QueryHistoryFile.load();

    /** Set of registered listeners for history change notifications */
    private final Set<QueryHistoryListener> listeners = new HashSet<>();

    /** Private constructor to enforce singleton pattern */
    private QueryHistory() {
    }

    /**
     * Gets the singleton instance of the QueryHistory manager.
     * Provides access to the single shared instance of the query history manager.
     *
     * @return the singleton QueryHistory instance
     */
    public static QueryHistory getInstance() {
        return instance;
    }

    /**
     * Called when a SQL query has been executed.
     * Creates a new history entry for the query and notifies all registered listeners.
     *
     * @param query the SQL query that was executed
     */
    @Override
    public void queryExecuted(String query) {
        QueryHistoryEntry entry = historyFile.add(query);
        this.fireQueryAdded(entry);
    }

    /**
     * Gets the complete history of executed queries.
     * Returns a chronologically sorted set of all query history entries,
     * with the most recent entries first.
     *
     * @return TreeSet containing all QueryHistoryEntry objects in reverse chronological order
     */
    public TreeSet<QueryHistoryEntry> getSQLHistory() {
        return QueryHistory.historyFile.getSqlHistory();
    }

    /**
     * Notifies all registered listeners that a new query entry was added.
     *
     * @param entry the QueryHistoryEntry that was added
     */
    private void fireQueryAdded(QueryHistoryEntry entry) {
        this.listeners.forEach(listener -> listener.queryAdded(entry));
    }

    /**
     * Registers a listener to receive notifications when query history changes.
     *
     * @param listener the QueryHistoryListener to register
     */
    public void addQueryHistoryListener(QueryHistoryListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Unregisters a listener from receiving query history change notifications.
     *
     * @param listener the QueryHistoryListener to remove
     */
    public void removeQueryHistoryListener(QueryHistoryListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Retrieves a specific query history entry by its index.
     * The entries are ordered chronologically (most recent first), so index 0
     * returns the most recently executed query.
     *
     * @param num the index of the history entry to retrieve (0-based)
     * @return the QueryHistoryEntry at the specified index, or null if index is invalid
     */
    public QueryHistoryEntry getQueryHistoryAt(int num) {
        if (num < 0 || num >= this.getSQLHistory().size()) {
            return null;
        }

        Iterator<QueryHistoryEntry> iterator = this.getSQLHistory().iterator();
        int index = 0;
        while (index < num) {
            iterator.next();
            index++;
        }
        return iterator.next();
    }
}
