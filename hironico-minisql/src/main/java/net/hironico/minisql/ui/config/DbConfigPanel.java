package net.hironico.minisql.ui.config;

import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
 * Panel to model the UI for managing connections to databases.
 * This panel provides a comprehensive interface for creating, editing, testing,
 * and managing database connection configurations with support for various
 * connection parameters and visual identification through color coding.
 */
public class DbConfigPanel extends JPanel {

    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(DbConfigPanel.class.getName());

    /** Toolbar containing action buttons and connection selector */
    private JToolBar toolbar;

    /** Combo box for selecting existing database configurations */
    private JComboBox<String> cmbConnectionList;

    /** Button for creating new database configuration */
    private JButton btnNew;

    /** Button for saving current configuration changes */
    private JButton btnSave;

    /** Button for deleting selected configuration */
    private JButton btnDelete;

    /** Button for duplicating selected configuration */
    private JButton btnDuplicate;

    /** Label for connection name field */
    private JLabel lblName;

    /** Text field for displaying/editing connection name */
    private JTextField txtName;

    /** Label for JDBC URL field */
    private JLabel lblJdbcUrl;

    /** Text field for JDBC connection URL */
    private JTextField txtJdbcUrl;

    /** Label for username field */
    private JLabel lblUser;

    /** Text field for database username */
    private JTextField txtUser;

    /** Label for password field */
    private JLabel lblPassword;

    /** Password field for database password */
    private JPasswordField txtPassword;

    /** Toggle button for showing/hiding password */
    private JToggleButton btnTogglePassword;

    /** Button for copying password to clipboard */
    private JButton btnCopyPassword;

    /** Label for driver class name field */
    private JLabel lblDriverClassName;

    /** Text field for JDBC driver class name */
    private JTextField txtDriverClassName;

    /** Label for SQL statement separator field */
    private JLabel lblStatementSeparator;

    /** Text field for SQL statement separator used in batch mode */
    private JTextField txtStatementSeparator;

    /** Button for testing database connection */
    private JButton btnTestConnection = null;

    /** Label for connection color selection */
    private JXLabel txtColor = null;

    /** Color selection button for connection visual identification */
    private JXColorSelectionButton colorChooser = null;

    /** Checkbox for enabling quoted identifiers */
    private JCheckBox chkUseQuotedIdentifiers = null;

    /**
     * Constructs a new DbConfigPanel with default configuration.
     * Initializes the UI components and loads all existing database configurations.
     */
    public DbConfigPanel() {
        super();
        initialize();
        loadAllConfigs();
    }

    /**
     * Loads all available database configurations into the connection list combo box.
     * Clears existing items and repopulates with current configuration names.
     */
    protected void loadAllConfigs() {
        JComboBox<String> cmb = getCmbConnectionList();
        cmb.removeAllItems();
        for (String name : DbConfigFile.getConfigNames()) {
            cmb.addItem(name);
        }
    }

    /**
     * Clears all form fields to their default empty state.
     * Resets text fields and checkbox to initial values.
     */
    protected void clearForm() {
        getTxtName().setText("");
        getTxtJdbcUrl().setText("");
        getTxtUser().setText("");
        getTxtPassword().setText("");
        getTxtDriverClassName().setText("");
        getTxtStatementSeparator().setText("");
        getChkUseQuotedIdentifiers().setSelected(false);
    }

    /**
     * Loads the specified database configuration into the form fields.
     * Clears existing form data and populates fields with configuration values.
     *
     * @param name the name of the configuration to load
     */
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

    /**
     * Initializes the UI components and layout.
     * Sets up the panel with GridBagLayout and adds all form elements.
     */
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
        gc.gridwidth = 3;
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
        gc.gridwidth = 3;
        add(getLblPassword(), gc);

        // Password row with field and buttons
        gc.gridy = 8;
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.weightx = 1.0;
        gc.insets.top = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(getTxtPassword(), gc);

        gc.gridx = 1;
        gc.weightx = 0.0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets.left = 5;
        add(getBtnTogglePassword(), gc);

        gc.gridx = 2;
        gc.insets.left = 0;
        add(getBtnCopyPassword(), gc);

        // Reset for next rows
        gc.gridx = 0;
        gc.gridwidth = 3;
        gc.weightx = 1.0;
        gc.insets.left = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridy = 9;
        gc.gridwidth = 3;
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

