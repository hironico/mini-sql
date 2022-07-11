package net.hironico.minisql.ui.history;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

@JacksonXmlRootElement(localName = "common-history")
public class QueryHistory implements QueryExecutionListener {

    private static final QueryHistory instance = new QueryHistory();

    private static final QueryHistoryFile historyFile = QueryHistoryFile.load();
    
    private final Set<QueryHistoryListener> listeners = new HashSet<>();

    private QueryHistory() {
    }

    public static QueryHistory getInstance() {
        return instance; 
    }

    @Override
    public void queryExecuted(String query) {
        QueryHistoryEntry entry = historyFile.add(query);
        this.fireQueryAdded(entry);
    }

    public TreeSet<QueryHistoryEntry> getSQLHistory() {
        return QueryHistory.historyFile.getSqlHistory();
    }
    
    private void fireQueryAdded(QueryHistoryEntry entry) {
        this.listeners.forEach(listener -> listener.queryAdded(entry));
    }

    public void addQueryHistoryListener(QueryHistoryListener listener) {
        this.listeners.add(listener);
    }

    public void removeQueryHistoryListener(QueryHistoryListener listener) {
        this.listeners.remove(listener);
    }

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