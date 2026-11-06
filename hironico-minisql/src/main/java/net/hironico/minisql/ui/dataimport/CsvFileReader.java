package net.hironico.minisql.ui.dataimport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Reads CSV files with the Apache Commons CSV library for the data import wizard.
 * The header row (if any) is read as a regular record so that duplicate or empty
 * column names do not prevent the file from being loaded.
 */
public class CsvFileReader {

    /** Unicode byte order mark that some editors put at the beginning of UTF files */
    private static final int BOM = '\uFEFF';

    /** The CSV file to read */
    private final File file;

    /** File encoding */
    private final Charset charset;

    /** Field delimiter */
    private final char delimiter;

    /** True if the first record holds the column names */
    private final boolean header;

    /**
     * Result of a preview read: the column names and the first records of the file.
     *
     * @param headers column names, read from the header row or generated as COLUMN_n
     * @param rows    first data records of the file
     */
    public record Preview(List<String> headers, List<List<String>> rows) {
    }

    /**
     * Creates a new CSV reader.
     *
     * @param file      the CSV file to read
     * @param charset   the encoding of the file
     * @param delimiter the field delimiter
     * @param header    true if the first record of the file holds the column names
     */
    public CsvFileReader(File file, Charset charset, char delimiter, boolean header) {
        this.file = file;
        this.charset = charset;
        this.delimiter = delimiter;
        this.header = header;
    }

    public File getFile() {
        return file;
    }

    public boolean hasHeader() {
        return header;
    }

    /**
     * Get the CSV format used to parse the file.
     *
     * @return CSVFormat configured with the delimiter of this reader
     */
    protected CSVFormat getFormat() {
        return CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setIgnoreEmptyLines(true)
                .setTrim(false)
                .build();
    }

    /**
     * Opens a parser on the file. The header row, if any, is NOT skipped: use {@link #records(CSVParser)}
     * to iterate over data records only. The caller is responsible for closing the parser.
     *
     * @return a new CSVParser over the file
     * @throws IOException if the file cannot be opened
     */
    public CSVParser open() throws IOException {
        PushbackReader reader = new PushbackReader(Files.newBufferedReader(file.toPath(), charset));
        int first = reader.read();
        if (first != -1 && first != BOM) {
            reader.unread(first);
        }
        return CSVParser.parse(reader, getFormat());
    }

    /**
     * Get an iterator over the data records of the parser, skipping the header row if needed.
     *
     * @param parser parser created with {@link #open()}
     * @return iterator over data records
     */
    public Iterator<CSVRecord> records(CSVParser parser) {
        Iterator<CSVRecord> it = parser.iterator();
        if (header && it.hasNext()) {
            it.next();
        }
        return it;
    }

    /**
     * Reads the column names and the first data records of the file.
     *
     * @param maxRows maximum number of data records to read
     * @return the preview of the file
     * @throws IOException if the file cannot be read or parsed
     */
    public Preview readPreview(int maxRows) throws IOException {
        try (CSVParser parser = open()) {
            Iterator<CSVRecord> it = parser.iterator();
            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            if (header && it.hasNext()) {
                headers.addAll(toList(it.next()));
            }

            int maxColumns = headers.size();
            while (it.hasNext() && rows.size() < maxRows) {
                List<String> row = toList(it.next());
                maxColumns = Math.max(maxColumns, row.size());
                rows.add(row);
            }

            // generate names for columns without header
            for (int i = headers.size(); i < maxColumns; i++) {
                headers.add("COLUMN_" + (i + 1));
            }

            return new Preview(Collections.unmodifiableList(headers), Collections.unmodifiableList(rows));
        }
    }

    /**
     * Counts the data records of the file (header row excluded).
     *
     * @return number of data records
     * @throws IOException if the file cannot be read or parsed
     */
    public long countRecords() throws IOException {
        try (CSVParser parser = open()) {
            Iterator<CSVRecord> it = records(parser);
            long count = 0;
            while (it.hasNext()) {
                it.next();
                count++;
            }
            return count;
        }
    }

    /**
     * Converts a CSV record into a list of values.
     *
     * @param csvRecord the record to convert
     * @return the values of the record
     */
    public static List<String> toList(CSVRecord csvRecord) {
        List<String> values = new ArrayList<>(csvRecord.size());
        csvRecord.forEach(values::add);
        return values;
    }

    /**
     * Converts the delimiter label displayed in the UI into a delimiter character.
     *
     * @param label delimiter label like ",", ";", "|" or "Tab"
     * @return the delimiter char, comma if the label is empty
     */
    public static char parseDelimiter(String label) {
        if (label == null || label.isEmpty()) {
            return ',';
        }
        if ("tab".equalsIgnoreCase(label.trim()) || "\\t".equals(label)) {
            return '\t';
        }
        return label.charAt(0);
    }
}
