package net.hironico.minisql.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table cell renderer for CLOB (Character Large OBject) database columns.
 * Provides specialized rendering for large text content by displaying a compact
 * indicator ("&lt;CLOB&gt;" or "&lt;NULL&gt;") with a viewer button, rather than
 * showing the actual potentially large content inline. Used as a decorator
 * around standard table cell renderers.
 */
public class ClobTableCellRenderer extends JPanel implements TableCellRenderer {

    /** Serial version UID for object serialization */
    private static final long serialVersionUID = 1L;

    /** Logger for this class */
    private static Logger LOGGER = Logger.getLogger(ClobTableCellRenderer.class.getName());

    /** Delegate renderer for non-CLOB columns */
    private TableCellRenderer rendererDelegate;

    /** Label displaying CLOB indicator text */
    private JLabel label = null;

    /** Button for potential future interaction (currently placeholder) */
    private JButton button = null;

    /**
     * Constructs a new CLOB table cell renderer.
     *
     * @param rendererDelegate the delegate renderer for non-CLOB columns
     */
    public ClobTableCellRenderer(TableCellRenderer rendererDelegate) {
        this.rendererDelegate = rendererDelegate;
        initialize();
    }

    /**
     * Initializes the renderer components.
     * Sets up the panel layout with label and button for compact CLOB display.
     */
    protected void initialize() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        add(getLabel());
        add(getButton());
    }

    /**
     * Gets or creates the label for displaying CLOB indicator text.
     * Shows the type of CLOB content (present or null).
     *
     * @return the JLabel for CLOB indication
     */
    protected JLabel getLabel() {
        if (label == null) {
            label = new JLabel();
            label.setBorder(BorderFactory.createEmptyBorder());
            label.setOpaque(false);
        }

        return label;
    }

    /**
     * Gets or creates the button for potential CLOB interaction.
     * Currently serves as a visual placeholder but could be used for
     * future interaction functionality.
     *
     * @return the JButton for CLOB interaction
     */
    protected JButton getButton() {
        if (button == null) {
            button = new JButton();
            button.setText("...");
            button.setBorderPainted(false);
            button.setMargin(new Insets(0, 0, 0, 0));

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    LOGGER.info("Action listener from renderer !");
                }
            });
        }

        return button;
    }

    /**
     * Gets the component for rendering the cell.
     * Returns this specialized CLOB renderer panel for CLOB columns,
     * or delegates to the standard renderer for other column types.
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
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        Component delegateComp = this.rendererDelegate.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);

        Class<?> clazz = table.getModel().getColumnClass(column);
        if (clazz.getName().toLowerCase().endsWith("clob")) {

            String txt = value == null ? "<NULL>" : "<CLOB>";
            getLabel().setText(txt);

            // LOGGER.info("CLOB renderer selected = " + isSelected);

            Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBackground(bg);

            Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
            getLabel().setForeground(fg);

            return this;
        } else {
            LOGGER.warning("Using delegate renderer...");
            return delegateComp;
        }
    }

}
