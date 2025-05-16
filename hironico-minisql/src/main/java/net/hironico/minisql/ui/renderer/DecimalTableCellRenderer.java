package net.hironico.minisql.ui.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

public class DecimalTableCellRenderer implements TableCellRenderer {
    private final DecimalFormat formatter;
    private final TableCellRenderer delegate;

    public DecimalTableCellRenderer(TableCellRenderer delegate) {
        this(delegate, "0.00##########");
    }

    public DecimalTableCellRenderer(TableCellRenderer delegate, String decimalPattern) {
        super();
        this.delegate = delegate;
        this.formatter = new DecimalFormat(decimalPattern);
    }

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
