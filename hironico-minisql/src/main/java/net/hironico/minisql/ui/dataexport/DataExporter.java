package net.hironico.minisql.ui.dataexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Streams the rows of a table or a view from the database directly to a file, without keeping
 * them in memory: each row read from the JDBC cursor is formatted and written at once to a buffered writer.
 * Supported output formats are CSV, JSON and SQL INSERT statements, see {@link ExportFormat}.
 * <p>
 * This class has no dependency on Swing so it can be used and tested on its own.
 */
public class DataExporter {

    private static final Logger LOGGER = Logger.getLogger(DataExporter.class.getName());

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    public static final String CRLF = "\r\n";

    /** Number of rows between two progress notifications */
    private static final int PROGRESS_EVERY = 1000;

    /** Size in chars of the output buffer */
    private static final int BUFFER_SIZE = 1 << 16;

    /**
     * Receives the progress of a running export. Called from the exporting thread.
     */
    public interface ExportListener {
        /** @param rowsExported number of rows written so far */
        void progress(long rowsExported);

        /** @param message information message for the user */
        void log(String message);
    }

    /**
     * Outcome of an export.
     *
     * @param rowsExported number of rows written
     * @param cancelled    true if the export was stopped before the end, in which case the file is deleted
     * @param elapsedNanos duration of the export, from the connection preparation to the last row written
     * @param bytesWritten size of the output written to the file
     */
    public record ExportResult(long rowsExported, boolean cancelled, long elapsedNanos, long bytesWritten) {

        /** @return the average number of rows written per second */
        public double rowsPerSecond() {
            return elapsedNanos <= 0 ? 0d : rowsExported * 1_000_000_000d / elapsedNanos;
        }

        /** @return the duration in seconds */
        public double elapsedSeconds() {
            return elapsedNanos / 1_000_000_000d;
        }
    }

    /**
     * Output stream counting the bytes written to the wrapped stream.
     */
    private static class CountingOutputStream extends java.io.OutputStream {
        private final java.io.OutputStream delegate;
        private long count = 0;

        CountingOutputStream(java.io.OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            count += len;
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        long getCount() {
            return count;
        }
    }

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** Interval between two throughput lines in the log */
    private static final long RATE_LOG_INTERVAL_NANOS = 2_000_000_000L;

    /**
     * @param message a log message
     * @return the message prefixed by the current wall clock time, like <code>[14:32:05.123] message</code>
     */
    public static String stamp(String message) {
        return "[" + CLOCK_FORMAT.format(java.time.LocalTime.now()) + "] " + message;
    }

    private static void log(ExportListener listener, String message) {
        if (listener != null) {
            listener.log(stamp(message));
        }
    }

    /**
     * @param bytes a size in bytes
     * @return the size in a human readable unit
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format(java.util.Locale.ROOT, "%.1f %s", value, units[unit]);
    }

    /**
     * @param prefix  start of the summary, like "Export finished"
     * @param result  the outcome of the export
     * @return a one line summary with the rows, the duration, the throughput and the size of the file
     */
    public static String summarize(String prefix, ExportResult result) {
        return String.format(java.util.Locale.ROOT, "%s: %,d rows in %.1f s (%,.0f rows/s), %s written",
                prefix, result.rowsExported(), result.elapsedSeconds(), result.rowsPerSecond(),
                formatSize(result.bytesWritten()));
    }

    private final String schema;
    private final String objectName;
    private final List<String> columnNames;
    private final boolean quotedIdentifiers;
    private ExportFormat format = ExportFormat.CSV;
    private Charset charset = StandardCharsets.UTF_8;
    private String rowSeparator = CRLF;
    private char delimiter = ',';
    private boolean header = true;
    private boolean quoteAll = true;
    private NullHandling nullHandling = NullHandling.EMPTY;
    private boolean utf8Bom = false;
    private boolean jsonLines = false;
    private String targetTable = null;
    private int rowsPerStatement = 1;
    private int fetchSize = 5000;
    private String datePattern = DEFAULT_DATE_PATTERN;
    private String timestampPattern = DEFAULT_TIMESTAMP_PATTERN;
    private String timePattern = DEFAULT_TIME_PATTERN;

    private volatile boolean cancelRequested = false;

    /**
     * @param schema            schema of the table or view, can be null or blank
     * @param objectName        name of the table or view
     * @param columnNames       names of the columns to export, in the output order
     * @param quotedIdentifiers true to quote the schema, object and column names in the generated SELECT
     */
    public DataExporter(String schema, String objectName, List<String> columnNames, boolean quotedIdentifiers) {
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("At least one column must be selected.");
        }
        this.schema = schema;
        this.objectName = objectName;
        this.columnNames = List.copyOf(columnNames);
        this.quotedIdentifiers = quotedIdentifiers;
    }

    public void setFormat(ExportFormat format) {
        this.format = format;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /** @param rowSeparator text written at the end of each row, CRLF by default */
    public void setRowSeparator(String rowSeparator) {
        this.rowSeparator = rowSeparator;
    }

    /** @param delimiter CSV field delimiter */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /** @param header CSV only: write the column names in the first row */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /** @param quoteAll CSV only: enclose every value in double quotes, true by default */
    public void setQuoteAll(boolean quoteAll) {
        this.quoteAll = quoteAll;
    }

    /** @param nullHandling CSV only: what to write for NULL values, empty by default */
    public void setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling;
    }

