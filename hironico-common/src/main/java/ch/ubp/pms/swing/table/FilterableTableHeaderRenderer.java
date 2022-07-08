package ch.ubp.pms.swing.table;

import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;

import java.awt.Component;
import java.awt.Font;

public class FilterableTableHeaderRenderer implements TableCellRenderer {
    private TableCellRenderer delegate = null;

    public FilterableTableHeaderRenderer(TableCellRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component comp = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table instanceof FilterableTable) {
            FilterableTable filterable = (FilterableTable) table;
            Font font = comp.getFont();
            if (filterable.isFiltered(column)) {
                font = font.deriveFont(Font.BOLD);
            } else {
                font = font.deriveFont(Font.PLAIN);
            }
            comp.setFont(font);
        }

        return comp;
    }

}