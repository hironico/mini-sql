package ch.ubp.pms.minisql.ui.renderer;

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

public class ClobTableCellRenderer extends JPanel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(ClobTableCellRenderer.class.getName());

    private TableCellRenderer rendererDelegate;

    private JLabel label = null;
    private JButton button = null;

    public ClobTableCellRenderer(TableCellRenderer rendererDelegate) {
        this.rendererDelegate = rendererDelegate;
        initialize();
    }

    protected void initialize() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        add(getLabel());
        add(getButton());
    }

    protected JLabel getLabel() {
        if (label == null) {
            label = new JLabel();
            label.setBorder(BorderFactory.createEmptyBorder());
            label.setOpaque(false);
        }

        return label;
    }
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