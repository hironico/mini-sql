package ch.ubp.pms.minisql.ui.history;

public interface QueryHistoryListener {
    public void queryAdded(QueryHistoryEntry query);

    public void queryRemoved(QueryHistoryEntry query);
}