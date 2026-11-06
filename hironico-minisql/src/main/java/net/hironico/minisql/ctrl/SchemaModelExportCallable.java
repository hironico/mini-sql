package net.hironico.minisql.ctrl;

import net.hironico.common.utils.json.JSONFile;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.model.schema.SchemaModel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Exports the structure of the given tables and views as a JSON document described by {@link SchemaModel}:
 * schema name, then tables by name with their columns, primary key, indexes and foreign keys.
 * If the objects belong to several schemas, the result is a JSON array of one document per schema.
 * All the metadata is read with a single connection, using standard JDBC DatabaseMetaData calls.
 */
public class SchemaModelExportCallable implements java.util.concurrent.Callable<String> {

    private static final Logger LOGGER = Logger.getLogger(SchemaModelExportCallable.class.getName());

    private final List<SQLObject> objects;
    private final DbConfig config;

    /**
     * Creates a new export of the given objects.
     *
     * @param objects tables and views to export, other object types are ignored
     * @param config  connection to read the metadata from
     */
    public SchemaModelExportCallable(List<SQLObject> objects, DbConfig config) {
        this.objects = objects;
        this.config = config;
    }

    /**
     * Tells if the object can be exported by this callable.
     *
     * @param obj the object to check
     * @return true for tables, views and materialized views (system ones included)
     */
    public static boolean isExportable(SQLObject obj) {
        return obj != null && obj.type != null && switch (obj.type) {
            case TABLE, SYSTEM_TABLE, VIEW, SYSTEM_VIEW, MATERIALIZED_VIEW -> true;
            default -> false;
        };
    }

    @Override
    public String call() throws Exception {
        try (Connection con = config.getConnection()) {
            List<SchemaModel> models = buildModels(con.getMetaData(), objects, config.name);
            return toJson(models);
        }
    }

    /**
     * Serializes the models: a single object if there is only one schema, an array otherwise.
     *
     * @param models the schema models to serialize
     * @return the JSON document
     * @throws Exception in case of serialization problem
     */
    public static String toJson(List<SchemaModel> models) throws Exception {
        return JSONFile.serialize(models.size() == 1 ? models.get(0) : models);
    }

