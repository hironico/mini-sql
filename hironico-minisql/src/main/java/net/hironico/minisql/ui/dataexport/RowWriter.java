package net.hironico.minisql.ui.dataexport;

import java.io.IOException;

/**
 * Writes the rows of an export one by one to an output stream, in a given format.
 * Implementations must not keep the rows in memory. The arrays given to
 * {@link #writeRow(String[], ValueKind[])} are reused between calls.
 */
public interface RowWriter {

    /**
     * Called once before the first row.
     *
     * @param columnNames names of the exported columns, in order
     */
    void start(String[] columnNames) throws IOException;

    /**
     * Writes one row.
     *
     * @param values text of each value, null when the kind is {@link ValueKind#NULL}
     * @param kinds  kind of each value
     */
    void writeRow(String[] values, ValueKind[] kinds) throws IOException;

    /** Called once after the last row. */
    void finish() throws IOException;
}
