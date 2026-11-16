package net.hironico.minisql.ui.dataexport;

import net.hironico.minisql.ui.dataimport.CsvFileReader;
import net.hironico.minisql.ui.dataimport.CsvTableImporter;
import net.hironico.minisql.ui.dataimport.ImportColumn;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * End to end tests of the streaming data export using an in memory H2 database.
 */
public class TestDataExporterH2 {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:dataexport_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE PEOPLE (ID INTEGER, LABEL VARCHAR(100), AMOUNT NUMERIC(18,2), "
                    + "BORN DATE, CREATED TIMESTAMP, OK BOOLEAN)");
            stmt.execute("INSERT INTO PEOPLE VALUES (1, 'plain', 10.50, DATE '2026-01-31', TIMESTAMP '2026-01-31 13:45:10', TRUE)");
            stmt.execute("INSERT INTO PEOPLE VALUES (2, 'with, comma and \"quote\"', 0.00, NULL, NULL, FALSE)");
            stmt.execute("INSERT INTO PEOPLE VALUES (3, 'it''s', -3.25, DATE '2000-02-29', TIMESTAMP '2000-02-29 00:00:01', NULL)");
        }
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
    }

    private String export(DataExporter exporter) throws Exception {
        File file = tmp.newFile();
        DataExporter.ExportResult result = exporter.runExport(conn, file, null);
        assertFalse(result.cancelled());
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    @Test
    public void csvSubsetOfColumnsWithCrlfAndQuoting() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("LABEL", "ID"), false);
        exporter.setQuoteAll(false);
        String csv = export(exporter);
        assertEquals("LABEL,ID\r\nplain,1\r\n\"with, comma and \"\"quote\"\"\",2\r\nit's,3\r\n", csv);
    }

    @Test
    public void csvDelimiterRowSeparatorAndNoHeader() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL"), false);
        exporter.setQuoteAll(false);
        exporter.setDelimiter(';');
        exporter.setRowSeparator("\n");
        exporter.setHeader(false);
        assertEquals("1;plain\n2;\"with, comma and \"\"quote\"\"\"\n3;it's\n", export(exporter));
    }

    @Test
    public void datePatternsApplyToDateAndTimestampColumns() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "BORN", "CREATED"), false);
        exporter.setQuoteAll(false);
        exporter.setDatePatterns("dd/MM/yyyy", "dd.MM.yyyy HH:mm", "HH:mm");
        exporter.setHeader(false);
        assertEquals("1,31/01/2026,31.01.2026 13:45\r\n2,,\r\n3,29/02/2000,29.02.2000 00:00\r\n", export(exporter));
    }

    @Test
    public void jsonArrayKeepsTypesAndEscapes() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL", "AMOUNT", "BORN", "OK"), false);
        exporter.setFormat(ExportFormat.JSON);
        String json = export(exporter);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("{\"ID\":1,\"LABEL\":\"plain\",\"AMOUNT\":10.50,\"BORN\":\"2026-01-31\",\"OK\":true}"));
        assertTrue(json.contains("\"LABEL\":\"with, comma and \\\"quote\\\"\",\"AMOUNT\":0.00,\"BORN\":null,\"OK\":false"));
        assertTrue(json.contains("\"AMOUNT\":-3.25,\"BORN\":\"2000-02-29\",\"OK\":null"));
        assertTrue(json.trim().endsWith("]"));
    }

    @Test
    public void quoteAllIsTheDefaultAndQuotesEveryValueAndHeader() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL", "AMOUNT", "BORN"), false);
        assertEquals("\"ID\",\"LABEL\",\"AMOUNT\",\"BORN\"\r\n"
                + "\"1\",\"plain\",\"10.50\",\"2026-01-31\"\r\n"
                + "\"2\",\"with, comma and \"\"quote\"\"\",\"0.00\",\r\n"
                + "\"3\",\"it's\",\"-3.25\",\"2000-02-29\"\r\n", export(exporter));
    }

    @Test
    public void nullsAreNeverQuotedAndDifferFromEmptyStrings() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE NULLS (A VARCHAR(10), B VARCHAR(10))");
            stmt.execute("INSERT INTO NULLS VALUES (NULL, '')");
        }
        DataExporter exporter = new DataExporter(null, "NULLS", List.of("A", "B"), false);
        exporter.setHeader(false);
        assertEquals(",\"\"\r\n", export(exporter));

        exporter.setNullHandling(NullHandling.NULL_LITERAL);
        assertEquals("NULL,\"\"\r\n", export(exporter));

        exporter.setQuoteAll(false);
        assertEquals("NULL,\r\n", export(exporter));
        exporter.setNullHandling(NullHandling.EMPTY);
        assertEquals(",\r\n", export(exporter));
    }

    @Test
    public void nullHandlingDoesNotChangeJsonAndSql() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "BORN"), false);
        exporter.setNullHandling(NullHandling.EMPTY);
        exporter.setFormat(ExportFormat.JSON);
        exporter.setJsonLines(true);
        assertTrue(export(exporter).contains("{\"ID\":2,\"BORN\":null}"));
        exporter.setFormat(ExportFormat.SQL);
        assertTrue(export(exporter).contains("VALUES (2, NULL);"));
    }

    @Test
    public void utf8BomIsWrittenOnlyForUtf8AndCountedInSize() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID"), false);
        exporter.setQuoteAll(false);
        exporter.setUtf8Bom(true);
        File file = tmp.newFile();
        DataExporter.ExportResult result = exporter.runExport(conn, file, null);
        byte[] bytes = Files.readAllBytes(file.toPath());
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
        assertEquals("ID", new String(bytes, 3, 2, StandardCharsets.UTF_8));
        assertEquals(bytes.length, result.bytesWritten());

        exporter.setCharset(StandardCharsets.ISO_8859_1);
        File latin = tmp.newFile();
        exporter.runExport(conn, latin, null);
        assertEquals('I', Files.readAllBytes(latin.toPath())[0]);
    }

    @Test
    public void quotedCsvCanBeImportedBackWithDelimiterInValues() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL"), false);
        exporter.setDelimiter(';');
        File file = tmp.newFile();
        exporter.runExport(conn, file, null);
        CsvFileReader reader = new CsvFileReader(file, StandardCharsets.UTF_8, ';', true);
        List<ImportColumn> cols = new ArrayList<>();
        cols.add(new ImportColumn(0, "ID", "ID"));
        cols.add(new ImportColumn(1, "LABEL", "LABEL"));
        cols.get(0).setSqlType("INTEGER");
        cols.get(1).setSqlType("VARCHAR(100)");
        new CsvTableImporter(reader, cols, null, "PEOPLE_Q", false).runImport(conn, null);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LABEL FROM PEOPLE_Q WHERE ID = 2")) {
            assertTrue(rs.next());
            assertEquals("with, comma and \"quote\"", rs.getString(1));
        }
    }

    @Test
    public void columnModelShowsSizeAndNullableFlag() {
        ExportColumnTableModel model = new ExportColumnTableModel();
        model.setColumns(List.of(
                new ExportColumn("ID", "INTEGER", java.sql.Types.INTEGER, 10, 0, false),
                new ExportColumn("AMOUNT", "NUMERIC", java.sql.Types.NUMERIC, 18, 2, true),
                new ExportColumn("BODY", "TEXT", java.sql.Types.VARCHAR, Integer.MAX_VALUE, 0, true),
                new ExportColumn("X", "OTHER", java.sql.Types.OTHER, 0, 0, true)));
        assertEquals(5, model.getColumnCount());
        assertEquals("Size", model.getColumnName(3));
        assertEquals("Nullable", model.getColumnName(4));
        assertEquals(Boolean.class, model.getColumnClass(4));
        assertEquals("10", model.getValueAt(0, 3));
        assertEquals(false, model.getValueAt(0, 4));
        assertEquals("18,2", model.getValueAt(1, 3));
        assertEquals("max", model.getValueAt(2, 3));
        assertEquals("", model.getValueAt(3, 3));
        assertEquals(true, model.getValueAt(3, 4));
        assertFalse(model.isCellEditable(0, 4));
    }

    @Test
    public void jsonLinesHasOneObjectPerLine() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID"), false);
        exporter.setFormat(ExportFormat.JSON);
        exporter.setJsonLines(true);
        assertEquals("{\"ID\":1}\r\n{\"ID\":2}\r\n{\"ID\":3}\r\n", export(exporter));
    }

    @Test
    public void sqlInsertsOneRowPerStatement() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL", "BORN", "OK"), false);
        exporter.setFormat(ExportFormat.SQL);
        exporter.setTargetTable("COPY_OF_PEOPLE");
        assertEquals("INSERT INTO COPY_OF_PEOPLE (ID, LABEL, BORN, OK) VALUES (1, 'plain', '2026-01-31', TRUE);\r\n"
                + "INSERT INTO COPY_OF_PEOPLE (ID, LABEL, BORN, OK) VALUES (2, 'with, comma and \"quote\"', NULL, FALSE);\r\n"
                + "INSERT INTO COPY_OF_PEOPLE (ID, LABEL, BORN, OK) VALUES (3, 'it''s', '2000-02-29', NULL);\r\n",
                export(exporter));
    }

    @Test
    public void sqlInsertsMultiRowAreExecutableOnH2() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE",
                List.of("ID", "LABEL", "AMOUNT", "BORN", "CREATED", "OK"), false);
        exporter.setFormat(ExportFormat.SQL);
        exporter.setTargetTable("PEOPLE_COPY");
        exporter.setRowsPerStatement(2);
        String sql = export(exporter);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE PEOPLE_COPY AS SELECT * FROM PEOPLE WHERE 1=0");
            for (String statement : sql.split(";\r\n")) {
                if (!statement.isBlank()) {
                    stmt.execute(statement);
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PEOPLE P JOIN PEOPLE_COPY C ON P.ID = C.ID "
                    + "AND P.LABEL = C.LABEL AND P.AMOUNT = C.AMOUNT")) {
                rs.next();
                assertEquals(3, rs.getInt(1));
            }
        }
    }

    @Test
    public void quotedIdentifiersAndSchemaInSelect() throws Exception {
        DataExporter exporter = new DataExporter("PUBLIC", "PEOPLE", List.of("ID", "LABEL"), true);
        assertEquals("SELECT \"ID\", \"LABEL\" FROM \"PUBLIC\".\"PEOPLE\"", exporter.buildSelectSql());
        exporter.setHeader(false);
        exporter.setQuoteAll(false);
        assertEquals("1,plain\r\n2,\"with, comma and \"\"quote\"\"\"\r\n3,it's\r\n", export(exporter));
    }

    @Test
    public void csvExportCanBeImportedBack() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL", "AMOUNT", "BORN"), false);
        File file = tmp.newFile();
        exporter.runExport(conn, file, null);

        CsvFileReader reader = new CsvFileReader(file, StandardCharsets.UTF_8, ',', true);
        List<ImportColumn> cols = new ArrayList<>();
        cols.add(new ImportColumn(0, "ID", "ID"));
        cols.add(new ImportColumn(1, "LABEL", "LABEL"));
        cols.add(new ImportColumn(2, "AMOUNT", "AMOUNT"));
        cols.add(new ImportColumn(3, "BORN", "BORN"));
        cols.get(0).setSqlType("INTEGER");
        cols.get(1).setSqlType("VARCHAR(100)");
        cols.get(2).setSqlType("NUMERIC(18,2)");
        cols.get(3).setSqlType("DATE");
        new CsvTableImporter(reader, cols, null, "PEOPLE_IMPORTED", false).runImport(conn, null);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LABEL FROM PEOPLE_IMPORTED WHERE ID = 2")) {
            assertTrue(rs.next());
            assertEquals("with, comma and \"quote\"", rs.getString(1));
        }
    }

    @Test
    public void largeTableIsStreamedToFile() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE BIG AS SELECT X AS ID, 'row ' || X AS LABEL FROM SYSTEM_RANGE(1, 200000)");
        }
        DataExporter exporter = new DataExporter(null, "BIG", List.of("ID", "LABEL"), false);
        exporter.setFetchSize(1000);
        File file = tmp.newFile();
        List<Long> progress = new ArrayList<>();
        DataExporter.ExportResult result = exporter.runExport(conn, file, new DataExporter.ExportListener() {
            public void progress(long rowsExported) { progress.add(rowsExported); }
            public void log(String message) { }
        });
        assertEquals(200000, result.rowsExported());
        assertEquals(200000L, (long) progress.get(progress.size() - 1));
        try (var lines = Files.lines(file.toPath())) {
            assertEquals(200001, lines.count());
        }
    }

    @Test
    public void logHasWallClockTimingsAndSummary() throws Exception {
        DataExporter exporter = new DataExporter(null, "PEOPLE", List.of("ID", "LABEL"), false);
        File file = tmp.newFile();
        List<String> logs = new ArrayList<>();
        DataExporter.ExportResult result = exporter.runExport(conn, file, new DataExporter.ExportListener() {
            public void progress(long rowsExported) { }
            public void log(String message) { logs.add(message); }
        });

        assertEquals(3, result.rowsExported());
        assertEquals(file.length(), result.bytesWritten());
        assertTrue(result.elapsedNanos() > 0);
        assertTrue(result.rowsPerSecond() > 0);

        assertTrue(logs.size() >= 4);
        for (String line : logs) {
            assertTrue(line, line.matches("^\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}] .+"));
        }
        assertTrue(logs.get(0).contains("Export started: SELECT ID, LABEL FROM PEOPLE"));
        assertTrue(logs.stream().anyMatch(l -> l.contains("First row received after")));
        String last = logs.get(logs.size() - 1);
        assertTrue(last, last.contains("Export finished: 3 rows in"));
        assertTrue(last, last.contains("rows/s"));
        assertTrue(last, last.contains(DataExporter.formatSize(file.length()) + " written"));
    }

    @Test
    public void formatSizeUsesHumanUnits() {
        assertEquals("512 B", DataExporter.formatSize(512));
        assertEquals("1.0 KB", DataExporter.formatSize(1024));
        assertEquals("1.5 MB", DataExporter.formatSize(1536 * 1024));
        assertEquals("2.0 GB", DataExporter.formatSize(2L * 1024 * 1024 * 1024));
    }

    @Test
    public void largeExportLogsThroughputLines() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE BIG3 AS SELECT X AS ID, RANDOM_UUID() AS U FROM SYSTEM_RANGE(1, 1500000)");
        }
        DataExporter exporter = new DataExporter(null, "BIG3", List.of("ID", "U"), false);
        File file = tmp.newFile();
        List<String> logs = new ArrayList<>();
        exporter.runExport(conn, file, new DataExporter.ExportListener() {
            public void progress(long rowsExported) { }
            public void log(String message) { logs.add(message); }
        });
        assertTrue(logs.toString(), logs.stream().anyMatch(l -> l.contains("rows/s now")) || logs.size() >= 4);
        assertTrue(logs.get(logs.size() - 1).contains("Export finished: 1,500,000 rows"));
    }

    @Test
    public void cancelDeletesPartialFile() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE BIG2 AS SELECT X AS ID FROM SYSTEM_RANGE(1, 100000)");
        }
        final DataExporter exporter = new DataExporter(null, "BIG2", List.of("ID"), false);
        File file = tmp.newFile();
        DataExporter.ExportResult result = exporter.runExport(conn, file, new DataExporter.ExportListener() {
            public void progress(long rowsExported) {
                if (rowsExported >= 1000) {
                    exporter.cancel();
                }
            }
            public void log(String message) { }
        });
        assertTrue(result.cancelled());
        assertTrue(result.rowsExported() < 100000);
        assertFalse(file.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDatePatternIsRejected() {
        new DataExporter(null, "PEOPLE", List.of("ID"), false).setDatePatterns("yyyy-MM-dd", "{", "HH");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noColumnIsRejected() {
        new DataExporter(null, "PEOPLE", List.of(), false);
    }
}
