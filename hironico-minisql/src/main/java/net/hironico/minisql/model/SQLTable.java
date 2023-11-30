package net.hironico.minisql.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.hironico.minisql.DbConfig;

/**
 * SQLTable object represents the metadata assocated to a TABLE in a relational-like database.
 */
public class SQLTable extends SQLObject {

    private static final Logger LOGGER = Logger.getLogger(SQLTable.class.getName());

    protected final List<SQLColumn> columns = new ArrayList<>();
    protected final Map<String, List<SQLTableForeignKey>> foreignKeys = new HashMap<>();

    public SQLTable(String schema, String name) {
        super();
        this.name = name;
        this.schemaName = schema;
        this.type = SQLObjectTypeEnum.TABLE;
    }

    public List<SQLColumn> getColumns() {
        return this.columns;
    }

    public Map<String, List<SQLTableForeignKey>> getForeignKeys() {
        return this.foreignKeys;
    }

    public String getDDLAddColumn(SQLColumn col) {
        String sql = "ALTER TABLE %s.%s ADD %s %s %s %s";  // name varchar(60) null defaultVal
        String nullable = col.nullable ? "NULL" : "NOT NULL";
        String defaultValue = col.defaultValue != null && !"".equals(col.defaultValue) ? "DEFAULT " + col.defaultValue : "";
        String typeName = getCompatibleType(col);
        if (col.size != null && !col.typeName.startsWith("TIME") && !col.typeName.startsWith("DATE")) {
            typeName += "(" + col.size;
            if (col.scale != null && !col.typeName.contains("CHAR")) {
                typeName += ", " + col.scale + ") ";
            } else {
                typeName += ") ";
            }
        }

        return String.format(sql, this.schemaName, this.name, col.name, typeName, nullable, defaultValue);
    }

    public boolean addColumn(SQLColumn col, DbConfig dbConfig) {
        if (col == null || dbConfig == null) {
            return false;
        }

        String sql = getDDLAddColumn(col);
        try {
            LOGGER.info("Executing: " + sql);
            return executeUpdate(sql, dbConfig) > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while adding column.", ex);
            return false;
        }
    }

    private String getDDLDropColumn(SQLColumn col) {
        String sql = "ALTER TABLE %s.%s DROP %s";
        return String.format(sql, this.schemaName, this.name, col.name);
    }

    public boolean dropColumn(SQLColumn col, DbConfig dbConfig) {
        if (col == null || dbConfig == null) {
            return false;
        }

        String sql = getDDLDropColumn(col);
        try {
            return executeUpdate(sql, dbConfig) > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while dropping column.", ex);
            return false;
        }
    }

    private String getDDLRenameColumn(SQLColumn source, String newName) {
        String sql = "ALTER TABLE %s.%s RENAME %s TO %s";
        return String.format(sql, this.schemaName, this.name, source.name, newName);
    }

    public boolean renameColumn(SQLColumn source, String newName, DbConfig dbConfig) {
        if (source == null || newName == null || "".equals(newName) || dbConfig == null) {
            return false;
        }

        String sql = getDDLRenameColumn(source, newName);
        try {
            return executeUpdate(sql, dbConfig) > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while renaming column " + source.name + " -> " + newName, ex);
            return false;
        }
    }

    private String getDDLColumnCopy(SQLColumn source, SQLColumn target) {
        String sql = "UPDATE %s.%s SET %s = %s";
        return String.format(sql, this.schemaName, this.name, target.name, source.name);
    }

    public boolean columnCopy(SQLColumn source, SQLColumn target, DbConfig dbConfig) {
        if (source == null || target == null || dbConfig == null) {
            return false;
        }

        String sql = getDDLColumnCopy(source, target);
        try {
            return executeUpdate(sql, dbConfig) > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while copying data to column " + source + " -> " + target, ex);
            return false;
        }
    }

    public String getDDLDrop() {
        return String.format("DROP TABLE %s.%s", this.schemaName, this.name);
    }

    public boolean drop(DbConfig dbConfig) {
        String sql = getDDLDrop();
        LOGGER.warning(sql);

        try {
            return executeUpdate(sql, dbConfig) > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while dropping table.", ex);
            return false;
        }
    }

    @Override
    public String getDDLCreate() {
        String sql = "CREATE TABLE %s.%s (\n";

        sql = String.format(sql, this.schemaName, this.name);

        int index = 0;
        for(SQLColumn col : this.columns) {            
            col.typeName = getCompatibleType(col);
            String colStr = String.format("%s %s", col.name, col.typeName);

            if (index > 0) {
                colStr = ",\n    " + colStr;
            }

            if (col.size != null && !col.typeName.startsWith("TIME") && !col.typeName.startsWith("DATE")) {
                colStr += "(" + col.size;
                if (col.scale != null && !col.typeName.contains("CHAR")) {
                    colStr += ", " + col.scale + ") ";
                } else {
                    colStr += ") ";
                }
            }

            colStr += col.nullable ? " NULL " : " NOT NULL";

            if (col.defaultValue != null && !"".equals(col.defaultValue)) {                
                colStr += " DEFAULT " + col.defaultValue;
            }

            sql += colStr;
            index++;
        }

        sql += ")";

        return sql;
    }

