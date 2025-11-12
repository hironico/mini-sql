package net.hironico.minisql.ui.batch;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer for displaying buttons in tree table cells.
 * This renderer displays either a button with text (when value is not null) or
 * an empty label (when value is null), properly handling selection colors.
 */
class ButtonRenderer extends JButton implements TableCellRenderer {

    /** Empty label used when no button should be displayed */
    private final JLabel emptyLabel = new JLabel("");

    /**
     * Constructs a new ButtonRenderer.
     * Initializes the button and empty label as opaque components.
     */
    public ButtonRenderer() {
        setOpaque(true);
        emptyLabel.setOpaque(true);
    }

    /**
     * Gets the component for rendering the cell.
     * Returns either this button instance (when value is not null) or
     * an empty label (when value is null), with appropriate colors for selection state.
     *
     * @param table the JTable instance
     * @param value the cell value to render
     * @param isSelected true if the cell is selected
     * @param hasFocus true if the cell has focus
     * @param row the row index
     * @param column the column index
     * @return the component to use for rendering the cell
     */
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
