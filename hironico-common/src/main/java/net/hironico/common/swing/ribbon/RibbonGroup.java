package net.hironico.common.swing.ribbon;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class RibbonGroup extends JPanel {

    private static final long serialVersionUID = 1L;

    protected String title = null;
    protected JLabel lblTitle = null;
    protected JPanel pnlTitle = null;
    protected JPanel pnlCommands = null;

    public static final int SMALL = 0;
    public static final int LARGE = 3;

    private int currentRow = 0;
    private int currentColumn = 0;

    public RibbonGroup(String title) {
        super();
        this.title = title;
        initialize();
    }

    /**
     * Add a checkbox to the ribbon. Size is always small
     * @param action action to perfom when the checkbox is clicked
     */
    public void addCheckBox(AbstractRibbonAction action) {
        final JCheckBox chk = new JCheckBox(action);
        chk.setContentAreaFilled(false);
        chk.setText((String)action.getValue(Action.NAME));
        chk.setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
        // chk.setIcon(action.getSmallIcon());
        chk.setMinimumSize(new Dimension(24,24));

        chk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                chk.setContentAreaFilled(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                chk.setContentAreaFilled(true);
            }

        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = currentColumn;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridheight = 1;
        gbc.weighty = 0.33d;

        getPnlCommands().add(chk, gbc);

        if (chk.getHeight() < 16) {
            chk.setSize(chk.getWidth(), 16);
        }

        // ensure we see the whole text
        FontMetrics fm = chk.getFontMetrics(chk.getFont());
        int width = fm.stringWidth(chk.getText());
        chk.setSize(width, chk.getHeight());

        if (currentRow == 2) {
            currentRow = 0;
            currentColumn++;
        } else {
            currentRow++;
        }
    }

    public void addAction(AbstractRibbonAction action, int size) {

        if ((size == LARGE) && (currentRow > 0)) {

            // add a filler for the missing rows
            for (int time = 0; time < (3 - currentRow); time++) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 2, 2, 2);
                gbc.gridx = currentColumn;
                gbc.gridy = currentRow;
                gbc.fill = GridBagConstraints.VERTICAL;
                gbc.gridheight = 1;
                gbc.weighty = 0.33d;
                JPanel pnlZero = new JPanel();
                pnlCommands.add(pnlZero, gbc);
            }

            currentRow = 0;
            currentColumn++;
        }

        final JButton btn = new JButton(action);
        btn.setContentAreaFilled(false);
        btn.setText((String)action.getValue(Action.NAME));
        btn.setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));

        if (size == LARGE) {
            btn.setIcon(action.getLargeIcon());            
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setMinimumSize(new Dimension(48,48));
        } else {            
            btn.setIcon(action.getSmallIcon());
            btn.setMinimumSize(new Dimension(24,24));
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
            }

        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = currentColumn;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;

        switch (size) {
        case SMALL:
            gbc.gridheight = 1;
            gbc.weighty = 0.33d;
            // btn.setPreferredSize(new Dimension(256, 16));
            break;

        case LARGE:
            gbc.gridheight = 3;
            gbc.weighty = 1.0d;
            btn.setHorizontalTextPosition(JButton.CENTER);
            btn.setVerticalTextPosition(JButton.BOTTOM);
            break;

        default:
            gbc.gridheight = 1;
            break;
        }

        getPnlCommands().add(btn, gbc);

        if (btn.getHeight() < 16) {
            btn.setSize(btn.getWidth(), 16);
        }

        if ((size == LARGE) || (currentRow == 2)) {
            currentRow = 0;
            currentColumn++;
        } else {
            currentRow++;
        }
    }

    protected void initialize() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(false);
        add(getPnlCommands(), BorderLayout.CENTER);
        add(getPnlTitle(), BorderLayout.SOUTH);
    }

    protected JPanel getPnlCommands() {
        if (this.pnlCommands == null) {
            this.pnlCommands = new JPanel();
            this.pnlCommands.setLayout(new GridBagLayout());
            this.pnlCommands.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            this.pnlCommands.setOpaque(false);
        }

        return this.pnlCommands;
    }

    protected JPanel getPnlTitle() {
        if (pnlTitle == null) {
            pnlTitle = new JPanel();
            pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
            pnlTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            pnlTitle.setOpaque(false);

            // pnlTitle.add(new JSeparator());
            pnlTitle.add(getLblTitle());
        }

        return pnlTitle;
    }

    protected JLabel getLblTitle() {
        if (this.lblTitle == null) {
            this.lblTitle = new JLabel(title);
            this.lblTitle.setBorder(BorderFactory.createEmptyBorder());
            this.lblTitle.setHorizontalTextPosition(JLabel.CENTER);
            this.lblTitle.setVerticalTextPosition(JLabel.CENTER);
            this.lblTitle.setOpaque(false);
            this.lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.lblTitle.setForeground(new Color(150, 109, 145));
        }

        return this.lblTitle;
    }

}