    /**
     * Gets or creates the toolbar component.
     * Creates toolbar with action buttons for configuration management.
     *
     * @return the JToolBar instance
     */
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

    /**
     * Gets or creates the test connection button.
     * Sets up action listener to test database connectivity using current configuration.
     *
     * @return the JButton for testing connections
     */
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

    /**
     * Gets or creates the new configuration button.
     * Sets up action listener to create new database configurations.
     *
     * @return the JButton for creating new configurations
     */
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

    /**
     * Saves the current form data to the database configuration.
     * Updates the DbConfig object with current field values and encrypts password.
     *
     * @return the updated DbConfig instance, or null if configuration not found
     */
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

    /**
     * Gets or creates the save configuration button.
     * Sets up action listener to save current configuration changes.
     *
     * @return the JButton for saving configurations
     */
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

    /**
     * Gets or creates the delete configuration button.
     * Sets up action listener to remove selected configuration after confirmation.
     *
     * @return the JButton for deleting configurations
     */
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

    /**
     * Gets or creates the duplicate configuration button.
     * Sets up action listener to create a copy of selected configuration.
     *
     * @return the JButton for duplicating configurations
     */
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

    /**
     * Gets or creates the connection list combo box.
     * Sets up item listener to load configuration when selection changes.
     *
     * @return the JComboBox containing available configurations
     */
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

    /**
     * Gets or creates the connection name label.
     *
     * @return the JLabel for connection name field
     */
    protected JLabel getLblName() {
        if (lblName == null) {
            lblName = new JLabel("Connection name:");
        }

        return lblName;
    }

    /**
     * Gets or creates the connection name text field.
     * Field is set to non-editable as names are managed through the combo box.
     *
     * @return the JTextField for connection name
     */
    protected JTextField getTxtName() {
        if (txtName == null) {
            txtName = new JTextField();
            txtName.setEditable(false);
        }

        return txtName;
    }

    /**
     * Gets or creates the JDBC URL label.
     *
     * @return the JLabel for JDBC URL field
     */
    protected JLabel getLblJdbcUrl() {
        if (lblJdbcUrl == null) {
            lblJdbcUrl = new JLabel("JDBC URL:");
        }

        return lblJdbcUrl;
    }

    /**
     * Gets or creates the JDBC URL text field.
     *
     * @return the JTextField for JDBC connection URL
     */
    protected JTextField getTxtJdbcUrl() {
        if (txtJdbcUrl == null) {
            txtJdbcUrl = new JTextField();
        }

        return txtJdbcUrl;
    }

    /**
     * Gets or creates the username label.
     *
     * @return the JLabel for username field
     */
    protected JLabel getLblUser() {
        if (lblUser == null) {
            lblUser = new JLabel("User name:");
        }

        return lblUser;
    }

    /**
     * Gets or creates the username text field.
     *
     * @return the JTextField for database username
     */
    protected JTextField getTxtUser() {
        if (txtUser == null) {
            txtUser = new JTextField();
        }

        return txtUser;
    }

    /**
     * Gets or creates the password label.
     *
     * @return the JLabel for password field
     */
    protected JLabel getLblPassword() {
        if (lblPassword == null) {
            lblPassword = new JLabel("Password:");
        }

        return lblPassword;
    }

    /**
     * Gets or creates the password field.
     *
     * @return the JPasswordField for database password
     */
    protected JPasswordField getTxtPassword() {
        if (txtPassword == null) {
            txtPassword = new JPasswordField();
        }

        return txtPassword;
    }

    /**
     * Gets or creates the driver class name label.
     *
     * @return the JLabel for driver class name field
     */
    protected JLabel getLblDriverClassName() {
        if (lblDriverClassName == null) {
            lblDriverClassName = new JLabel();
            lblDriverClassName.setText("Driver class name:");
        }

        return lblDriverClassName;
    }

    /**
     * Gets or creates the driver class name text field.
     *
     * @return the JTextField for JDBC driver class name
     */
    protected JTextField getTxtDriverClassName() {
        if (txtDriverClassName == null) {
            txtDriverClassName = new JTextField();
        }

        return txtDriverClassName;
    }

    /**
     * Gets or creates the SQL statement separator label.
     *
     * @return the JLabel for statement separator field
     */
    protected JLabel getLblStatementSeparator() {
        if (lblStatementSeparator == null) {
            lblStatementSeparator = new JLabel("SQL statement separator (for batch mode only):");
        }

        return lblStatementSeparator;
    }

