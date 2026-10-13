package net.hironico.minisql.ui;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ctrl.MongoQueryResultCallable;
import net.hironico.minisql.ctrl.QueryResultCallable;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.history.QueryHistory;

import java.util.List;
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

    public static void executeQueryAsync(QueryPanel query29_queryPanel) {

        String sql = query29_queryPanel.getQueryText();
        int resultDisplayType = query29_queryPanel.getResultDisplayType();

        LOGGER.fine("Executing '" + sql + "'");

        query29_queryPanel.setResultsComponent(new JLabel("Executing query, please wait."));

        MainWindow.executorService.submit(() -> {
            try {
                List<SQLResultSetTableModel> results;
                if (query29_queryPanel.getConfig() != null && query29_queryPanel.getConfig().isMongoDB()) {
                    MongoQueryResultCallable mongoCall = new MongoQueryResultCallable(sql, query29_queryPanel.getConfig());
                    results = mongoCall.call();
                } else {
                    QueryResultCallable queryCall = new QueryResultCallable(sql, query29_queryPanel.getConfig(), query29_queryPanel.isBatchMode());
                    queryCall.addQueryHistoryListener(QueryHistory.getInstance());
                    results = queryCall.call();
                }

                results.forEach(result -> result.setDisplayType(resultDisplayType));
                JComponent compResults = QueryPanel.getResultComponentTab(results);
                SwingUtilities.invokeLater(() -> query29_queryPanel.setResultsComponent(compResults));

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JTextPane txtErr = new JTextPane();
                    txtErr.setText(ex.getMessage());
                    txtErr.setBorder(BorderFactory.createEmptyBorder());
                    txtErr.setEditable(false);
                    txtErr.setEnabled(false);
                    JScrollPane scrollErr = new JScrollPane(txtErr);
                    scrollErr.setBorder(BorderFactory.createEmptyBorder());
                    query29_queryPanel.setResultsComponent(scrollErr);
                });
            }
        });
    }

}