package net.hironico.minisql.ctrl;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.model.SQLResultSetTableModel;

public class MetadataResultCallable implements Callable<List<SQLResultSetTableModel>> {

    private static final Logger LOGGER = Logger.getLogger(MetadataResultCallable.class.getName());

    private final String schemaName;
    private final String objectName;
    private final SQLObjectTypeEnum objectType;
    private final DbConfig config;

    public MetadataResultCallable(SQLObject obj, DbConfig config) {
        this(obj.schemaName, obj.name, obj.type, config);
    }

    public MetadataResultCallable(String schemaName, String objectName, SQLObjectTypeEnum objectType, DbConfig config) {
        super();
        this.schemaName = schemaName;
        this.objectName = objectName;
        this.objectType = objectType;
        this.config = config;
    }

    private SQLResultSetTableModel getTableColumns(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(null, schemaName, objectName, null)) {
            return new SQLResultSetTableModel(resultSet, "Columns", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
    }

    private SQLResultSetTableModel getTableIndexes(DatabaseMetaData metaData) throws SQLException {
        try(ResultSet result = metaData.getIndexInfo(null, schemaName, objectName, false, false)) {
            return new SQLResultSetTableModel(result, "Index(es)", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
    }

    private SQLResultSetTableModel getTablePrivileges(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet rs = metaData.getTablePrivileges(null, schemaName, objectName)) {
            return new SQLResultSetTableModel(rs, "Grants", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
    }

    private SQLResultSetTableModel getTableForeignKeys(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet rs = metaData.getImportedKeys(null, schemaName, objectName)) {
            return new SQLResultSetTableModel(rs, "Foreign keys", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
    }

    private SQLResultSetTableModel getProcedureColumns(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet rs = metaData.getProcedureColumns(null, schemaName, objectName, null)) {
            return new SQLResultSetTableModel(rs, "Columns", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
    }

    /**
     * Gets the table triggers from its schema and table name. Can only be invoked if the object
     * is of table nature.
     * @return SQLResultSetTableModel containing trigger info.
     * @throws SQLException in case of any problem while requesting the database.
     */
    private SQLResultSetTableModel getTableTriggers() throws SQLException {
        try (Connection con = config.getConnection()) {
            ResultSet rsTriggers = getTriggerMetaData(con);
            if (rsTriggers == null) {
                return null;
            }
            return new SQLResultSetTableModel(rsTriggers, "Triggers", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    private ResultSet getTriggerMetaData(Connection con) throws  SQLException{
        String query = this.getTriggerQuery();
        if (query == null) {
            return null;
        }
        Statement stmt = con.createStatement();
        return stmt.executeQuery(query);
    }

    private String getTriggerQuery() {
        if (config.jdbcUrl.contains("postgres")) {
            return String.format("""
                SELECT trigger_name,
                       event_manipulation,
                       action_statement,
                       action_timing
                FROM   information_schema.triggers
                WHERE  trigger_schema = '%s'
                  AND  event_object_table = '%s'
                """, schemaName, objectName);
        }

        if (config.jdbcUrl.contains("oracle")) {
            return String.format("""
                    SELECT owner,
                           trigger_name,
                           trigger_type,
                           table_name,
                           status
                    FROM   all_triggers
                    WHERE  owner      = %s
                      AND  table_name = %s
                    """, schemaName, objectName);
        }

        if (config.jdbcUrl.contains("tds")) {
            return String.format("""
                    SELECT trg.name          AS trigger_name,
                           trg.type_desc     AS trigger_type,
                           obj.name          AS table_name,
                           m.definition      AS trigger_definition
                    FROM   sys.triggers trg
                    JOIN   sys.tables   obj ON trg.parent_id = obj.object_id
                    CROSS APPLY sys.sql_modules m ON m.object_id = trg.object_id
                    WHERE  obj.name = %s
                    """, objectName);
        }

        LOGGER.severe("Triggers metadata retrieve for this JDBC URL is unsupported: " + config.jdbcUrl);
        return null;
    }

    public List<SQLResultSetTableModel> call() {
        List<SQLResultSetTableModel> result = new ArrayList<>();

        try (Connection con = this.config.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();

            switch (objectType) {
            case TABLE:
            case MATERIALIZED_VIEW:
                SQLResultSetTableModel resultTableCols = getTableColumns(metaData);
                result.add(resultTableCols);

                SQLResultSetTableModel resultIndexes = getTableIndexes(metaData);
                result.add(resultIndexes);

                SQLResultSetTableModel resultPriv = getTablePrivileges(metaData);
                result.add(resultPriv);

                SQLResultSetTableModel resultFK = getTableForeignKeys(metaData);
                result.add(resultFK);

                SQLResultSetTableModel resultTriggers = getTableTriggers();
                if (resultTriggers != null) {
                    result.add(resultTriggers);
                }
                break;

                case VIEW:
                SQLResultSetTableModel resultViewCols = getTableColumns(metaData);
                result.add(resultViewCols);

                SQLResultSetTableModel resultViewPriv = getTablePrivileges(metaData);
                result.add(resultViewPriv);
                break;

            case PROCEDURE:
                SQLResultSetTableModel resultProc = getProcedureColumns(metaData);
                result.add(resultProc);
                break;

            default:
                break;
            }

        } catch (InvocationTargetException | SQLException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException sqle) {
            LOGGER.log(Level.SEVERE, "SAQ Exception occurred while loading driver.", sqle);
            throw new RuntimeException(sqle);
        }

        return result;
    }
}