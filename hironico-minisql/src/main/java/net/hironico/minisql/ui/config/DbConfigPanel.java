package net.hironico.minisql.ui.config;

import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.DbConfig;
import org.jdesktop.swingx.JXColorSelectionButton;
import org.jdesktop.swingx.JXLabel;

/**
 * Panel to model the UI for managing connections to databases
 */
public class DbConfigPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DbConfigPanel.class.getName());

    private JToolBar toolbar;
    private JComboBox<String> cmbConnectionList;
    private JButton btnNew;
    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnDuplicate;
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblJdbcUrl;
    private JTextField txtJdbcUrl;
    private JLabel lblUser;
    private JTextField txtUser;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblDriverClassName;
    private JTextField txtDriverClassName;
    private JLabel lblStatementSeparator;
    private JTextField txtStatementSeparator;
    private JButton btnTestConnection = null;
    private JXLabel txtColor = null;
    private JXColorSelectionButton colorChooser = null;
    private JCheckBox chkUsequotedIdentifiers = null;

    public DbConfigPanel() {
        super();
        initialize();
        loadAllConfigs();
    }

    protected void loadAllConfigs() {
        JComboBox<String> cmb = getCmbConnectionList();
        cmb.removeAllItems();
        for (String name : DbConfigFile.getConfigNames()) {
            cmb.addItem(name);
        }
    }

    protected void clearForm() {
        getTxtName().setText("");
        getTxtJdbcUrl().setText("");
        getTxtUser().setText("");
        getTxtPassword().setText("");
        getTxtDriverClassName().setText("");
        getTxtStatementSeparator().setText("");
        getChkUseQuotedIdentifiers().setSelected(false);
    }

    protected void loadSelectedConfig(String name) {
        DbConfig cfg = DbConfigFile.getConfig(name);
        if (cfg == null) {
            return;
        }

        clearForm();

        getTxtName().setText(cfg.name);
        getTxtJdbcUrl().setText(cfg.jdbcUrl);
        getTxtUser().setText(cfg.user);
        getTxtPassword().setText(DbConfig.decryptPassword(cfg.password));
        getTxtDriverClassName().setText(cfg.driverClassName);
        getTxtStatementSeparator().setText(cfg.batchStatementSeparator);
        Color conColor = cfg.color == null ? Color.BLUE : Color.decode(cfg.color);
        getColorChooser().getChooser().setColor(conColor);
        getColorChooser().setBackground(conColor);
        getChkUseQuotedIdentifiers().setSelected(cfg.useQuotedIdentifiers);
    }

    protected void initialize() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(-2, 5, 5, 5));

        setOpaque(true);
        // setBackground(new Color(238, 243, 250));
        setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;

        add(getToolbar(), gc);

        gc.gridy = 1;
        gc.insets.top = 5;
        add(getLblName(), gc);

        gc.gridy = 2;
        gc.insets.top = 0;
        add(getTxtName(), gc);

        gc.gridy = 3;
        gc.insets.top = 5;
        add(getLblJdbcUrl(), gc);

        gc.gridy = 4;
        gc.insets.top = 0;
        add(getTxtJdbcUrl(), gc);

        gc.gridy = 5;
        gc.insets.top = 5;
        add(getLblUser(), gc);

        gc.gridy = 6;
        gc.insets.top = 0;
        add(getTxtUser(), gc);

        gc.gridy = 7;
        gc.insets.top = 5;
        add(getLblPassword(), gc);

        gc.gridy = 8;
        gc.insets.top = 0;
        add(getTxtPassword(), gc);

        gc.gridy = 9;
        gc.insets.top = 5;
        add(getLblDriverClassName(), gc);

        gc.gridy = 10;
        gc.insets.top = 0;
        add(getTxtDriverClassName(), gc);

        gc.gridy = 11;
        gc.insets.top = 5;
        add(getLblStatementSeparator(), gc);

        gc.gridy = 12;
        gc.insets.top = 5;
        add(getTxtStatementSeparator(), gc);

        gc.gridy = 13;
        gc.insets.top = 5;
        add(getTxtColor(), gc);

        gc.gridy = 14;
        gc.insets.top = 5;
        add(getColorChooser(), gc);

        gc.gridy = 15;
        gc.anchor = GridBagConstraints.NORTH;
        gc.weighty = 1.0;
        gc.insets.top = 0;
        add(getChkUseQuotedIdentifiers(), gc);
    }

    protected JToolBar getToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();

            toolbar.add(getBtnNew());
            toolbar.add(getBtnDuplicate());
            toolbar.add(getCmbConnectionList());
            toolbar.add(getBtnSave());
            toolbar.addSeparator();
            toolbar.add(getBtnTestConnection());
            toolbar.addSeparator();
            toolbar.add(getBtnDelete());
        }

        return toolbar;
    }

    protected JButton getBtnTestConnection() {
        if (btnTestConnection == null) {
            btnTestConnection = new JButton();
            btnTestConnection.setText("Test");
            btnTestConnection.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DbConfig cfg = DbConfigPanel.this.saveDbConfig();
                    try {
                        assert cfg != null;
                        try (Connection con = cfg.getConnection()) {
                            JOptionPane.showMessageDialog(DbConfigPanel.this, "It works !", "Yeah...",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Problem while testing connection.", ex);
                        JOptionPane.showMessageDialog(DbConfigPanel.this,
                                "Problem while testing connection.\n" + ex.getMessage(), "Error...",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        return btnTestConnection;
    }

    protected JButton getBtnNew() {
        if (btnNew == null) {
            btnNew = new JButton("New");
            btnNew.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog(DbConfigPanel.this, "Please enter new configuration name:");
                    if (name == null) {
                        return;
                    }

                    DbConfig cfg = DbConfigFile.getConfig(name);

                    if (cfg != null) {
                        cmbConnectionList.setSelectedItem(name);
                        return;
                    }

                    DbConfigFile.addConfig(name);
                    DbConfigPanel.this.loadAllConfigs();
                }
            });
        }

        return btnNew;
    }

    private DbConfig saveDbConfig() {
        String name = getTxtName().getText();
        DbConfig cfg = DbConfigFile.getConfig(name);
        if (cfg == null) {
            JOptionPane.showMessageDialog(DbConfigPanel.this,
                    "Error: This config name is unknown. This should not have happened: " + name, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        cfg.jdbcUrl = getTxtJdbcUrl().getText();
        cfg.user = getTxtUser().getText();
        cfg.password = DbConfig.encryptPassword(String.copyValueOf(getTxtPassword().getPassword()));
        cfg.driverClassName = getTxtDriverClassName().getText();
        cfg.batchStatementSeparator = getTxtStatementSeparator().getText();
        Color bg = getColorChooser().getBackground();
        cfg.color = String.format("#%02x%02x%02x", bg.getRed(), bg.getGreen(), bg.getBlue());
        cfg.useQuotedIdentifiers = getChkUseQuotedIdentifiers().isSelected();

        return cfg;
    }

    protected JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton("Save");

            btnSave.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    saveDbConfig();
                }
            });
        }

        return btnSave;
    }

    protected JButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new JButton("Delete");

            btnDelete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = getTxtName().getText();
                    DbConfig cfg = DbConfigFile.getConfig(name);
                    if (cfg == null) {
                        JOptionPane.showMessageDialog(DbConfigPanel.this,
                                "Error: This config name is unknown. This should not have happened: " + name, "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int resp = JOptionPane.showConfirmDialog(DbConfigPanel.this,
                            "Delete this configuration ?\nThis operation cannot be undone.", "Warning",
                            JOptionPane.YES_NO_OPTION);
                    if (resp != JOptionPane.YES_OPTION) {
                        return;
                    }

                    DbConfigFile.removeConfig(name);

                    DbConfigPanel.this.loadAllConfigs();
                }
            });
        }

        return btnDelete;
    }

    protected JButton getBtnDuplicate() {
        if (btnDuplicate == null) {
            btnDuplicate = new JButton("Duplicate");

            btnDuplicate.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String sourceConfig = (String) getCmbConnectionList().getSelectedItem();
                    if (sourceConfig == null) {
                        return;
                    }

                    String name = JOptionPane.showInputDialog(DbConfigPanel.this, "Please enter new configuration name:");
                    if (name == null) {
                        return;
                    }

                    DbConfig cfg = DbConfigFile.getConfig(name);

                    if (cfg != null) {
                        cmbConnectionList.setSelectedItem(name);
                        return;
                    }

                    cfg = DbConfigFile.duplicate(sourceConfig, name);
                    DbConfigPanel.this.loadAllConfigs();
                    getCmbConnectionList().setSelectedItem(name);
                }
            });
        }

        return btnDuplicate;
    }

    protected JComboBox<String> getCmbConnectionList() {
        if (cmbConnectionList == null) {
            cmbConnectionList = new JComboBox<String>();

            cmbConnectionList.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        loadSelectedConfig((String) e.getItem());
                    }
                }
            });
        }

        return cmbConnectionList;
    }

    protected JLabel getLblName() {
        if (lblName == null) {
            lblName = new JLabel("Connection name:");
        }

        return lblName;
    }

    protected JTextField getTxtName() {
        if (txtName == null) {
            txtName = new JTextField();
            txtName.setEditable(false);
        }

        return txtName;
    }

    protected JLabel getLblJdbcUrl() {
        if (lblJdbcUrl == null) {
            lblJdbcUrl = new JLabel("JDBC URL:");
        }

        return lblJdbcUrl;
    }

    protected JTextField getTxtJdbcUrl() {
        if (txtJdbcUrl == null) {
            txtJdbcUrl = new JTextField();
        }

        return txtJdbcUrl;
    }

    protected JLabel getLblUser() {
        if (lblUser == null) {
            lblUser = new JLabel("User name:");
        }

        return lblUser;
    }

    protected JTextField getTxtUser() {
        if (txtUser == null) {
            txtUser = new JTextField();
        }

        return txtUser;
    }

    protected JLabel getLblPassword() {
        if (lblPassword == null) {
            lblPassword = new JLabel("Password:");
        }

        return lblPassword;
    }

    protected JPasswordField getTxtPassword() {
        if (txtPassword == null) {
            txtPassword = new JPasswordField();
        }

        return txtPassword;
    }

    protected JLabel getLblDriverClassName() {
        if (lblDriverClassName == null) {
            lblDriverClassName = new JLabel();
            lblDriverClassName.setText("Driver class name:");
        }

        return lblDriverClassName;
    }

    protected JTextField getTxtDriverClassName() {
        if (txtDriverClassName == null) {
            txtDriverClassName = new JTextField();
        }

        return txtDriverClassName;
    }

    protected JLabel getLblStatementSeparator() {
        if (lblStatementSeparator == null) {
            lblStatementSeparator = new JLabel("SQL statement separator (for batch mode only):");
        }

        return lblStatementSeparator;
    }

    protected JTextField getTxtStatementSeparator() {
        if (txtStatementSeparator == null) {
            txtStatementSeparator = new JTextField();
            txtStatementSeparator.setToolTipText("Separator used to segregate statements to be executed one by one in a batch. Only valid for batch execution mode.");
        }

        return txtStatementSeparator;
    }

    protected JXLabel getTxtColor() {
        if (txtColor == null) {
            txtColor = new JXLabel("Connection color:");
        }
        return txtColor;
    }

    protected JXColorSelectionButton getColorChooser() {
        if (this.colorChooser == null) {
            this.colorChooser = new JXColorSelectionButton();
            this.colorChooser.setText("Connection color");
        }

        return colorChooser;
    }

    private JCheckBox getChkUseQuotedIdentifiers() {
        if (chkUsequotedIdentifiers == null) {
            chkUsequotedIdentifiers = new JCheckBox("Use quoted identfiers");
        }
        return chkUsequotedIdentifiers;
    }
}