    /**
     * @param utf8Bom true to start the file with a UTF-8 byte order mark, which lets Excel detect the encoding.
     *                Only applied when the charset is UTF-8.
     */
    public void setUtf8Bom(boolean utf8Bom) {
        this.utf8Bom = utf8Bom;
    }

    /** @param jsonLines JSON only: write one object per line instead of a JSON array */
    public void setJsonLines(boolean jsonLines) {
        this.jsonLines = jsonLines;
    }

    /** @param targetTable SQL only: table name used in the INSERT statements, the source table if blank */
    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    /** @param rowsPerStatement SQL only: number of rows per INSERT statement */
    public void setRowsPerStatement(int rowsPerStatement) {
        this.rowsPerStatement = Math.max(1, rowsPerStatement);
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = Math.max(1, fetchSize);
    }

    /**
     * Sets the patterns used to format the dates, the timestamps and the times.
     *
     * @throws IllegalArgumentException if a pattern is not a valid {@link DateTimeFormatter} pattern
     */
    public void setDatePatterns(String datePattern, String timestampPattern, String timePattern) {
        DateTimeFormatter.ofPattern(datePattern);
        DateTimeFormatter.ofPattern(timestampPattern);
        DateTimeFormatter.ofPattern(timePattern);
        this.datePattern = datePattern;
        this.timestampPattern = timestampPattern;
        this.timePattern = timePattern;
    }

    /** Asks the running export to stop at the next row. Safe to call from any thread. */
    public void cancel() {
        cancelRequested = true;
    }

    /**
     * Quotes the identifier with double quotes if quoted identifiers are enabled.
     */
    public String quote(String identifier) {
        if (!quotedIdentifiers) {
            return identifier;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /** @return the table or view name prefixed by the schema if any */
    public String getQualifiedName() {
        if (schema == null || schema.isBlank()) {
            return quote(objectName.trim());
        }
        return quote(schema.trim()) + "." + quote(objectName.trim());
    }

    /** @return the SELECT statement reading the selected columns */
    public String buildSelectSql() {
        String cols = columnNames.stream().map(this::quote).collect(Collectors.joining(", "));
        return "SELECT " + cols + " FROM " + getQualifiedName();
    }

    /**
     * Reads the table or view and writes it to the file. The file is deleted if the export fails or is cancelled.
     *
     * @param conn     open connection, not closed by this method
     * @param file     target file, overwritten if it exists
     * @param listener progress listener, can be null
     * @return the number of rows written and the cancellation state
     * @throws SQLException on database errors
     * @throws IOException  on file errors
     */
    public ExportResult runExport(Connection conn, File file, ExportListener listener) throws SQLException, IOException {
        cancelRequested = false;

        boolean previousAutoCommit = conn.getAutoCommit();
        boolean autoCommitChanged = false;
        boolean success = false;
        try {
            // PostgreSQL only streams with a cursor when autocommit is off, otherwise it loads the whole result.
            if (previousAutoCommit && isPostgres(conn)) {
                conn.setAutoCommit(false);
                autoCommitChanged = true;
            }

            ExportResult result = doExport(conn, file, listener);
            success = !result.cancelled();
            return result;
        } finally {
            if (autoCommitChanged) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ex) {
                    LOGGER.warning("Cannot restore the connection autocommit mode: " + ex.getMessage());
                }
            }
            if (!success && file.exists() && !file.delete()) {
                LOGGER.warning("Cannot delete the incomplete export file " + file.getAbsolutePath());
            }
        }
    }

