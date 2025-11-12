package net.hironico.minisql.ui.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Table cell renderer for formatting decimal/numeric values in tables.
 * Converts Number objects to formatted strings using a configurable decimal pattern.
 * Non-numeric values are passed through to the delegate renderer unchanged.
 * Useful for displaying precise decimal representations in query result tables.
 */
public class DecimalTableCellRenderer implements TableCellRenderer {

    /** Decimal formatter for converting numbers to formatted strings */
    private final DecimalFormat formatter;

    /** Delegate renderer for actual cell rendering */
    private final TableCellRenderer delegate;

    /**
     * Constructs a new decimal table cell renderer with default pattern.
     * Uses the pattern "0.00##########" which displays up to 10 decimal places
     * while hiding trailing zeros.
     *
     * @param delegate the delegate renderer for cell display
     */
    public DecimalTableCellRenderer(TableCellRenderer delegate) {
        this(delegate, "0.00##########");
    }

    /**
     * Constructs a new decimal table cell renderer with custom pattern.
     *
     * @param delegate the delegate renderer for cell display
     * @param decimalPattern the DecimalFormat pattern for number formatting
     */
    public DecimalTableCellRenderer(TableCellRenderer delegate, String decimalPattern) {
        super();
        this.delegate = delegate;
        this.formatter = new DecimalFormat(decimalPattern);
    }

    /**
     * Gets the component for rendering the cell.
     * If the value is numeric, formats it using the configured decimal pattern.
     * Otherwise, delegates to the standard renderer for unchanged display.
     *
     * @param table the JTable instance
     * @param value the cell value to render
     * @param isSelected true if the cell is selected
     * @param hasFocus true if the cell has focus
     * @param row the row index
     * @param column the column index
     * @return the component with formatted decimal display
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof Number) {
            final String text = formatter.format(value);
            return this.delegate.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        } else {
            return this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
