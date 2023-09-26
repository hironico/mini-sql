package net.hironico.minisql.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.model.SQLResultSetTableModel;

public class MetadataResultCallable implements Callable<List<SQLResultSetTableModel>> {

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

    private SQLResultSetTableModel getProcedureColumns(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet rs = metaData.getProcedureColumns(null, schemaName, objectName, null)) {
            return new SQLResultSetTableModel(rs, "Columns", "N/A", SQLResultSetTableModel.DISPLAY_TYPE_TABLE);
        }
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

        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException sqle) {
            sqle.printStackTrace();
        }

        return result;
    }
}