package net.hironico.minisql.ui.dataexport;

import java.io.IOException;
import java.io.Writer;
import java.util.function.UnaryOperator;

/**
 * Writes rows as SQL INSERT statements. With one row per statement each row gives
 * <code>INSERT INTO t (a, b) VALUES (1, 'x');</code>, with more rows per statement the
 * VALUES clause is a list of tuples. Strings and dates are written as quoted literals
 * (quotes doubled), NULL as NULL, numbers and booleans unquoted.
 */
public class SqlInsertRowWriter implements RowWriter {

    private final Writer out;
    private final String rowSeparator;
    private final String targetTable;
    private final int rowsPerStatement;
    private final UnaryOperator<String> quoter;
    private String insertPrefix;
    private int rowsInStatement = 0;

    /**
     * @param targetTable      table name written in the statements, already quoted/qualified if needed
     * @param rowsPerStatement number of rows grouped in one INSERT statement, at least 1
     * @param quoter           quotes a column name, can be the identity function
     */
    public SqlInsertRowWriter(Writer out, String rowSeparator, String targetTable, int rowsPerStatement,
                              UnaryOperator<String> quoter) {
        this.out = out;
        this.rowSeparator = rowSeparator;
        this.targetTable = targetTable;
        this.rowsPerStatement = Math.max(1, rowsPerStatement);
        this.quoter = quoter;
    }

    @Override
    public void start(String[] columnNames) {
        StringBuilder sb = new StringBuilder("INSERT INTO ").append(targetTable).append(" (");
        for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(quoter.apply(columnNames[i]));
        }
        insertPrefix = sb.append(") VALUES").toString();
    }

    @Override
    public void writeRow(String[] values, ValueKind[] kinds) throws IOException {
        if (rowsInStatement == 0) {
            out.write(insertPrefix);
            out.write(rowsPerStatement == 1 ? " " : rowSeparator);
        } else {
            out.write(',');
            out.write(rowSeparator);
        }

        out.write('(');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.write(", ");
            }
            writeLiteral(values[i], kinds[i]);
        }
        out.write(')');

        rowsInStatement++;
        if (rowsInStatement >= rowsPerStatement) {
            endStatement();
        }
    }

    @Override
    public void finish() throws IOException {
        if (rowsInStatement > 0) {
            endStatement();
        }
        out.flush();
    }

    private void endStatement() throws IOException {
        out.write(';');
        out.write(rowSeparator);
        rowsInStatement = 0;
    }

    private void writeLiteral(String value, ValueKind kind) throws IOException {
        switch (kind) {
            case NULL -> out.write("NULL");
            case BOOLEAN -> out.write(value.toUpperCase());
            case NUMBER -> {
                if (!value.isEmpty() && (Character.isDigit(value.charAt(0)) || value.charAt(0) == '-')) {
                    out.write(value);
                } else {
                    writeQuoted(value);
                }
            }
            default -> writeQuoted(value);
        }
    }

    private void writeQuoted(String value) throws IOException {
        out.write('\'');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\'') {
                out.write('\'');
            }
            out.write(c);
        }
        out.write('\'');
    }
}
