package net.hironico.minisql.ui.dataexport;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes rows as delimited text following RFC 4180 quoting rules. Two quoting modes exist:
 * <ul>
 *     <li>quote all: every value, and every header name, is enclosed in double quotes, with inner
 *     quotes doubled. This keeps spreadsheets such as Excel from splitting values containing the delimiter;</li>
 *     <li>minimal: only fields containing the delimiter, a double quote or a line break are quoted.</li>
 * </ul>
 * NULL values are written bare, as an empty field or as the NULL literal, see {@link NullHandling}.
 * They are never quoted, so they stay different from an empty string, which is written as "" in quote all mode.
 */
public class CsvRowWriter implements RowWriter {

    private final Writer out;
    private final char delimiter;
    private final String rowSeparator;
    private final boolean header;
    private final boolean quoteAll;
    private final NullHandling nullHandling;

    /**
     * @param out          destination
     * @param delimiter    field delimiter
     * @param rowSeparator text written at the end of each row
     * @param header       true to write the column names in the first row
     * @param quoteAll     true to quote every value
     * @param nullHandling what to write for NULL values
     */
    public CsvRowWriter(Writer out, char delimiter, String rowSeparator, boolean header, boolean quoteAll,
                        NullHandling nullHandling) {
        this.out = out;
        this.delimiter = delimiter;
        this.rowSeparator = rowSeparator;
        this.header = header;
        this.quoteAll = quoteAll;
        this.nullHandling = nullHandling;
    }

    /** Creates a writer with minimal quoting and empty NULLs. */
    public CsvRowWriter(Writer out, char delimiter, String rowSeparator, boolean header) {
        this(out, delimiter, rowSeparator, header, false, NullHandling.EMPTY);
    }

    @Override
    public void start(String[] columnNames) throws IOException {
        if (header) {
            for (int i = 0; i < columnNames.length; i++) {
                if (i > 0) {
                    out.write(delimiter);
                }
                writeValue(columnNames[i]);
            }
            out.write(rowSeparator);
        }
    }

    @Override
    public void writeRow(String[] values, ValueKind[] kinds) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.write(delimiter);
            }
            if (kinds[i] == ValueKind.NULL || values[i] == null) {
                if (nullHandling == NullHandling.NULL_LITERAL) {
                    out.write("NULL");
                }
            } else {
                writeValue(values[i]);
            }
        }
        out.write(rowSeparator);
    }

    @Override
    public void finish() throws IOException {
        out.flush();
    }

    private void writeValue(String value) throws IOException {
        if (!quoteAll && (value.isEmpty() || !needsQuotes(value))) {
            out.write(value);
            return;
        }
        out.write('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                out.write('"');
            }
            out.write(c);
        }
        out.write('"');
    }

    private boolean needsQuotes(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == delimiter || c == '"' || c == '\n' || c == '\r') {
                return true;
            }
        }
        return false;
    }
}
