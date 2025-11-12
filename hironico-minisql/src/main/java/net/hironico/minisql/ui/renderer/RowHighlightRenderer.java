package net.hironico.minisql.ui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table cell renderer that provides alternating row highlighting with special handling for null values.
 * This decorator renderer enhances any delegate renderer by applying alternate row background colors
 * and special visual treatment for null values. Can be chained in a renderer delegation pattern for
 * complex table display requirements.
 */
public class RowHighlightRenderer implements TableCellRenderer {

    /** Delegate renderer that handles the actual rendering */
    private TableCellRenderer delegate;

    /** Background color for odd-numbered rows (row % 2 == 0) */
    private final Color bgColorOdd = new Color(237, 243, 254);

    /**
     * Constructs a new row highlight renderer.
     *
     * @param delegate the delegate renderer to decorate with highlighting behavior
     */
    public RowHighlightRenderer(TableCellRenderer delegate) {
        assert (delegate != null);
        this.delegate = delegate;
    }

    /**
     * Gets the component for rendering the cell with row highlighting.
     * Applies alternating background colors for unselected rows and special
     * visual treatment for null values. For unselected rows, uses light blue
     * for even rows and white for odd rows. Null values get a yellowish tint.
     * Selected rows retain their selection colors.
     *
     * @param table the JTable instance
     * @param value the cell value to render
     * @param isSelected true if the cell is selected
     * @param hasFocus true if the cell has focus
     * @param row the row index (used for alternating color logic)
     * @param column the column index
     * @return the component with enhanced visual styling
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component delegateComp = this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        if (!isSelected) {
            Color bgColor = row % 2 == 0 ? bgColorOdd : Color.WHITE;

            int sum = bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue();
            sum = sum / 128;

            Color fgColor = delegateComp.getForeground();
            // couleur moyenne foncée ?
            if (sum < 3) {
                fgColor = Color.WHITE;
            }

            // c'est null ? yellowifier le fond
            if (value == null) {
                int rgb = bgColor.getRGB() & 0x00ffffc8; // mask rgb + alpha channel
                bgColor = new Color(rgb);
            }

            delegateComp.setBackground(bgColor);
            delegateComp.setForeground(fgColor);
        }

        return delegateComp;
    }
}
