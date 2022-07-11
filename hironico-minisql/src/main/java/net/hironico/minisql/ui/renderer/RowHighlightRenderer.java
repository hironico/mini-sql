package net.hironico.minisql.ui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * TableCellRenderer highlight background color for odd rows 
 * This renderer enrich the behavior of a delegate renderer so it can be part of a renderer
 * delegation chain.
 */
public class RowHighlightRenderer implements TableCellRenderer {
    private TableCellRenderer delegate;
    private final Color bgColorOdd = new Color(237, 243, 254);

    public RowHighlightRenderer(TableCellRenderer delegate) {
        assert (delegate != null);
        this.delegate = delegate;
    }

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