package net.hironico.minisql.ui.history;

public interface QueryHistoryListener {
    public void queryAdded(QueryHistoryEntry query);

    public void queryRemoved(QueryHistoryEntry query);
}