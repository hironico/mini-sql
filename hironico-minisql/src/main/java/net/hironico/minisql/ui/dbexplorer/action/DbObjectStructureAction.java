package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.minisql.App;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.MetadataResultCallable;
import net.hironico.minisql.ctrl.QueryResultCallable;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.*;

public class DbObjectStructureAction extends AbstractDbExplorerAction {

    private static final Logger LOGGER = Logger.getLogger(DbObjectRefreshAction.class.getName());

    public static final String NAME = "Structure";

    public DbObjectStructureAction() {
        super(NAME, "icons8_work_64px.png");
    }

    public void actionPerformed(ActionEvent evt) {
        SQLObject obj = this.getExplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }

        final DbConfig configToUse = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (configToUse == null) {
            return;
        }

        switch (obj.type) {
        case TABLE:
        case SYSTEM_TABLE:
            showTableStructure(obj, configToUse);
            break;

        case VIEW:
        case MATERIALIZED_VIEW:
        case SYSTEM_VIEW:
            showViewStructure(obj, configToUse);
            break;

        case PROCEDURE:
        case SYSTEM_PROCEDURE:
            showProcedureStructure(obj, configToUse);
            break;

        case FUNCTION:
        case SYSTEM_FUNCTION:
            showFunctionStructure(obj, configToUse);
            break;

        case SEQUENCE:
        case SYSTEM_SEQUENCE:
            showSequenceStructure(obj, configToUse);
            break;

        case ENUM:
            showEnumStructure(obj, configToUse);
            break;

        default:
            LOGGER.warning("Object structure required for non supported SQL object type: " + obj.type);
            break;
        }
    }

    private Future<List<SQLResultSetTableModel>> executeDialectSpecificQuery(String filename, final SQLObject obj, DbConfig config)
    throws Exception {
        String sql = null;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new Exception("Cannot open dialect specific query file: " + filename);
            }
            sql = new String(is.readAllBytes());
        }

        sql = sql.replace("?SCHEMA?", String.format("'%s'", obj.schemaName));
        sql = sql.replace("?NAME?", String.format("'%s'", obj.name));
        sql = sql.replace("?USER?", String.format("'%s'", config.user));

        QueryResultCallable queryCall = new QueryResultCallable(sql, config);
        return MainWindow.executorService.submit(queryCall);
    }

    private void showTableStructure(final SQLObject obj, DbConfig config) {
        Runnable runDisplayResult = () -> {
            MetadataResultCallable call = new MetadataResultCallable(obj.schemaName, obj.name, obj.type, config);
            Future<List<SQLResultSetTableModel>> futureResult = MainWindow.executorService.submit(call);

            List<SQLResultSetTableModel> modelToDisplay;
            try {
                modelToDisplay = futureResult.get();
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting table structure.", ie);
                modelToDisplay = new ArrayList<>();
            }

            displayResults(modelToDisplay, obj);
        };

        Thread threadQuery = new Thread(runDisplayResult);
        threadQuery.start();
    }

    private Future<List<SQLResultSetTableModel>> getFunctionTextPostgresql(final SQLObject obj, DbConfig config) {
        String query = String.format("SELECT pg_get_functiondef('%s.%s'::regproc::oid);", obj.schemaName, obj.name);
        QueryResultCallable queryCall = new QueryResultCallable(query, config);
        return MainWindow.executorService.submit(queryCall);
    }

    private void showFunctionStructure(final SQLObject obj, DbConfig config) {
        Thread threadQuery = new Thread( () -> {
            if (config.jdbcUrl.startsWith("jdbc:postgresql")) {
                List<SQLResultSetTableModel> modelListToDisplay = new ArrayList<>();

                try {
                    Future<List<SQLResultSetTableModel>> futText = this.getFunctionTextPostgresql(obj, config);
                    Future<List<SQLResultSetTableModel>> futStructure = executeDialectSpecificQuery("net/hironico/minisql/metadata/postgresql/pg_get_functiondef.sql", obj, config);

                    List<SQLResultSetTableModel> resultStructure = futStructure.get();
                    resultStructure = resultStructure.stream().map(r -> {
                        try {
                            r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
                            r.setTitle("Structure");
                            return r.transpose();
                        } catch (Exception ex) {
                            return r;
                        }
                    }).toList();
                    modelListToDisplay.addAll(resultStructure);

                    List<SQLResultSetTableModel> textList = futText.get();
                    textList.forEach(m -> {
                        m.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_SQL);
                        m.setTitle("SQL text");
                    });
                    modelListToDisplay.addAll(textList);
                } catch (Exception ie) {
                    LOGGER.log(Level.SEVERE, "Error while getting the procedure text.", ie);
                    modelListToDisplay = Collections.emptyList();
                }

                displayResults(modelListToDisplay, obj);
            } else {
                JOptionPane.showMessageDialog(App.mainWindow,
                        "Show function structure is not supported at this time for this kind of database.\n" + config.jdbcUrl);
            }
        });

        threadQuery.start();
    }

    private void showProcedureStructure(final SQLObject obj, DbConfig config) {
        if (config.jdbcUrl.startsWith("jdbc:postgresql")) {
            this.showFunctionStructure(obj, config);
            return;
        }

        Thread threadQuery = new Thread( () -> {
            String query = null;
            if (config.jdbcUrl.startsWith("jdbc:oracle")) {
                query = String.format("SELECT text FROM all_source WHERE name = '%s' and owner = '%s' ORDER BY line", obj.name, obj.schemaName);
            }

            if (query == null) {
                JOptionPane.showMessageDialog(App.mainWindow,
                        "Show procedure structure is not supported at this time for this kind of database.\n" + config.jdbcUrl);
                return;
            }

            QueryResultCallable queryCall = new QueryResultCallable(query, config);
            Future<List<SQLResultSetTableModel>> futureResult = MainWindow.executorService.submit(queryCall);

            List<SQLResultSetTableModel> modelListToDisplay;
            try {
                modelListToDisplay = futureResult.get();
                modelListToDisplay.forEach(r -> r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_SQL));
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting the procedure text.", ie);
                modelListToDisplay = Collections.emptyList();
            }

            displayResults(modelListToDisplay, obj);
        });

        threadQuery.start();
    }

    private void showViewStructure(final SQLObject obj, DbConfig config) {
        Runnable runDisplayResult = () -> {
            MetadataResultCallable call = new MetadataResultCallable(obj.schemaName, obj.name, obj.type, config);
            Future<List<SQLResultSetTableModel>> futureResult = MainWindow.executorService.submit(call);

            // oracle query as default
            String query = String.format("SELECT text FROM all_views WHERE view_name = '%s'", obj.name);

            if (config.jdbcUrl.startsWith("jdbc:postgresql")) {
                query = String.format("select pg_get_viewdef('%s.%s'::regclass, true)", obj.schemaName, obj.name);
            }

            QueryResultCallable queryCall = new QueryResultCallable(query, config);
            Future<List<SQLResultSetTableModel>> viewTextFuture = MainWindow.executorService.submit(queryCall);

            List<SQLResultSetTableModel> modelToDisplay = new ArrayList<>();
            try {
                modelToDisplay.addAll(futureResult.get());
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting view structure.", ie);
            }

            try {
                List<SQLResultSetTableModel> textResult = viewTextFuture.get();
                textResult.forEach(r -> {
                    r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_SQL);
                    r.setTitle("View text");
                });
                modelToDisplay.addAll(textResult);
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting view SQL text.", ie);
            }

            displayResults(modelToDisplay, obj);
        };

        Thread threadQuery = new Thread(runDisplayResult);
        threadQuery.start();
    }

    private void showSequenceStructure(final SQLObject obj, DbConfig config) {
        Runnable runDisplayResult = () -> {
            // default use of oracle query
            String sql = String.format("SELECT * FROM all_sequences s WHERE s.sequence_owner = '%s' AND s.sequence_name = '%s' ORDER BY s.sequence_name", obj.schemaName, obj.name);

            if (config.jdbcUrl.startsWith("jdbc:postgresql")) {
                sql = String.format("SELECT * FROM information_schema.sequences s WHERE s.sequence_schema = '%s' AND s.sequence_name = '%s' ORDER BY s.sequence_name", obj.schemaName, obj.name);
            }

            QueryResultCallable queryCall = new QueryResultCallable(sql, config);
            Future<List<SQLResultSetTableModel>> sequenceStructureFuture = MainWindow.executorService.submit(queryCall);

            List<SQLResultSetTableModel> modelToDisplay = new ArrayList<>();
            try {
                modelToDisplay.addAll(sequenceStructureFuture.get());
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting view structure.", ie);
            }

            try {
                List<SQLResultSetTableModel> sequenceResults = sequenceStructureFuture.get();
                modelToDisplay = sequenceResults.stream().map(r -> {
                    try {
                        r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
                        r.setTitle("Structure");
                        return r.transpose();
                    } catch (Exception ex) {
                        return r;
                    }
                }).collect(Collectors.toList());
            } catch (InterruptedException | ExecutionException ie) {
                LOGGER.log(Level.SEVERE, "Error while getting view SQL text.", ie);
            }

            displayResults(modelToDisplay, obj);
        };

        Thread threadQuery = new Thread(runDisplayResult);
        threadQuery.start();
    }

    private void showEnumStructure(final SQLObject obj, final DbConfig config) {
        if (!config.jdbcUrl.startsWith("jdbc:postgresql")) {
            JOptionPane.showMessageDialog(MainWindow.getInstance(),
                    "Enum structure display is not supported for this dataserver.",
                    "Not supported...",
                    JOptionPane.ERROR_MESSAGE);
        }
        Runnable run = () -> {
            try {
                Future<List<SQLResultSetTableModel>> fut = executeDialectSpecificQuery("net/hironico/minisql/metadata/postgresql/pg_get_enumdef.sql", obj, config);
                List<SQLResultSetTableModel> modelList = fut.get();
                displayResults(modelList, obj);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(),
                        "Unable to get the Enum structure.\n" + ex.getMessage(),
                        "Ohoh...",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        Thread runThread = new Thread(run);
        runThread.start();
    }

    private void displayResults(List<SQLResultSetTableModel> modelListToDisplay, SQLObject objectToDisplay) {
        JComponent resultComp = QueryPanel.getResultComponentTab(modelListToDisplay);
        String tabTitle = objectToDisplay.schemaName + "." + objectToDisplay.name + " (" + objectToDisplay.type + ")";

        // resultComp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JRoundedPanel pnl = new JRoundedPanel();
        pnl.setGradientBackground(false);
        pnl.setLayout(new BorderLayout());
        pnl.add(resultComp, BorderLayout.CENTER);

        MainWindow.getInstance().addNewEditorTab(resultComp, tabTitle);
    }
}