    /**
     * Builds one schema model per schema of the given objects.
     *
     * @param metaData       metadata of the source connection
     * @param objects        tables and views to export, other object types are ignored
     * @param connectionName name of the connection, stored in the document
     * @return the schema models sorted by schema name
     * @throws SQLException in case of problem while reading the metadata
     */
    public static List<SchemaModel> buildModels(DatabaseMetaData metaData, List<SQLObject> objects, String connectionName) throws SQLException {
        SchemaModel.Source source = new SchemaModel.Source(connectionName,
                metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
        String exportDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // schema name -> table name -> table, all sorted by name
        Map<String, Map<String, SchemaModel.Table>> schemas = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        for (SQLObject obj : objects) {
            if (!isExportable(obj)) {
                LOGGER.warning("Unsupported object type for export model: " + obj.type + " " + obj.name);
                continue;
            }
            SchemaModel.Table table = buildTable(metaData, obj);
            schemas.computeIfAbsent(obj.schemaName, k -> new TreeMap<>()).put(obj.name, table);
        }

        List<SchemaModel> result = new ArrayList<>();
        schemas.forEach((schema, tables) ->
                result.add(new SchemaModel(SchemaModel.FORMAT_VERSION, exportDate, source, schema, tables)));
        return result;
    }

    private static SchemaModel.Table buildTable(DatabaseMetaData metaData, SQLObject obj) throws SQLException {
        List<SchemaModel.Column> columns = loadColumns(metaData, obj);

        // views have no keys nor indexes
        if (obj.type == SQLObjectTypeEnum.VIEW || obj.type == SQLObjectTypeEnum.SYSTEM_VIEW) {
            return new SchemaModel.Table(obj.type.toString(), columns, null, List.of(), List.of());
        }

        SchemaModel.PrimaryKey pk = loadPrimaryKey(metaData, obj);
        List<SchemaModel.Index> indexes = loadIndexes(metaData, obj, pk);
        List<SchemaModel.ForeignKey> foreignKeys = loadForeignKeys(metaData, obj);
        return new SchemaModel.Table(obj.type.toString(), columns, pk, indexes, foreignKeys);
    }

    private static List<SchemaModel.Column> loadColumns(DatabaseMetaData metaData, SQLObject obj) throws SQLException {
        List<SchemaModel.Column> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, obj.schemaName, obj.name, null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                Integer size = getInteger(rs, "COLUMN_SIZE");
                Integer scale = getInteger(rs, "DECIMAL_DIGITS");
                boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String defaultValue = trimToNull(rs.getString("COLUMN_DEF"));
                int position = rs.getInt("ORDINAL_POSITION");
                boolean autoIncrement = "YES".equalsIgnoreCase(getOptionalString(rs, "IS_AUTOINCREMENT"));

                columns.add(new SchemaModel.Column(name, position, typeName, jdbcTypeName(dataType),
                        size, scale, nullable, autoIncrement, defaultValue));
            }
        }
        columns.sort(Comparator.comparingInt(SchemaModel.Column::position));
        return columns;
    }

    private static SchemaModel.PrimaryKey loadPrimaryKey(DatabaseMetaData metaData, SQLObject obj) throws SQLException {
        // key sequence -> column name
        Map<Integer, String> columns = new TreeMap<>();
        String pkName = null;
        try (ResultSet rs = metaData.getPrimaryKeys(null, obj.schemaName, obj.name)) {
            while (rs.next()) {
                columns.put(rs.getInt("KEY_SEQ"), rs.getString("COLUMN_NAME"));
                if (pkName == null) {
                    pkName = rs.getString("PK_NAME");
                }
            }
        }
        return columns.isEmpty() ? null : new SchemaModel.PrimaryKey(pkName, new ArrayList<>(columns.values()));
    }

    private static List<SchemaModel.Index> loadIndexes(DatabaseMetaData metaData, SQLObject obj, SchemaModel.PrimaryKey pk) throws SQLException {
        // index name -> uniqueness, and index name -> ordinal position -> column (sorted by index name)
        Map<String, Boolean> uniqueness = new TreeMap<>();
        Map<String, TreeMap<Integer, SchemaModel.IndexColumn>> indexColumns = new TreeMap<>();

        // approximate = true: do not ask the database to compute statistics (costly on Oracle)
        try (ResultSet rs = metaData.getIndexInfo(null, obj.schemaName, obj.name, false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName == null || rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }
                uniqueness.putIfAbsent(indexName, !rs.getBoolean("NON_UNIQUE"));
                String order = rs.getString("ASC_OR_DESC");
                order = "A".equalsIgnoreCase(order) ? "ASC" : "D".equalsIgnoreCase(order) ? "DESC" : null;
                TreeMap<Integer, SchemaModel.IndexColumn> cols = indexColumns.computeIfAbsent(indexName, k -> new TreeMap<>());
                cols.put(rs.getInt("ORDINAL_POSITION"), new SchemaModel.IndexColumn(rs.getString("COLUMN_NAME"), order));
            }
        }

        List<SchemaModel.Index> indexes = new ArrayList<>();
        indexColumns.forEach((name, cols) -> {
            List<SchemaModel.IndexColumn> columnList = new ArrayList<>(cols.values());
            boolean unique = uniqueness.get(name);
            indexes.add(new SchemaModel.Index(name, unique, isPrimaryKeyIndex(name, unique, columnList, pk), columnList));
        });
        return indexes;
    }

    /**
     * An index backs the primary key if it has the same name as the constraint or,
     * when names differ (PostgreSQL, H2...), if it is unique on exactly the key columns.
     */
    private static boolean isPrimaryKeyIndex(String name, boolean unique, List<SchemaModel.IndexColumn> columns, SchemaModel.PrimaryKey pk) {
        if (pk == null) {
            return false;
        }
        if (pk.name() != null && pk.name().equalsIgnoreCase(name)) {
            return true;
        }
        List<String> names = columns.stream().map(SchemaModel.IndexColumn::name).toList();
        return unique && names.equals(pk.columns());
    }

    /** Columns of a foreign key being loaded, keyed by KEY_SEQ to keep the key order */
    private static final class ForeignKeyBuilder {
        String referencedSchema;
        String referencedTable;
        String onUpdate;
        String onDelete;
        final TreeMap<Integer, String[]> columns = new TreeMap<>();
    }

    private static List<SchemaModel.ForeignKey> loadForeignKeys(DatabaseMetaData metaData, SQLObject obj) throws SQLException {
        Map<String, ForeignKeyBuilder> builders = new TreeMap<>();
        try (ResultSet rs = metaData.getImportedKeys(null, obj.schemaName, obj.name)) {
            while (rs.next()) {
                String pkTable = rs.getString("PKTABLE_NAME");
                String fkName = rs.getString("FK_NAME");
                // some drivers (JTDS) do not return the foreign key name
                if (fkName == null) {
                    fkName = String.join("_", Objects.toString(pkTable, ""), Objects.toString(rs.getString("FKTABLE_NAME"), ""));
                }
                ForeignKeyBuilder fk = builders.computeIfAbsent(fkName, k -> new ForeignKeyBuilder());
                fk.referencedSchema = rs.getString("PKTABLE_SCHEM");
                fk.referencedTable = pkTable;
                fk.onUpdate = ruleName(rs.getShort("UPDATE_RULE"));
                fk.onDelete = ruleName(rs.getShort("DELETE_RULE"));
                fk.columns.put((int) rs.getShort("KEY_SEQ"),
                        new String[]{rs.getString("FKCOLUMN_NAME"), rs.getString("PKCOLUMN_NAME")});
            }
        }

        List<SchemaModel.ForeignKey> result = new ArrayList<>();
        builders.forEach((name, fk) -> result.add(new SchemaModel.ForeignKey(name,
                fk.columns.values().stream().map(c -> c[0]).toList(),
                fk.referencedSchema, fk.referencedTable,
                fk.columns.values().stream().map(c -> c[1]).toList(),
                fk.onUpdate, fk.onDelete)));
        return result;
    }

    /**
     * Converts a JDBC foreign key rule (DatabaseMetaData.importedKey*) to its SQL name.
     *
     * @param rule the JDBC rule
     * @return CASCADE, SET NULL, SET DEFAULT, RESTRICT or NO ACTION
     */
    public static String ruleName(short rule) {
        return switch (rule) {
            case DatabaseMetaData.importedKeyCascade -> "CASCADE";
            case DatabaseMetaData.importedKeySetNull -> "SET NULL";
            case DatabaseMetaData.importedKeySetDefault -> "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict -> "RESTRICT";
            default -> "NO ACTION";
        };
    }

    /**
     * Get the portable JDBC type name of a java.sql.Types value.
     *
     * @param dataType java.sql.Types value
     * @return the JDBCType name, or OTHER if the value is vendor specific
     */
    public static String jdbcTypeName(int dataType) {
        try {
            return JDBCType.valueOf(dataType).getName();
        } catch (IllegalArgumentException ex) {
            return JDBCType.OTHER.getName();
        }
    }

    private static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    /** Reads a column that old drivers may not provide (JDBC 4.1 additions such as IS_AUTOINCREMENT). */
    private static String getOptionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
