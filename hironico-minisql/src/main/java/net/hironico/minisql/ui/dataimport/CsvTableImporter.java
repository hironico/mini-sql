package net.hironico.minisql.ui.dataimport;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a table from the column definitions of the import wizard and loads the CSV records
 * into it using JDBC batches. This class has no dependency on Swing so that it can be unit tested.
 */
public class CsvTableImporter {

    /** Default number of rows sent to the database in one JDBC batch */
    public static final int DEFAULT_BATCH_SIZE = 100;

    /** Default pattern used to parse DATE values */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /** Default pattern used to parse TIMESTAMP values */
    public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** Default pattern used to parse TIME values */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /** Kind of JDBC value a SQL type is converted to */
    public enum ValueKind { STRING, INTEGER, BIGINT, DECIMAL, DATE, TIME, TIMESTAMP, BOOLEAN }

    /** Callback used to report the import progress. Called from the import thread. */
    public interface ImportListener {
        void progress(long rowsImported, long batchesCommitted);
        void log(String message);
    }

    /**
     * Summary of a finished import.
     *
     * @param rowsImported     number of rows inserted and committed
     * @param batchesCommitted number of batches executed
     * @param cancelled        true if the import was cancelled by the user
     */
    public record ImportResult(long rowsImported, long batchesCommitted, boolean cancelled) {
    }

    private final CsvFileReader reader;
    private final List<ImportColumn> columns;
    private final String schema;
    private final String tableName;
    private final boolean quotedIdentifiers;

    private int batchSize = DEFAULT_BATCH_SIZE;
    private boolean createTable = true;
    private boolean dropTableFirst = false;
    private String datePattern = DEFAULT_DATE_PATTERN;
    private String timestampPattern = DEFAULT_TIMESTAMP_PATTERN;
    private String timePattern = DEFAULT_TIME_PATTERN;

    private volatile boolean cancelRequested = false;

    /** Cache of date time formatters by pattern, avoids creating one formatter per value */
    private final Map<String, DateTimeFormatter> formatters = new HashMap<>();

