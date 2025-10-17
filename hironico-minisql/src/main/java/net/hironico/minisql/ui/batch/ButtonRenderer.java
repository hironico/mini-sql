package net.hironico.minisql.ui.batch;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Button renderer for tree table cells.
 */
class ButtonRenderer extends JButton implements TableCellRenderer {
    private final JLabel emptyLabel = new JLabel("");

    public ButtonRenderer() {
        setOpaque(true);
        emptyLabel.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value == null) {
            if (isSelected) {
                emptyLabel.setBackground(table.getSelectionBackground());
                emptyLabel.setForeground(table.getSelectionForeground());
            } else {
                emptyLabel.setBackground(table.getBackground());
                emptyLabel.setForeground(table.getForeground());
            }

            return emptyLabel;
        } else {
            setText(value.toString());
            return this;
        }
    }
}
