package net.hironico.minisql.ui.config;

import net.hironico.minisql.DbConfigFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneralConfigPanel extends JPanel {
    private JCheckBox chkDecoratedWindow = null;

    public GeneralConfigPanel() {
        super();
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTH;

        add(getChkDecoratedWindow(), gc);
    }

    private JCheckBox getChkDecoratedWindow() {
        if (chkDecoratedWindow == null) {
            chkDecoratedWindow = new JCheckBox("Decorated window. EXPERIMENTAL !");
            chkDecoratedWindow.setToolTipText("WARNING: Do not use with multiscreen configurations.");
            chkDecoratedWindow.setSelected(DbConfigFile.getInstance().getDecoratedWindow());

            chkDecoratedWindow.addActionListener(e -> {
                DbConfigFile.getInstance().setDecoratedWindow(getChkDecoratedWindow().isSelected());
            });
        }

        return chkDecoratedWindow;
    }
}