    public boolean create(DbConfig dbConfig) {

        if (this.columns == null || this.columns.isEmpty()) {
            LOGGER.severe(String.format("Cannot create table %s.%s since the columns are undefined or empty.", this.schemaName, this.name));
            return false;
        }

        String sql = this.getDDLCreate();

        LOGGER.warning(sql);

        try {
            executeUpdate(sql, dbConfig);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problem while creating table.", ex);
            return false;
        }

        return true;
    }

    public void alterColumn(SQLColumn target, DbConfig dbConfig) throws SQLException {
        String sql = "ALTER TABLE %s MODIFY %s %s";
        if (target.defaultValue != null && !"".equals(target.defaultValue)) {
            sql += " DEFAULT " + target.defaultValue;
        }
        sql += target.nullable ? " NULL " : " NOT NULL ";

        sql = String.format(sql, this.name, target.name, getCompatibleType(target));

        LOGGER.warning(sql);

        try(Connection con = dbConfig.getConnection();
            Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }

        // refresh current state of this SQLTable
        loadMetaData(dbConfig);
    }

    public void loadMetaData(DbConfig dbConfig) throws SQLException {
        try (Connection con = dbConfig.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            loadColumnsMetaData(metaData);
            loadForeignKey(metaData);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    public List<SQLColumn> loadColumnsMetaData(DatabaseMetaData metaData) throws SQLException, IOException {
        this.columns.clear();
        try (ResultSet resultSet = metaData.getColumns(null, schemaName, name, null)) {
            while (resultSet.next()) {
                SQLColumn column = new SQLColumn();
                column.name = resultSet.getString(4);
                column.typeName = resultSet.getString(6);
                column.size = resultSet.getInt(7);
                column.scale = resultSet.getInt(9);

                column.defaultValue = "";
                Reader reader = resultSet.getCharacterStream(13);
                if (reader != null) {
                    BufferedReader bufReader = new BufferedReader(reader);

                    String line = bufReader.readLine();
                    while (line != null) {
                        column.defaultValue += line + "\n";
                        line = bufReader.readLine();
                    }
                    reader.close();
                }

                column.nullable = "YES".equalsIgnoreCase(resultSet.getString(18));
                column.autoIncrement = "YES".equalsIgnoreCase(resultSet.getString(23));

                this.columns.add(column);
            }
        }

        return this.columns;
    }

    public void loadForeignKey(DatabaseMetaData metaData) throws SQLException {
        ResultSet rsFK = metaData.getImportedKeys(null, schemaName, name);
        while (rsFK.next()) {
//                    logger.debug("PKTABLE_NAME = " + rsFK.getString("PKTABLE_NAME") + "\n" +
//                            "PKCOLUMN_NAME = " + rsFK.getString("PKCOLUMN_NAME") + "\n" +
//                            "FKTABLE_NAME = " + rsFK.getString("FKTABLE_NAME") + "\n" +
//                            "FKCOLUMN_NAME = " + rsFK.getString("FKCOLUMN_NAME") + "\n" +
//                            "FK_NAME = " + rsFK.getString("FK_NAME") + "\n" +
//                            "KEY_SEQ = " + rsFK.getInt("KEY_SEQ"));
            String fkName = rsFK.getString("FK_NAME");

            // parfois la clé étrangére n'a pas de nom (driver JTDS).
            // on utilise alors la combinaison PK Table + FK Table
            if (fkName == null) {
                fkName = String.join("_",
                        rsFK.getString("PKTABLE_NAME"),
                        rsFK.getString("FKTABLE_NAME"));
            }

            SQLTableForeignKey fk = new SQLTableForeignKey(fkName);

            fk.schemaName = rsFK.getString("FKTABLE_SCHEM");
            fk.fkTableName = rsFK.getString("FKTABLE_NAME");
            fk.fkColumnName = rsFK.getString("FKCOLUMN_NAME");

            fk.pkSchemaName = rsFK.getString("PKTABLE_SCHEM");
            fk.pkTableName = rsFK.getString("PKTABLE_NAME");
            fk.pkColumnName = rsFK.getString("PKCOLUMN_NAME");

            fk.deleteRule = rsFK.getString("DELETE_RULE");
            fk.updateRule = rsFK.getString("UPDATE_RULE");
            fk.kewSeq = rsFK.getInt("KEY_SEQ");

            List<SQLTableForeignKey> fkList = foreignKeys.getOrDefault(fk.name, new ArrayList<>());
            foreignKeys.putIfAbsent(fk.name, fkList);
            fkList.add(fk);
        }
        rsFK.close();
    }
}