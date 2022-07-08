package ch.ubp.pms.minisql.ui.history;

import java.util.Date;
import javax.swing.table.DefaultTableModel;

public class QueryHistoryTableModel extends DefaultTableModel implements QueryHistoryListener {

    public QueryHistoryTableModel() {
        super();
        String[] colNames = { "Timestamp", "SQL"};
        setColumnIdentifiers(colNames);

        QueryHistory history = QueryHistory.getInstance();
        history.addQueryHistoryListener(this);
        this.setQueryHistory(history);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch(col) {
            case 0:
                return Date.class;

            case 1:
                return String.class;

            default:
                return Object.class;
        }
    }

    public void setQueryHistory(QueryHistory history) {

        while(getRowCount() > 1) {
            removeRow(0);
        }

        history.getSQLHistory().stream()
                .map(h -> {
                    Object[] row = new Object[2];
                    row[0] = h.timestamp;
                    row[1] = h.query;
                    return row;
                })
                .forEach(this::addRow);
    }

    @Override
    public void queryAdded(QueryHistoryEntry query) {
        Object[] row = new Object[2];
        row[0] = query.timestamp;
        row[1] = query.query;
        this.addRow(row);
    }

    @Override
    public void queryRemoved(QueryHistoryEntry query) {
        // noop
    }
}
