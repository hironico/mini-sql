package net.hironico.minisql.ui;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ctrl.QueryResultCallable;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.history.QueryHistory;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Utility class for generating result component for displaying query results.
 * This can be used when executing a query from the QueryPanel or from the DB explorer when generating
 * an object structure query or whatever SQL call that needs to display something.
 * We use the SQLResultSetTableModel to store data to display and the way to display it on screen.
 * As a consequence, a list of SQLResultSetTableModel can display each result differently.
 */
public abstract class AbstractQueryAction extends AbstractRibbonAction {
    private static final Logger LOGGER = Logger.getLogger(AbstractQueryAction.class.getName());

    public AbstractQueryAction(String name, String icon) {
        super(name, icon);
    }

    public static void executeQueryAsync(QueryPanel queryPanel) {

        String sql = queryPanel.getQueryText();
        int resultDisplayType = queryPanel.getResultDisplayType();

        LOGGER.fine("Executing '" + sql + "'");

        queryPanel.setResultsComponent(new JLabel("Executing query, please wait."));

        QueryResultCallable queryCall = new QueryResultCallable(sql, queryPanel.getConfig(), queryPanel.isBatchMode());
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