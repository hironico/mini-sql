package ch.ubp.pms.minisql.ctrl;

import ch.ubp.pms.minisql.DbConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaListCallable implements Callable<List<String>>, Supplier<List<String>> {
    private static final Logger LOGGER = Logger.getLogger(SchemaListCallable.class.getName());

    private DbConfig configToUse;

    public SchemaListCallable(DbConfig configToUse) {
        this.configToUse = configToUse;
    }

    @Override
    public List<String> call() {
        List<String> result = new ArrayList<>();
        try (Connection con = configToUse.getConnection();
             ResultSet rs = con.getMetaData().getSchemas()) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Throwable t) {
            String msg = "Error while getting the table list. See log." + t.getMessage();
            result.add(msg);
            LOGGER.log(Level.SEVERE, msg, t);
        }

        Collections.sort(result);

        return result;
    }

    @Override
    public List<String> get() {
        return call();
    }
}