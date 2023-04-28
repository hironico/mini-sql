package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.App;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.MetadataResultCallable;
import net.hironico.minisql.ctrl.QueryResultCallable;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.QueryPanel;

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
import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class DbObjectStructureAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

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
        case "TABLE":
            showTableStructure(obj, configToUse);
            break;

        case "VIEW":
        case "MATERIALIZED VIEW":
            showViewStructure(obj, configToUse);
            break;

        case "PROCEDURE":
            showProcedureStructure(obj, configToUse);
            break;

        case "FUNCTION":
            showFunctionStructure(obj, configToUse);
            break;

        case "SEQUENCE":
            showSequenceStructure(obj, configToUse);
            break;

        default:
            LOGGER.warning("Object structure required for non supported SQL object type: " + obj.type);
            break;
        }
    }

    private void showTableStructure(final SQLObject obj, DbConfig config) {
        Runnable runDisplayResult = () -> {
            MetadataResultCallable call = new MetadataResultCallable(obj.schemaName, obj.name, SQLObjectTypeEnum.valueOf(obj.type), config);
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

    private Future<List<SQLResultSetTableModel>> getFunctionStructurePostgresql(final SQLObject obj, DbConfig config) {
        String sql = null;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("net/hironico/minisql/metadata/postgresql/pg_get_functiondef.sql")) {
            sql = new String(is.readAllBytes());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot load the postgresql query for listing functions.", ex);
            return null;
        }

        sql = sql.replace("?1", String.format("'%s'", obj.schemaName));
        sql = sql.replace("?2", String.format("'%s'", obj.name));

        QueryResultCallable queryCall = new QueryResultCallable(sql, config);
        return MainWindow.executorService.submit(queryCall);
    }

    private void showFunctionStructure(final SQLObject obj, DbConfig config) {
        Thread threadQuery = new Thread( () -> {
            if (config.jdbcUrl.startsWith("jdbc:postgresql")) {
                Future<List<SQLResultSetTableModel>> futText = this.getFunctionTextPostgresql(obj, config);
                Future<List<SQLResultSetTableModel>> futStructure = this.getFunctionStructurePostgresql(obj, config);

                List<SQLResultSetTableModel> modelListToDisplay = new ArrayList<>();
                try {
                    List<SQLResultSetTableModel> resultStructure = futStructure.get();
                    resultStructure = resultStructure.stream().map(r -> {
                        try {
                            r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
                            return r.transpose();
                        } catch (Exception ex) {
                            return r;
                        }
                    }).collect(Collectors.toList());
                    modelListToDisplay.addAll(resultStructure);


                    List<SQLResultSetTableModel> textList = futText.get();
                    textList.forEach(m -> m.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_SQL));
                    modelListToDisplay.addAll(textList);
                } catch (InterruptedException | ExecutionException ie) {
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
            SQLObjectTypeEnum sqlObjectType = SQLObjectTypeEnum.valueOf(obj.type.replaceAll(" ", "_"));
            MetadataResultCallable call = new MetadataResultCallable(obj.schemaName, obj.name, sqlObjectType, config);
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
                textResult.forEach(r -> r.setDisplayType(SQLResultSetTableModel.DISPLAY_TYPE_SQL));
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

    private void displayResults(List<SQLResultSetTableModel> modelListToDisplay, SQLObject objectToDisplay) {
        JComponent resultComp = QueryPanel.getResultComponentTab(modelListToDisplay);
        String tabTitle = objectToDisplay.schemaName + "." + objectToDisplay.name + " (" + objectToDisplay.type + ")";
        MainWindow.getInstance().displayCloseableComponent(resultComp, tabTitle);
    }
}