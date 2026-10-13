package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.MetadataResultCallable;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.MainWindow;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbObjectExportStructureAction extends AbstractDbExplorerAction{
    private static final Logger LOGGER = Logger.getLogger(DbObjectExportStructureAction.class.getName());

    public DbObjectExportStructureAction() {
        super("Export Model", null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        final DbConfig configToUse = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (configToUse == null || rejectIfMongoDB(configToUse)) {
            return;
        }

        List<SQLObject> objList = this.getExplorerPanel().getAllSelectedSQLObjects();
        if (objList == null || objList.isEmpty()) {
            return;
        }

        // display a tab showing the operation is in progress
        RSyntaxTextArea textArea = new RSyntaxTextArea(String.format("Now loading model for %d objects...", objList.size()));
        textArea.setEnabled(true);
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane scrollResults = new RTextScrollPane(textArea);
        scrollResults.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        scrollResults.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollResults.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // this call occurs in a different thread
        MainWindow.getInstance().addNewEditorTab(scrollResults, String.format("JSON Model: %s", configToUse.name));

        List<Callable<String>> tasks = new ArrayList<>();
        for (SQLObject obj : objList) {
            switch (obj.type) {
                case TABLE:
                case SYSTEM_TABLE:
                case VIEW:
                case MATERIALIZED_VIEW:
                case SYSTEM_VIEW:
                    tasks.add(exportTableModel(obj, configToUse));
                    break;

                default:
                    LOGGER.severe("Unsupported object type for export model: " + obj.type);
                    break;
            }
        }

        Thread thread = new Thread(() -> {
            StringBuilder resultBuilder = new StringBuilder();
            try {
                List<Future<String>> jsonParts = MainWindow.executorService.invokeAll(tasks);

                jsonParts.forEach(f -> {
                    try {
                        resultBuilder.append(f.get()).append("\n");
                    } catch (InterruptedException | ExecutionException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        resultBuilder.append(ex.getMessage()).append("\n");
                    }
                });
            } catch (InterruptedException ex) {
                resultBuilder.append(ex.getMessage()).append("\n");
                throw new RuntimeException(ex);
            }

            // update the results
            textArea.setText(resultBuilder.toString());
        });

        thread.start();
    }

    private Callable<String> exportTableModel(SQLObject obj, DbConfig config) {
        return () -> {
            MetadataResultCallable call = new MetadataResultCallable(obj.schemaName, obj.name, obj.type, config);
            List<SQLResultSetTableModel> modelToDisplay;
            try {
                modelToDisplay = call.call();
            } catch (Exception ie) {
                LOGGER.log(Level.SEVERE, "Error while getting table structure.", ie);
                modelToDisplay = new ArrayList<>();
            }

            StringBuilder resultBuilder = new StringBuilder();
            modelToDisplay.forEach(model -> {
                model.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_JSON);
                String json = model.toJSONString();
                resultBuilder.append(json).append("\n");
            });

            return resultBuilder.toString();
        };
    }

    @Override
    public boolean accept(Object sender) {
        return super.accept(sender);
    }
}
