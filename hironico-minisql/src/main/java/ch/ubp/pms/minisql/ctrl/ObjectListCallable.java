package ch.ubp.pms.minisql.ctrl;

import java.util.concurrent.Callable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.logging.Logger;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.minisql.model.SQLObjectTypeEnum;

public class ObjectListCallable implements Callable<List<String[]>>, Supplier<List<String[]>> {

    private static final Logger LOGGER = Logger.getLogger(ObjectListCallable.class.getName());

    private String schemaName;
    private DbConfig configToUse;

    public ObjectListCallable(DbConfig configToUse, String schemaName) {
        this.configToUse = configToUse;
        this.schemaName = schemaName;
    }

    public List<String[]> call() {
        if (this.schemaName == null) {
            LOGGER.severe("Cannot get object list for null schema.");
            return null;
        }

        Connection con = null;
        List<String[]> result = new ArrayList<>();
        try {
            if (configToUse == null) {
                LOGGER.severe("Config to use is null. Cannot get object list.");
                return null;
            }
            con = configToUse.getConnection();
            DatabaseMetaData metaData = con.getMetaData();

            // tables et vues

            ResultSet rs = metaData.getTables(null, schemaName, null, null);
            while (rs.next()) {
                String row[] = new String[3];
                row[0] = rs.getString(2);
                row[1] = rs.getString(3);
                row[2] = rs.getString(4);

                result.add(row);
            }
            rs.close();

            // proc stockess
            rs = metaData.getProcedures(null, schemaName, null);
            while (rs.next()) {
                String row[] = new String[3];
                row[0] = rs.getString(2);
                row[1] = rs.getString(3);
                row[2] = SQLObjectTypeEnum.PROCEDURE.toString();

                result.add(row);
            }
            rs.close();

            // Collections.sort(result);

        } catch (Throwable t) {
            t.printStackTrace();
            String[] row = { "Error while getting the table list. See log.", t.getMessage(), "" };
            result.add(row);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ignored) {
                    // nop
                }
            }
        }

        return result;
    }

    @Override
    public List<String[]> get() {
        return call();
    }
}