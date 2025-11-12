package net.hironico.minisql.ui.config;

import net.hironico.minisql.DbConfigFile;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class GeneralConfigPanel extends JPanel {
    private JCheckBox chkDecoratedWindow = null;
    private JTextField txtLogMaxRows = null;

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
        gc.weightx = 1.0d;
        gc.weighty = 0.0d;
        gc.anchor = GridBagConstraints.NORTH;

        add(getChkDecoratedWindow(), gc);

        gc.gridy++;
        gc.insets = new Insets(10, 0,0, 0);
        add(new JLabel("Log panel max rows:"), gc);
        gc.gridy++;
        gc.weighty = 1.0d;
        gc.insets.top = 0;
        add(getTxtLogMaxRows(), gc);
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

    private JTextField getTxtLogMaxRows() {
        if (txtLogMaxRows == null) {
            txtLogMaxRows = new JTextField();
            txtLogMaxRows.setText(String.valueOf(DbConfigFile.getInstance().getLogMaxRows()));
            txtLogMaxRows.setToolTipText("Enter a positive integer value for maximum log rows");

            txtLogMaxRows.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    validateAndUpdate();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validateAndUpdate();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validateAndUpdate();
                }

                private void validateAndUpdate() {
                    String text = txtLogMaxRows.getText().trim();
                    
                    // Check if empty
                    if (text.isEmpty()) {
                        txtLogMaxRows.setBackground(new Color(255, 200, 200));
                        txtLogMaxRows.setToolTipText("Value cannot be empty. Enter a positive integer.");
                        return;
                    }
                    
                    // Try to parse as integer
                    try {
                        int value = Integer.parseInt(text);
                        
                        // Check if positive
                        if (value <= 0) {
                            txtLogMaxRows.setBackground(new Color(255, 200, 200));
                            txtLogMaxRows.setToolTipText("Value must be a positive integer (greater than 0).");
                            return;
                        }
                        
                        // Valid integer - update config and reset background
                        txtLogMaxRows.setBackground(Color.WHITE);
                        txtLogMaxRows.setToolTipText("Enter a positive integer value for maximum log rows");
                        DbConfigFile.getInstance().setLogMaxRows(value);
                        
                    } catch (NumberFormatException ex) {
                        // Invalid integer format
                        txtLogMaxRows.setBackground(new Color(255, 200, 200));
                        txtLogMaxRows.setToolTipText("Invalid input. Enter a positive integer value only.");
                    }
                }
            });
        }

        return txtLogMaxRows;
    }
}
