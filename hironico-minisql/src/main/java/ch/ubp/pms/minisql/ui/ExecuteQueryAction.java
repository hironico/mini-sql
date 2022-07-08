package ch.ubp.pms.minisql.ui;

import ch.ubp.pms.minisql.ctrl.QueryResultCallable;
import ch.ubp.pms.minisql.model.SQLResultSetTableModel;
import ch.ubp.pms.minisql.ui.history.QueryHistory;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.*;

public class ExecuteQueryAction extends AbstractQueryAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ExecuteQueryAction.class.getName());

    public ExecuteQueryAction() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        QueryPanel queryPanel = null;
        if ((comp == null) || !(comp instanceof QueryPanel)) {
            return;
        } else {
            queryPanel = (QueryPanel)comp;
        }

        ExecuteQueryAction.executeQueryAsync(queryPanel);
    }

    public static void executeQueryAsync(QueryPanel queryPanel) {
        
        String sql = queryPanel.getQueryText();
        int resultDisplayType = queryPanel.getResultDisplayType();

        LOGGER.fine("Executing '" + sql + "'");

        queryPanel.setResultsComponent(new JLabel("Executing query, please wait."));

        QueryResultCallable queryCall = new QueryResultCallable(sql, queryPanel.getConfig());
        queryCall.addQueryHistoryListener(QueryHistory.getInstance());
        final Future<List<SQLResultSetTableModel>> futureResults = MainWindow.executorService.submit(queryCall);

        Runnable waitQueryRun = () -> {
            try {
                List<SQLResultSetTableModel> results = futureResults.get();
                results.forEach(result -> result.setDisplayType(resultDisplayType));
                JComponent compResults = QueryPanel.getResultComponentTab(results);
                queryPanel.setResultsComponent(compResults);
            } catch (Exception ex) {
                JTextPane txtErr = new JTextPane();
                txtErr.setText(ex.getMessage());
                txtErr.setBorder(BorderFactory.createEmptyBorder());
                txtErr.setEditable(false);
                txtErr.setEnabled(false);
                JScrollPane scrollErr = new JScrollPane(txtErr);
                scrollErr.setBorder(BorderFactory.createEmptyBorder());
                queryPanel.setResultsComponent(scrollErr);
            }
        };

        Thread waitQueryThread = new Thread(waitQueryRun);
        waitQueryThread.start();
    }
}