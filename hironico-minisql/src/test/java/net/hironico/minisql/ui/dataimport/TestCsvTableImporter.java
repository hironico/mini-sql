package net.hironico.minisql.ui.dataimport;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestCsvTableImporter {

    private static List<ImportColumn> columns(String... names) {
        List<ImportColumn> cols = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            cols.add(new ImportColumn(i, names[i], names[i]));
        }
        return cols;
    }

    @Test
    public void testSanitizeIdentifier() {
        Set<String> used = new HashSet<>();
        assertEquals("First_name", CsvTableImporter.sanitizeIdentifier(" First name ", used));
        assertEquals("Prenom_ete", CsvTableImporter.sanitizeIdentifier("Prénom (été)", used));
        assertEquals("C_2024_total", CsvTableImporter.sanitizeIdentifier("2024 total", used));
        assertEquals("COLUMN", CsvTableImporter.sanitizeIdentifier("", used));
        assertEquals("COLUMN_2", CsvTableImporter.sanitizeIdentifier(null, used));
        assertEquals("first_name_2", CsvTableImporter.sanitizeIdentifier("first-name", used));
    }

    @Test
    public void testCreateTableSqlUsesDefaultTypeAndSkipsExcludedColumns() {
        List<ImportColumn> cols = columns("ID", "NAME", "IGNORED", "BIRTH");
        cols.get(0).setSqlType("INTEGER");
        cols.get(0).setNullable(false);
        cols.get(2).setIncluded(false);
        cols.get(3).setSqlType("DATE");

        CsvTableImporter importer = new CsvTableImporter(null, cols, "", "PEOPLE", false);

        assertEquals("CREATE TABLE PEOPLE (\n"
                + "    ID INTEGER NOT NULL,\n"
                + "    NAME VARCHAR(150),\n"
                + "    BIRTH DATE\n"
                + ")", importer.buildCreateTableSql());
        assertEquals("INSERT INTO PEOPLE (ID, NAME, BIRTH) VALUES (?, ?, ?)", importer.buildInsertSql());
        assertEquals("DROP TABLE PEOPLE", importer.buildDropTableSql());
    }

    @Test
    public void testQuotedIdentifiersAndSchema() {
        CsvTableImporter importer = new CsvTableImporter(null, columns("id", "my\"col"), "app", "data", true);

        assertEquals("\"app\".\"data\"", importer.getQualifiedTableName());
        assertEquals("INSERT INTO \"app\".\"data\" (\"id\", \"my\"\"col\") VALUES (?, ?)", importer.buildInsertSql());
    }

    @Test
    public void testValueKind() {
        assertEquals(CsvTableImporter.ValueKind.STRING, CsvTableImporter.getValueKind("VARCHAR(150)"));
        assertEquals(CsvTableImporter.ValueKind.STRING, CsvTableImporter.getValueKind("varchar2(400 char)"));
        assertEquals(CsvTableImporter.ValueKind.INTEGER, CsvTableImporter.getValueKind("integer"));
        assertEquals(CsvTableImporter.ValueKind.BIGINT, CsvTableImporter.getValueKind("BIGINT"));
        assertEquals(CsvTableImporter.ValueKind.DECIMAL, CsvTableImporter.getValueKind("NUMERIC(18, 2)"));
        assertEquals(CsvTableImporter.ValueKind.DECIMAL, CsvTableImporter.getValueKind("NUMBER(10)"));
        assertEquals(CsvTableImporter.ValueKind.DATE, CsvTableImporter.getValueKind("DATE"));
        assertEquals(CsvTableImporter.ValueKind.TIME, CsvTableImporter.getValueKind("TIME"));
        assertEquals(CsvTableImporter.ValueKind.TIMESTAMP, CsvTableImporter.getValueKind("TIMESTAMP(6)"));
        assertEquals(CsvTableImporter.ValueKind.TIMESTAMP, CsvTableImporter.getValueKind("TIMESTAMP WITH TIME ZONE"));
        assertEquals(CsvTableImporter.ValueKind.TIMESTAMP, CsvTableImporter.getValueKind("DATETIME2"));
        assertEquals(CsvTableImporter.ValueKind.BOOLEAN, CsvTableImporter.getValueKind("BOOLEAN"));
    }

    @Test
    public void testConvertValue() {
        CsvTableImporter importer = new CsvTableImporter(null, columns("a"), null, "T", false);
        importer.setDatePattern("dd/MM/yyyy");

        assertNull(importer.convertValue("", CsvTableImporter.ValueKind.INTEGER));
        assertNull(importer.convertValue("  ", CsvTableImporter.ValueKind.DECIMAL));
        assertEquals(" keep spaces ", importer.convertValue(" keep spaces ", CsvTableImporter.ValueKind.STRING));
        assertEquals(42, importer.convertValue(" 42 ", CsvTableImporter.ValueKind.INTEGER));
        assertEquals(new BigDecimal("1234.56"), importer.convertValue("1 234,56", CsvTableImporter.ValueKind.DECIMAL));
        assertEquals(Date.valueOf("2026-01-31"), importer.convertValue("31/01/2026", CsvTableImporter.ValueKind.DATE));
        assertEquals(Timestamp.valueOf("2026-01-31 13:45:10"),
                importer.convertValue("2026-01-31 13:45:10", CsvTableImporter.ValueKind.TIMESTAMP));
        assertEquals(Boolean.TRUE, importer.convertValue("Yes", CsvTableImporter.ValueKind.BOOLEAN));
        assertEquals(Boolean.FALSE, importer.convertValue("0", CsvTableImporter.ValueKind.BOOLEAN));

        for (String bad : Arrays.asList("abc", "1.5")) {
            try {
                importer.convertValue(bad, CsvTableImporter.ValueKind.INTEGER);
                fail("Conversion of " + bad + " must fail");
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
    }
}
