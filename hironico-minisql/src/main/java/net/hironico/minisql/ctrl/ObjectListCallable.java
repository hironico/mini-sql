package net.hironico.minisql.ctrl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Statement;
import java.util.concurrent.Callable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import org.postgresql.PGConnection;

public class ObjectListCallable implements Callable<List<String[]>>, Supplier<List<String[]>> {

    private static final Logger LOGGER = Logger.getLogger(ObjectListCallable.class.getName());

    private final String schemaName;
    private final DbConfig configToUse;
    private final SQLObjectTypeEnum objectFilter;

    public ObjectListCallable(DbConfig configToUse, String schemaName) {
        this(configToUse, schemaName, null);
    }

    public ObjectListCallable(DbConfig configToUse, String schemaName, SQLObjectTypeEnum objectFilter) {
        this.configToUse = configToUse;
        this.schemaName = schemaName;
        this.objectFilter = objectFilter;
    }

    public List<String[]> call() {
        if (this.schemaName == null) {
            LOGGER.severe("Cannot get object list for null schema.");
            return null;
        }

        if (configToUse == null) {
            LOGGER.severe("Config to use is null. Cannot get object list.");
            return null;
        }

        List<String[]> result = new ArrayList<>();
        try (Connection con = configToUse.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();

            // tables and views (sequence if postgres come with table metadata queries...)
            if (this.objectFilter == null || SQLObjectTypeEnum.isTableOrView(objectFilter) || this.objectFilter == SQLObjectTypeEnum.SEQUENCE) {
                List<String> types = new ArrayList<String>();
                if (this.objectFilter == null) {
                    types = null;
                } else {
                    ResultSet rsTypes = metaData.getTableTypes();
                    String objectFilterStr = this.objectFilter.toString();
                    while(rsTypes.next()) {
                        String theType = rsTypes.getString(1).toUpperCase();
                        if (theType.contains(objectFilterStr)) {
                            types.add(theType);
                        }
                    }
                    rsTypes.close();
                }

                ResultSet rs = metaData.getTables(null, schemaName, null, types == null ? null : types.toArray(new String[0]));
                while (rs.next()) {
                    String[] row = new String[3];
                    row[0] = rs.getString(2);
                    row[1] = rs.getString(3);
                    row[2] = rs.getString(4);

                    LOGGER.fine("Table found: " + String.join(" ; ", row));

                    result.add(row);
                }
                rs.close();
            }

            // procedures
            if (this.objectFilter == null || this.objectFilter == SQLObjectTypeEnum.PROCEDURE) {
                ResultSet rs = metaData.getProcedures(null, schemaName, null);
                while (rs.next()) {
                    String[] row = new String[3];
                    row[0] = rs.getString(2);
                    row[1] = rs.getString(3);
                    row[2] = SQLObjectTypeEnum.PROCEDURE.toString();

                    LOGGER.fine("Procedure found: " + String.join(" ; ", row));

                    result.add(row);
                }
                rs.close();
            }

            // sequences for oracle (postgres sequences come with table type...)
            if (this.objectFilter == null || this.objectFilter == SQLObjectTypeEnum.SEQUENCE) {
                if (configToUse.jdbcUrl.contains("oracle")) {
                    LOGGER.info("Loading sequences ...");
                    String sql = String.format("SELECT s.sequence_name FROM all_sequences s WHERE s.sequence_owner = '%s' ORDER BY s.sequence_name", schemaName);
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        String[] row = new String[3];
                        row[0] = schemaName;
                        row[1] = rs.getString(1);
                        row[2] = SQLObjectTypeEnum.SEQUENCE.toString();

                        LOGGER.fine("Sequence found: " + String.join(" ; ", row));

                        result.add(row);
                    }
                }
            }

            // functions for postgres
            if (this.objectFilter == null || this.objectFilter == SQLObjectTypeEnum.FUNCTION) {
                if (configToUse.jdbcUrl.contains("postgres")) {
                    LOGGER.fine("Loading Postgresql functions...");
                    String sql = null;
                    try (InputStream is = getClass().getClassLoader().getResourceAsStream("net/hironico/minisql/metadata/postgresql/pg_get_functions.sql")) {
                        sql = new String(is.readAllBytes());
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Cannot load the postgresql query for listing functions.", ex);
                    }

                    if (sql != null) {
                        Statement stmt = con.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        while (rs.next()) {
                            if (rs.getString("schema").equalsIgnoreCase(schemaName)) {
                                String[] row = new String[3];
                                row[0] = schemaName;
                                row[1] = rs.getString("name");
                                row[2] = SQLObjectTypeEnum.FUNCTION.toString();

                                LOGGER.fine("Function found: " + String.join(" ; ", row));

                                result.add(row);
                            }
                        }
                    }
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            String[] row = { "Error while getting the object list. See log.", t.getMessage(), "" };
            result.add(row);
        }

        return result;
    }

    @Override
    public List<String[]> get() {
        return call();
    }
}