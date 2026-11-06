package net.hironico.minisql.ui.dataimport;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestCsvFileReader {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File write(String content, Charset charset) throws IOException {
        File file = tmp.newFile();
        Files.write(file.toPath(), content.getBytes(charset));
        return file;
    }

    @Test
    public void testPreviewWithHeaderAndQuotes() throws IOException {
        String csv = "id,name,comment\n"
                + "1,\"Doe, John\",\"He said \"\"hello\"\"\"\n"
                + "2,Smith,\"multi\nline\"\n"
                + "3,,\n";
        CsvFileReader reader = new CsvFileReader(write(csv, StandardCharsets.UTF_8), StandardCharsets.UTF_8, ',', true);

        CsvFileReader.Preview preview = reader.readPreview(100);

        assertEquals(Arrays.asList("id", "name", "comment"), preview.headers());
        assertEquals(3, preview.rows().size());
        assertEquals(Arrays.asList("1", "Doe, John", "He said \"hello\""), preview.rows().get(0));
        assertEquals("multi\nline", preview.rows().get(1).get(2));
        assertEquals(Arrays.asList("3", "", ""), preview.rows().get(2));
        assertEquals(3L, reader.countRecords());
    }

    @Test
    public void testPreviewWithoutHeaderGeneratesColumnNames() throws IOException {
        String csv = "a;b\nc;d;e\n";
        CsvFileReader reader = new CsvFileReader(write(csv, StandardCharsets.UTF_8), StandardCharsets.UTF_8, ';', false);

        CsvFileReader.Preview preview = reader.readPreview(100);

        assertEquals(Arrays.asList("COLUMN_1", "COLUMN_2", "COLUMN_3"), preview.headers());
        assertEquals(2, preview.rows().size());
        assertEquals(2L, reader.countRecords());
    }

    @Test
    public void testPreviewLimitAndBomAndEmptyLines() throws IOException {
        StringBuilder csv = new StringBuilder("\uFEFFcode\n");
        for (int i = 0; i < 50; i++) {
            csv.append(i).append("\n\n");
        }
        CsvFileReader reader = new CsvFileReader(write(csv.toString(), StandardCharsets.UTF_8), StandardCharsets.UTF_8, ',', true);

        CsvFileReader.Preview preview = reader.readPreview(10);

        assertEquals("BOM must be removed from the first header", "code", preview.headers().get(0));
        assertEquals(10, preview.rows().size());
        assertEquals(50L, reader.countRecords());
    }

    @Test
    public void testTabDelimiterAndEncoding() throws IOException {
        String csv = "ville\tpays\nMontréal\tCanada\n";
        Charset latin1 = StandardCharsets.ISO_8859_1;
        CsvFileReader reader = new CsvFileReader(write(csv, latin1), latin1, CsvFileReader.parseDelimiter("Tab"), true);

        CsvFileReader.Preview preview = reader.readPreview(100);

        assertEquals(Arrays.asList("ville", "pays"), preview.headers());
        assertEquals(Arrays.asList("Montréal", "Canada"), preview.rows().get(0));
    }

    @Test
    public void testParseDelimiter() {
        assertEquals(',', CsvFileReader.parseDelimiter(null));
        assertEquals(',', CsvFileReader.parseDelimiter(""));
        assertEquals(';', CsvFileReader.parseDelimiter(";"));
        assertEquals('\t', CsvFileReader.parseDelimiter("Tab"));
        assertEquals('\t', CsvFileReader.parseDelimiter("\\t"));
        assertEquals('|', CsvFileReader.parseDelimiter("|"));
    }
}
