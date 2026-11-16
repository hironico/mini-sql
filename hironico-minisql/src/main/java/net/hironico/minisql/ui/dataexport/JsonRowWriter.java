package net.hironico.minisql.ui.dataexport;

import java.io.IOException;
import java.io.Writer;

/**
 * Streams rows as JSON, one object per row. Two layouts are supported:
 * a JSON array of objects, or JSON Lines (one object per line, no enclosing array) which is
 * the better choice for very large exports. Numbers and booleans keep their JSON type,
 * dates are strings and NULL is written as null.
 */
public class JsonRowWriter implements RowWriter {

    private final Writer out;
    private final String rowSeparator;
    private final boolean jsonLines;
    private String[] names;
    private boolean first = true;

    public JsonRowWriter(Writer out, String rowSeparator, boolean jsonLines) {
        this.out = out;
        this.rowSeparator = rowSeparator;
        this.jsonLines = jsonLines;
    }

    @Override
    public void start(String[] columnNames) throws IOException {
        this.names = columnNames.clone();
        if (!jsonLines) {
            out.write('[');
        }
    }

    @Override
    public void writeRow(String[] values, ValueKind[] kinds) throws IOException {
        if (jsonLines) {
            if (!first) {
                out.write(rowSeparator);
            }
        } else {
            if (!first) {
                out.write(',');
            }
            out.write(rowSeparator);
        }
        first = false;

        out.write('{');
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            writeString(names[i]);
            out.write(':');
            writeValue(values[i], kinds[i]);
        }
        out.write('}');
    }

    @Override
    public void finish() throws IOException {
        if (jsonLines) {
            if (!first) {
                out.write(rowSeparator);
            }
        } else {
            out.write(rowSeparator);
            out.write(']');
            out.write(rowSeparator);
        }
        out.flush();
    }

    private void writeValue(String value, ValueKind kind) throws IOException {
        switch (kind) {
            case NULL -> out.write("null");
            case BOOLEAN -> out.write(value);
            case NUMBER -> {
                // NaN and Infinity are not valid JSON numbers
                if (isJsonNumber(value)) {
                    out.write(value);
                } else {
                    writeString(value);
                }
            }
            default -> writeString(value);
        }
    }

    private static boolean isJsonNumber(String value) {
        if (value.isEmpty()) {
            return false;
        }
        char first = value.charAt(0);
        return Character.isDigit(first) || ((first == '-') && value.length() > 1 && Character.isDigit(value.charAt(1)));
    }

    private void writeString(String value) throws IOException {
        out.write('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.write("\\\"");
                case '\\' -> out.write("\\\\");
                case '\n' -> out.write("\\n");
                case '\r' -> out.write("\\r");
                case '\t' -> out.write("\\t");
                case '\b' -> out.write("\\b");
                case '\f' -> out.write("\\f");
                default -> {
                    if (c < 0x20) {
                        out.write(String.format("\\u%04x", (int) c));
                    } else {
                        out.write(c);
                    }
                }
            }
        }
        out.write('"');
    }
}
