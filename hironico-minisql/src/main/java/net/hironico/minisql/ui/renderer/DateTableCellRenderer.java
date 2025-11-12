package net.hironico.minisql.ui.renderer;

import java.awt.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table cell renderer for formatting date and time values in tables.
 * Converts java.sql.Timestamp objects to formatted strings using a configurable
 * date-time pattern. Non-date values are passed through to the delegate renderer unchanged.
 * Uses Europe/Zurich timezone for consistent date/time representation.
 */
public class DateTableCellRenderer implements TableCellRenderer {

    /** Date-time formatter for converting timestamps to strings */
    private final DateTimeFormatter formatter;

    /** Delegate renderer for actual cell rendering */
    private final TableCellRenderer delegate;

    /**
     * Constructs a new date table cell renderer with default pattern.
     * Uses the standard "yyyy-MM-dd HH:mm:ss" format.
     *
     * @param delegate the delegate renderer for cell display
     */
    public DateTableCellRenderer(TableCellRenderer delegate) {
        this(delegate, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Constructs a new date table cell renderer with custom pattern.
     *
     * @param delegate the delegate renderer for cell display
     * @param dateTimePattern the pattern to use for date-time formatting
     */
    public DateTableCellRenderer(TableCellRenderer delegate, String dateTimePattern) {
        super();
        this.delegate = delegate;
        this.formatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    /**
     * Gets the component for rendering the cell.
     * If the value is a Date/Timestamp, formats it using the configured pattern.
     * Otherwise, delegates to the standard renderer for unchanged display.
     *
     * @param table the JTable instance
     * @param value the cell value to render
     * @param isSelected true if the cell is selected
     * @param hasFocus true if the cell has focus
     * @param row the row index
     * @param column the column index
     * @return the component with formatted date display
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if (value instanceof Date) {
            Timestamp valueDate = (Timestamp)value;
            LocalDateTime dt = LocalDateTime.ofInstant(valueDate.toInstant(), ZoneId.of("Europe/Zurich"));
            return this.delegate.getTableCellRendererComponent(table, dt.format(formatter), isSelected, hasFocus, row, column);
        } else {
            return this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
