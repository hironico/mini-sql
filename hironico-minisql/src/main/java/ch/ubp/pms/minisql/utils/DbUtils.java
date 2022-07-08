package ch.ubp.pms.minisql.utils;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.utils.json.JSONFile;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class DbUtils {
    private static final Logger LOGGER = Logger.getLogger(DbUtils.class.getName());

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Utility to get the result of an SQL qeury into JSON formatted string.
     * JSON properties are deduced from the meta data produced by the result of the query.
     * This method works only if the sqlk query is actually returning data as a result, that is, update queries
     * will not work (most propbably).
     * @param dbConfig the database config to use for running the provided query
     * @param sql the query to get the JSON result from
     * @return the JSON formatted result of the sql query
     * @since 2.18.x
     */
    public static String getJsonResultSet(DbConfig dbConfig, String sql) throws SQLException {
        if (dbConfig == null) {
            LOGGER.severe("Cannot get JSON resultset for a null db config.");
            throw new SQLException("Cannot get JSON resultset for a null db config.");
        }

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Recommend to use jackson-ObjectMapper to streaming json directly to outputstream
            String result = JSONFile.serialize(DbUtils.serialize(rs));

            return result;
        } catch (Exception src) {
            throw new SQLException(src);
        }
    }

    /**
     * Convert the rows returned by the result set into a list of map associated properties.
     * Each key is the column name and the value is converted as a set of properties if possible.
     * this works only if the values are text based and parsed using the given string : propSeparator is
     * used to identifies key/value pairs and kvSeparator is used to identifies key and value.
     * @param dbConfig the config to connect to
     * @param sql the sql to get result from
     * @param propSeparator used to split properties (aka key value pairs)
     * @param,kvSeparator used to split key and value for each property
     * @return list of mapped properties to a column
     * @throws Exception in case of any problem
     */
    public static List<Map<String, Properties>> getPropertiesResultSet(DbConfig dbConfig, String sql, String propSeparator, String kvSeparator) throws Exception {
        if (dbConfig == null) {
            LOGGER.severe("Cannot get Properties resultset for a null db config.");
            throw new SQLException("Cannot get Properties resultset for a null db config.");
        }

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

             List<Map<String, Object>> rowList = DbUtils.serialize(rs);
             List<Map<String, Properties>> result = new ArrayList<>();

             rowList.forEach(row -> {
                 Map<String, Properties> propRow = new HashMap<>();
                 row.forEach((k,v) -> {
                     Properties props = DbUtils.convertToProps(v, propSeparator, kvSeparator);
                     propRow.put(k, props);
                 });
                 result.add(propRow);
             });

             return result;

        } catch (Exception src) {
            throw new SQLException(src);
        }
    }

    private static Properties convertToProps(Object obj, String propSeparator, String kvSeparator) {
        if (obj == null) {
            return null;
        }

        Properties result = new Properties();

        String str = obj.toString();
        String[] propSet = str.split(propSeparator);
        for (String keyValueStr : propSet) {
            String[] keyValue = keyValueStr.split(kvSeparator);
            if (keyValue.length != 2) {
                LOGGER.severe("Cannot parse property for string: " + keyValueStr);
            } else {
                result.setProperty(keyValue[0], keyValue[1]);
            }
        }

        return result;
    }

    /**
     * Serialize a resultset object into a List of Maps. Each map represent a row of the resultset
     * wich key/value pairs are column names with their values.
     * DATE, TIME and TIMESTAMP objects are formatted using the date format defined statically in this class.
     * <strong>Please note that if passed a resultset object, then the resultset is NOT closed
     * by this method.</strong>
     * @see #dateTimeFormat
     * @see #timeFormat
     */
    public static List<Map<String, Object>> serialize(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        String[] colNames = new String[columnCount];
        for (int col = 1; col <= columnCount; col++) {
            colNames[col - 1] = rsmd.getColumnName(col);
        }

        while (rs.next()) {
            // Represent a row in DB. Key: Column name, Value: Column value
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                // Note that the index is 1-based
                String colName = colNames[i - 1];

                Object colVal = rs.getObject(i);

                // lets format timestamps and related stuff into strings.
                switch (rsmd.getColumnType(i)) {
                    case Types.TIMESTAMP:
                        colVal = rs.wasNull() ? null : dateTimeFormat.format(rs.getTimestamp(i));
                        break;
                    case Types.DATE:
                        colVal = rs.wasNull() ? null : dateTimeFormat.format(rs.getDate(i));
                        break;

                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        colVal = rs.wasNull() ? null : timeFormat.format(rs.getTime(i));
                        break;

                    case Types.CLOB:
                        try {
                            Clob clob = rs.getClob(i);
                            Reader is = clob.getCharacterStream();
                            char[] buffer = new char[1024];
                            int read = is.read(buffer, 0, buffer.length);
                            StringBuffer sb = new StringBuffer();
                            while (read > 0) {
                                sb.append(new String(buffer, 0, read));
                                read = is.read(buffer, 0, buffer.length);
                            }
                            is.close();
                            colVal = sb.toString();
                        } catch (IOException ioe) {
                            throw new SQLException(ioe);
                        }

                    default:
                        // NOP
                        break;
                }

                row.put(colName, colVal);
            }
            rows.add(row);
        }

        return rows;
    }
}