    /**
     * Gets or creates the SQL statement separator text field.
     * Used for separating multiple SQL statements in batch execution mode.
     *
     * @return the JTextField for SQL statement separator
     */
    protected JTextField getTxtStatementSeparator() {
        if (txtStatementSeparator == null) {
            txtStatementSeparator = new JTextField();
            txtStatementSeparator.setToolTipText("Separator used to segregate statements to be executed one by one in a batch. Only valid for batch execution mode.");
        }

        return txtStatementSeparator;
    }

    /**
     * Gets or creates the connection color label.
     *
     * @return the JXLabel for color selection
     */
    protected JXLabel getTxtColor() {
        if (txtColor == null) {
            txtColor = new JXLabel("Connection color:");
        }
        return txtColor;
    }

    /**
     * Gets or creates the color selection button.
     * Allows users to choose a color for visual identification of the connection.
     *
     * @return the JXColorSelectionButton for color selection
     */
    protected JXColorSelectionButton getColorChooser() {
        if (this.colorChooser == null) {
            this.colorChooser = new JXColorSelectionButton();
            this.colorChooser.setText("Connection color");
        }

        return colorChooser;
    }

    /**
     * Gets or creates the quoted identifiers checkbox.
     * Controls whether SQL identifiers should be quoted in generated queries.
     *
     * @return the JCheckBox for quoted identifiers option
     */
    private JCheckBox getChkUseQuotedIdentifiers() {
        if (chkUseQuotedIdentifiers == null) {
            chkUseQuotedIdentifiers = new JCheckBox("Use quoted identfiers");
        }
        return chkUseQuotedIdentifiers;
    }

    /**
     * Gets or creates the toggle password visibility button.
     * Allows showing/hiding the password in plain text.
     *
     * @return the JToggleButton for toggling password visibility
     */
    protected JToggleButton getBtnTogglePassword() {
        if (btnTogglePassword == null) {
            btnTogglePassword = new JToggleButton();
            btnTogglePassword.setPreferredSize(new Dimension(32, 26));
            btnTogglePassword.setMinimumSize(new Dimension(32, 26));
            btnTogglePassword.setMaximumSize(new Dimension(32, 26));
            
            // Load icon from resources
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/icons/png_64/icons8_eye_checked_64px.png"));
                if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                    // Scale the icon to fit button
                    btnTogglePassword.setIcon(new ImageIcon(icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
                } else {
                    btnTogglePassword.setText("👁");
                }
            } catch (Exception ex) {
                // Fallback to text if icon not found
                btnTogglePassword.setText("👁");
            }
            
            btnTogglePassword.setToolTipText("Show/hide password");
            btnTogglePassword.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnTogglePassword.isSelected()) {
                        // Show password
                        getTxtPassword().setEchoChar((char) 0);
                    } else {
                        // Hide password
                        getTxtPassword().setEchoChar('•');
                    }
                }
            });
        }
        return btnTogglePassword;
    }

    /**
     * Gets or creates the copy password button.
     * Copies the password to system clipboard.
     *
     * @return the JButton for copying password to clipboard
     */
    protected JButton getBtnCopyPassword() {
        if (btnCopyPassword == null) {
            btnCopyPassword = new JButton();
            btnCopyPassword.setPreferredSize(new Dimension(32, 26));
            btnCopyPassword.setMinimumSize(new Dimension(32, 26));
            btnCopyPassword.setMaximumSize(new Dimension(32, 26));
            
            // Load icon from resources
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/icons/png_64/icons8_copy_64px_2.png"));
                if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                    // Scale the icon to fit button
                    btnCopyPassword.setIcon(new ImageIcon(icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
                } else {
                    btnCopyPassword.setText("📋");
                }
            } catch (Exception ex) {
                // Fallback to text if icon not found
                btnCopyPassword.setText("📋");
            }
            
            btnCopyPassword.setToolTipText("Copy password to clipboard");
            btnCopyPassword.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String password = String.copyValueOf(getTxtPassword().getPassword());
                    if (password != null && !password.isEmpty()) {
                        StringSelection stringSelection = new StringSelection(password);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                        JOptionPane.showMessageDialog(DbConfigPanel.this, 
                            "Password copied to clipboard!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }
        return btnCopyPassword;
    }
}