    /**
     * Creates a new importer.
     *
     * @param reader            the CSV file reader (may be null when only generating SQL)
     * @param columns           columns definitions, only included ones are used
     * @param schema            target schema, null or empty for the connection default schema
     * @param tableName         target table name
     * @param quotedIdentifiers true to quote table and column names
     */
    public CsvTableImporter(CsvFileReader reader, List<ImportColumn> columns, String schema,
                            String tableName, boolean quotedIdentifiers) {
        this.reader = reader;
        this.columns = columns;
        this.schema = schema;
        this.tableName = tableName;
        this.quotedIdentifiers = quotedIdentifiers;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = Math.max(1, batchSize);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    public void setDropTableFirst(boolean dropTableFirst) {
        this.dropTableFirst = dropTableFirst;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
    }

    public void setTimePattern(String timePattern) {
        this.timePattern = timePattern;
    }

    /** Requests the running import to stop after the current batch. */
    public void cancel() {
        this.cancelRequested = true;
    }

    /** @return the columns that will be created and imported */
    public List<ImportColumn> getIncludedColumns() {
        return columns.stream().filter(ImportColumn::isIncluded).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ SQL generation

    /**
     * Turns a CSV header value into a valid SQL identifier: accents removed, non alphanumeric
     * characters replaced by underscores, must not start with a digit, unique in the given set.
     *
     * @param raw  the header value
     * @param used names already used (upper case), updated with the returned name
     * @return a valid and unique column name
     */
    public static String sanitizeIdentifier(String raw, Set<String> used) {
        String name = raw == null ? "" : Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        name = name.replaceAll("[^A-Za-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
        if (name.isEmpty()) {
            name = "COLUMN";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "C_" + name;
        }
        String candidate = name;
        int suffix = 2;
        while (used.contains(candidate.toUpperCase(Locale.ROOT))) {
            candidate = name + "_" + suffix++;
        }
        used.add(candidate.toUpperCase(Locale.ROOT));
        return candidate;
    }

    /**
     * Quotes the identifier with double quotes if quoted identifiers are enabled.
     *
     * @param identifier the identifier to quote
     * @return the quoted or unchanged identifier
     */
    public String quote(String identifier) {
        if (!quotedIdentifiers) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /** @return the table name prefixed by the schema if any */
    public String getQualifiedTableName() {
        String table = tableName == null ? "" : tableName.trim();
        if (schema == null || schema.isBlank()) {
            return quote(table);
        }
        return quote(schema.trim()) + "." + quote(table);
    }

    /** @return the DROP TABLE statement for the target table */
    public String buildDropTableSql() {
        return "DROP TABLE " + getQualifiedTableName();
    }

    /** @return the CREATE TABLE statement built from the included columns */
    public String buildCreateTableSql() {
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(getQualifiedTableName()).append(" (\n");
        List<ImportColumn> included = getIncludedColumns();
        for (int i = 0; i < included.size(); i++) {
            ImportColumn col = included.get(i);
            sql.append("    ").append(quote(col.getName())).append(' ').append(col.getSqlType().trim());
            if (!col.isNullable()) {
                sql.append(" NOT NULL");
            }
            sql.append(i < included.size() - 1 ? ",\n" : "\n");
        }
        return sql.append(")").toString();
    }

    /** @return the parameterized INSERT statement for the included columns */
    public String buildInsertSql() {
        List<ImportColumn> included = getIncludedColumns();
        String names = included.stream().map(c -> quote(c.getName())).collect(Collectors.joining(", "));
        String params = included.stream().map(c -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + getQualifiedTableName() + " (" + names + ") VALUES (" + params + ")";
    }

    // ------------------------------------------------------------------ value conversion

    /**
     * Get the kind of JDBC value to bind for the given SQL type.
     *
     * @param sqlType SQL type as entered in the wizard, like VARCHAR(150) or NUMERIC(10,2)
     * @return the value kind used to convert CSV strings
     */
    public static ValueKind getValueKind(String sqlType) {
        String type = sqlType == null ? "" : sqlType.trim().toUpperCase(Locale.ROOT);
        int paren = type.indexOf('(');
        String base = (paren >= 0 ? type.substring(0, paren) : type).trim();

        if (base.startsWith("TIMESTAMP") || base.equals("DATETIME") || base.equals("DATETIME2")) {
            return ValueKind.TIMESTAMP;
        }
        return switch (base) {
            case "INT", "INTEGER", "SMALLINT", "TINYINT", "INT2", "INT4" -> ValueKind.INTEGER;
            case "BIGINT", "INT8" -> ValueKind.BIGINT;
            case "NUMERIC", "DECIMAL", "NUMBER", "REAL", "FLOAT", "DOUBLE", "DOUBLE PRECISION", "MONEY" -> ValueKind.DECIMAL;
            case "DATE" -> ValueKind.DATE;
            case "TIME" -> ValueKind.TIME;
            case "BOOLEAN", "BOOL", "BIT" -> ValueKind.BOOLEAN;
            default -> ValueKind.STRING;
        };
    }

    /**
     * Converts a CSV string value into the Java object bound to the insert statement.
     * Empty values are converted to null.
     *
     * @param value the CSV value
     * @param kind  the target value kind
     * @return the converted value, or null
     * @throws IllegalArgumentException if the value cannot be converted
     */
    public Object convertValue(String value, ValueKind kind) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (kind == ValueKind.STRING) {
            return value;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return switch (kind) {
                case INTEGER -> Integer.valueOf(v);
                case BIGINT -> Long.valueOf(v);
                case DECIMAL -> new BigDecimal(v.replace(" ", "").replace(',', '.'));
                case DATE -> Date.valueOf(LocalDate.parse(v, formatter(datePattern)));
                case TIME -> Time.valueOf(LocalTime.parse(v, formatter(timePattern)));
                case TIMESTAMP -> Timestamp.valueOf(LocalDateTime.parse(v, formatter(timestampPattern)));
                case BOOLEAN -> parseBoolean(v);
                default -> value;
            };
        } catch (NumberFormatException | DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Cannot convert '%s' to %s: %s", value, kind, ex.getMessage()), ex);
        }
    }

    private static Boolean parseBoolean(String v) {
        return switch (v.toLowerCase(Locale.ROOT)) {
            case "true", "t", "yes", "y", "1", "oui", "o" -> Boolean.TRUE;
            case "false", "f", "no", "n", "0", "non" -> Boolean.FALSE;
            default -> throw new NumberFormatException("not a boolean value");
        };
    }

    private DateTimeFormatter formatter(String pattern) {
        return formatters.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    private static int toSqlType(ValueKind kind) {
        return switch (kind) {
            case INTEGER -> Types.INTEGER;
            case BIGINT -> Types.BIGINT;
            case DECIMAL -> Types.NUMERIC;
            case DATE -> Types.DATE;
            case TIME -> Types.TIME;
            case TIMESTAMP -> Types.TIMESTAMP;
            case BOOLEAN -> Types.BOOLEAN;
            default -> Types.VARCHAR;
        };
    }

    private static void bind(PreparedStatement ps, int index, Object value, ValueKind kind) throws SQLException {
        if (value == null) {
            ps.setNull(index, toSqlType(kind));
            return;
        }
        switch (kind) {
            case INTEGER -> ps.setInt(index, (Integer) value);
            case BIGINT -> ps.setLong(index, (Long) value);
            case DECIMAL -> ps.setBigDecimal(index, (BigDecimal) value);
            case DATE -> ps.setDate(index, (Date) value);
            case TIME -> ps.setTime(index, (Time) value);
            case TIMESTAMP -> ps.setTimestamp(index, (Timestamp) value);
            case BOOLEAN -> ps.setBoolean(index, (Boolean) value);
            default -> ps.setString(index, value.toString());
        }
    }

    // ------------------------------------------------------------------ import execution

    /**
     * Creates the table (if requested) and imports all the CSV records using JDBC batches.
     * DDL statements run with the connection auto commit mode, then the inserts are committed
     * after each batch when the database supports transactions. If an error occurs, the current
     * batch is rolled back and the exception is thrown; batches already committed are kept.
     * The connection is not closed by this method.
     *
     * @param conn     an open connection to the target database
     * @param listener progress listener, may be null
     * @return the import summary
     * @throws Exception if the table cannot be created, a value cannot be converted or inserted
     */
    public ImportResult runImport(Connection conn, ImportListener listener) throws Exception {
        ImportListener l = listener != null ? listener : new ImportListener() {
            public void progress(long rowsImported, long batchesCommitted) { }
            public void log(String message) { }
        };
        cancelRequested = false;

        List<ImportColumn> included = getIncludedColumns();
        if (included.isEmpty()) {
            throw new IllegalStateException("No column selected for the import.");
        }

        if (dropTableFirst) {
            executeDdl(conn, buildDropTableSql(), l, true);
        }
        if (createTable) {
            executeDdl(conn, buildCreateTableSql(), l, false);
        }

        boolean transactional = conn.getMetaData().supportsTransactions();
        boolean previousAutoCommit = conn.getAutoCommit();
        if (transactional) {
            conn.setAutoCommit(false);
        }

        ValueKind[] kinds = included.stream().map(c -> getValueKind(c.getSqlType())).toArray(ValueKind[]::new);
        String insertSql = buildInsertSql();
        l.log("Insert statement: " + insertSql);
        l.log(String.format("Importing with batch size %d ...", batchSize));

        long rows = 0;
        long batches = 0;
        int pending = 0;
        long recordNumber = 0;
        boolean cancelled = false;

        try (CSVParser parser = reader.open(); PreparedStatement ps = conn.prepareStatement(insertSql)) {
            Iterator<CSVRecord> it = reader.records(parser);
            while (it.hasNext()) {
                CSVRecord rec = it.next();
                recordNumber = rec.getRecordNumber();

                for (int i = 0; i < included.size(); i++) {
                    int src = included.get(i).getSourceIndex();
                    String raw = src < rec.size() ? rec.get(src) : null;
                    Object value;
                    try {
                        value = convertValue(raw, kinds[i]);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(String.format("CSV record %d, column %s: %s",
                                recordNumber, included.get(i).getName(), ex.getMessage()), ex);
                    }
                    bind(ps, i + 1, value, kinds[i]);
                }
                ps.addBatch();
                pending++;

                if (pending >= batchSize) {
                    executeBatch(conn, ps, transactional);
                    rows += pending;
                    batches++;
                    pending = 0;
                    l.progress(rows, batches);
                    if (cancelRequested) {
                        cancelled = true;
                        break;
                    }
                }
            }

            if (!cancelled && pending > 0) {
                executeBatch(conn, ps, transactional);
                rows += pending;
                batches++;
                l.progress(rows, batches);
            }
        } catch (Exception ex) {
            if (transactional) {
                rollbackQuietly(conn, l);
            }
            l.log(String.format("Import failed near CSV record %d after %d committed rows: %s",
                    recordNumber, rows, ex.getMessage()));
            throw ex;
        } finally {
            if (transactional) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ex) {
                    // connection may be broken, nothing more to do
                }
            }
        }

        l.log(String.format("%s: %d rows imported in %d batch(es) into %s.",
                cancelled ? "Import cancelled" : "Import finished", rows, batches, getQualifiedTableName()));
        return new ImportResult(rows, batches, cancelled);
    }

    private void executeBatch(Connection conn, PreparedStatement ps, boolean transactional) throws SQLException {
        ps.executeBatch();
        ps.clearBatch();
        if (transactional) {
            conn.commit();
        }
    }

    private void executeDdl(Connection conn, String sql, ImportListener l, boolean ignoreErrors) throws SQLException {
        l.log(sql);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException ex) {
            if (!ignoreErrors) {
                throw ex;
            }
            if (!conn.getAutoCommit()) {
                rollbackQuietly(conn, l);
            }
            l.log("Ignored: " + ex.getMessage());
        }
    }

    private static void rollbackQuietly(Connection conn, ImportListener l) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            l.log("Rollback failed: " + ex.getMessage());
        }
    }
}
