package net.hironico.minisql.ui.dataimport;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * End to end tests of the CSV import using an in memory H2 database.
 */
public class TestCsvTableImporterH2 {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:csvimport_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
    }

    private CsvFileReader writeCsv(int rows, Integer badValueAtRow) throws Exception {
        StringBuilder csv = new StringBuilder("id,label,amount,created\n");
        for (int i = 1; i <= rows; i++) {
            String id = (badValueAtRow != null && i == badValueAtRow) ? "oops" : String.valueOf(i);
            csv.append(id).append(",\"label ").append(i).append(", quoted\",")
                    .append(i).append(".50,2026-01-").append(String.format("%02d", (i % 28) + 1)).append('\n');
        }
        File file = tmp.newFile();
        Files.writeString(file.toPath(), csv.toString(), StandardCharsets.UTF_8);
        return new CsvFileReader(file, StandardCharsets.UTF_8, ',', true);
    }

    private List<ImportColumn> columns() {
        List<ImportColumn> cols = new ArrayList<>();
        cols.add(new ImportColumn(0, "id", "ID"));
        cols.add(new ImportColumn(1, "label", "LABEL"));
        cols.add(new ImportColumn(2, "amount", "AMOUNT"));
        cols.add(new ImportColumn(3, "created", "CREATED"));
        cols.get(0).setSqlType("INTEGER");
        cols.get(2).setSqlType("NUMERIC(18,2)");
        cols.get(3).setSqlType("DATE");
        return cols;
    }

    private long count(String table) throws Exception {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static CsvTableImporter.ImportListener onProgress(java.util.function.LongConsumer batches) {
        return new CsvTableImporter.ImportListener() {
            public void progress(long rowsImported, long batchesCommitted) { batches.accept(batchesCommitted); }
            public void log(String message) { }
        };
    }

    @Test
    public void testImportByBatches() throws Exception {
        CsvTableImporter importer = new CsvTableImporter(writeCsv(250, null), columns(), null, "IMPORTED", false);
        AtomicLong progressCalls = new AtomicLong();

        CsvTableImporter.ImportResult result = importer.runImport(conn, onProgress(b -> progressCalls.incrementAndGet()));

        assertEquals(250, result.rowsImported());
        assertEquals("250 rows with default batch size 100 means 3 batches", 3, result.batchesCommitted());
        assertEquals(3, progressCalls.get());
        assertFalse(result.cancelled());
        assertEquals(250, count("IMPORTED"));
        assertTrue("auto commit mode must be restored", conn.getAutoCommit());

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LABEL, AMOUNT, CREATED FROM IMPORTED WHERE ID = 42")) {
            assertTrue(rs.next());
            assertEquals("label 42, quoted", rs.getString(1));
            assertEquals(new BigDecimal("42.50"), rs.getBigDecimal(2));
            assertEquals(java.sql.Date.valueOf("2026-01-15"), rs.getDate(3));
        }
    }

    @Test
    public void testDropAndRecreateWithExcludedColumn() throws Exception {
        new CsvTableImporter(writeCsv(10, null), columns(), null, "AGAIN", false).runImport(conn, null);

        List<ImportColumn> cols = columns();
        cols.get(3).setIncluded(false);
        CsvTableImporter importer = new CsvTableImporter(writeCsv(5, null), cols, null, "AGAIN", false);
        importer.setDropTableFirst(true);
        importer.setBatchSize(2);

        CsvTableImporter.ImportResult result = importer.runImport(conn, null);

        assertEquals(5, result.rowsImported());
        assertEquals(3, result.batchesCommitted());
        assertEquals(5, count("AGAIN"));
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'AGAIN'")) {
            rs.next();
            assertEquals(3, rs.getInt(1));
        }
    }

    @Test
    public void testConversionErrorKeepsCommittedBatches() throws Exception {
        CsvTableImporter importer = new CsvTableImporter(writeCsv(30, 25), columns(), null, "PARTIAL", false);
        importer.setBatchSize(10);

        try {
            importer.runImport(conn, null);
            fail("Import must fail on the invalid integer value");
        } catch (IllegalArgumentException ex) {
            // record 1 is the header, so data row 25 is CSV record 26
            assertTrue(ex.getMessage(), ex.getMessage().contains("CSV record 26"));
            assertTrue(ex.getMessage(), ex.getMessage().contains("ID"));
        }

        assertEquals("the 2 first batches are committed, the failing one is rolled back", 20, count("PARTIAL"));
    }

    @Test
    public void testCancelStopsAfterCurrentBatch() throws Exception {
        CsvTableImporter importer = new CsvTableImporter(writeCsv(100, null), columns(), null, "CANCELLED", false);
        importer.setBatchSize(10);

        CsvTableImporter.ImportResult result = importer.runImport(conn, onProgress(b -> {
            if (b == 2) {
                importer.cancel();
            }
        }));

        assertTrue(result.cancelled());
        assertEquals(20, result.rowsImported());
        assertEquals(20, count("CANCELLED"));
    }
}
