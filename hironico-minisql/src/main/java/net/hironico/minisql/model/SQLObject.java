package net.hironico.minisql.model;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Logger;

import net.hironico.minisql.DbConfig;

public class SQLObject {
    private static final Logger LOGGER = Logger.getLogger(SQLObject.class.getName());

    public String schemaName;
    public String name;
    public SQLObjectTypeEnum type;
    public enum DDLActionEnum {
        CREATE, DROP
    }

    public String getDDL(DDLActionEnum action) {
        switch(action) {
            case CREATE:
                return getDDLCreate();

            case DROP:
                return getDDLDrop();

            default:
                return "";
        }
    }

    public String getDDLCreate() {
        return "";
    }

    public String getDDLDrop() {
        return "";
    }

    public static String getCompatibleType(SQLColumn col) {
        if (col.typeName.toUpperCase().startsWith("TIMESTAMP")) {
            return "TIMESTAMP";
        }

        if (col.typeName.toUpperCase().startsWith("DATETIME")) {
            return "DATETIME";
        }

        return col.typeName.toUpperCase()
                .replace("NUMBER", "NUMERIC")
                .replace("VARCHAR2", "VARCHAR")
                .replace("NVAR", "VAR");
    }

    /**
     * Executes an update statement on the database refered by the provided dbconfig 
     * @param sql command to execute 
     * @param dbConfig the target database to execute the common onto
     * @return update count from the common execute update.
     * @throws Exception if anything goes wrong : connection, driver problem, invalid common...
     */
    protected int executeUpdate(String sql, DbConfig dbConfig) throws Exception {
        try(Connection con = dbConfig.getConnection();
            Statement stmt = con.createStatement()) {
            LOGGER.fine(String.format("Execute update for: %s", sql));
            return stmt.executeUpdate(sql);
        }
    }

    @Override
    public String toString() {
        return String.join(".", schemaName, name);
    }
}