    private ExportResult doExport(Connection conn, File file, ExportListener listener) throws SQLException, IOException {
        String sql = buildSelectSql();
        final long startNanos = System.nanoTime();
        log(listener, "Export started: " + sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            stmt.setFetchSize(fetchSize);

            CountingOutputStream counter = new CountingOutputStream(new FileOutputStream(file));
            if (utf8Bom && StandardCharsets.UTF_8.equals(charset)) {
                counter.write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            }
            try (ResultSet rs = stmt.executeQuery();
                 Writer out = new BufferedWriter(new OutputStreamWriter(counter, charset), BUFFER_SIZE)) {

                log(listener, String.format(java.util.Locale.ROOT, "Query executed in %.2f s, writing rows to %s",
                        (System.nanoTime() - startNanos) / 1_000_000_000d, file.getAbsolutePath()));

                ResultSetMetaData md = rs.getMetaData();
                int count = md.getColumnCount();
                int[] types = new int[count];
                for (int i = 0; i < count; i++) {
                    types[i] = md.getColumnType(i + 1);
                }

                DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(datePattern);
                DateTimeFormatter timestampFmt = DateTimeFormatter.ofPattern(timestampPattern);
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(timePattern);

                RowWriter writer = createRowWriter(out);
                writer.start(columnNames.toArray(new String[0]));

                String[] values = new String[count];
                ValueKind[] kinds = new ValueKind[count];
                long rows = 0;
                long lastLogNanos = System.nanoTime();
                long lastLogRows = 0;
                boolean firstRowLogged = false;

                while (rs.next()) {
                    if (!firstRowLogged) {
                        firstRowLogged = true;
                        log(listener, String.format(java.util.Locale.ROOT, "First row received after %.2f s",
                                (System.nanoTime() - startNanos) / 1_000_000_000d));
                        lastLogNanos = System.nanoTime();
                    }

                    if (cancelRequested) {
                        out.flush();
                        ExportResult cancelled = new ExportResult(rows, true, System.nanoTime() - startNanos, counter.getCount());
                        if (listener != null) {
                            listener.progress(rows);
                        }
                        log(listener, summarize("Export cancelled", cancelled));
                        return cancelled;
                    }

                    for (int i = 0; i < count; i++) {
                        readValue(rs, i + 1, types[i], values, kinds, i, dateFmt, timestampFmt, timeFmt);
                    }
                    writer.writeRow(values, kinds);
                    rows++;

                    if (listener != null && rows % PROGRESS_EVERY == 0) {
                        listener.progress(rows);

                        // the clock is only read once per progress step to keep the per row cost minimal
                        long now = System.nanoTime();
                        if (now - lastLogNanos >= RATE_LOG_INTERVAL_NANOS) {
                            double windowSeconds = (now - lastLogNanos) / 1_000_000_000d;
                            double overallSeconds = (now - startNanos) / 1_000_000_000d;
                            log(listener, String.format(java.util.Locale.ROOT, "%,d rows written (%,.0f rows/s now, %,.0f rows/s average)",
                                    rows, (rows - lastLogRows) / windowSeconds, rows / overallSeconds));
                            lastLogNanos = now;
                            lastLogRows = rows;
                        }
                    }
                }

                writer.finish();
                ExportResult result = new ExportResult(rows, false, System.nanoTime() - startNanos, counter.getCount());
                if (listener != null) {
                    listener.progress(rows);
                }
                log(listener, summarize("Export finished", result));
                return result;
            }
        }
    }

    private RowWriter createRowWriter(Writer out) {
        return switch (format) {
            case CSV -> new CsvRowWriter(out, delimiter, rowSeparator, header, quoteAll, nullHandling);
            case JSON -> new JsonRowWriter(out, rowSeparator, jsonLines);
            case SQL -> new SqlInsertRowWriter(out, rowSeparator,
                    targetTable == null || targetTable.isBlank() ? getQualifiedName() : targetTable.trim(),
                    rowsPerStatement, this::quote);
        };
    }

    /**
     * Reads one cell of the current row and stores its text and kind at the given position.
     */
    private static void readValue(ResultSet rs, int col, int sqlType, String[] values, ValueKind[] kinds, int pos,
                                  DateTimeFormatter dateFmt, DateTimeFormatter timestampFmt, DateTimeFormatter timeFmt)
            throws SQLException {
        switch (sqlType) {
            case Types.DATE -> {
                java.sql.Date d = rs.getDate(col);
                set(values, kinds, pos, d == null ? null : dateFmt.format(d.toLocalDate()), ValueKind.TEMPORAL);
            }
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> {
                Time t = rs.getTime(col);
                set(values, kinds, pos, t == null ? null : timeFmt.format(t.toLocalTime()), ValueKind.TEMPORAL);
            }
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                Timestamp ts = rs.getTimestamp(col);
                set(values, kinds, pos, ts == null ? null : timestampFmt.format(ts.toLocalDateTime()), ValueKind.TEMPORAL);
            }
            case Types.BIT, Types.BOOLEAN -> {
                boolean b = rs.getBoolean(col);
                set(values, kinds, pos, rs.wasNull() ? null : String.valueOf(b), ValueKind.BOOLEAN);
            }
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.REAL, Types.FLOAT, Types.DOUBLE ->
                    set(values, kinds, pos, rs.getString(col), ValueKind.NUMBER);
            case Types.NUMERIC, Types.DECIMAL -> {
                BigDecimal bd = rs.getBigDecimal(col);
                set(values, kinds, pos, bd == null ? null : bd.toPlainString(), ValueKind.NUMBER);
            }
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> {
                byte[] bytes = rs.getBytes(col);
                set(values, kinds, pos, bytes == null ? null : toHex(bytes), ValueKind.TEXT);
            }
            default -> set(values, kinds, pos, rs.getString(col), ValueKind.TEXT);
        }
    }

    private static void set(String[] values, ValueKind[] kinds, int pos, String value, ValueKind kind) {
        values[pos] = value;
        kinds[pos] = value == null ? ValueKind.NULL : kind;
    }

    private static String toHex(byte[] bytes) {
        char[] digits = "0123456789ABCDEF".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = digits[(bytes[i] >> 4) & 0xF];
            out[i * 2 + 1] = digits[bytes[i] & 0xF];
        }
        return new String(out);
    }

    private static boolean isPostgres(Connection conn) {
        try {
            return conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
        } catch (SQLException ex) {
            return false;
        }
    }
}
