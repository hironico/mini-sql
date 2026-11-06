package net.hironico.minisql.ui.dbexplorer;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.SchemaModelImporter;
import net.hironico.minisql.model.schema.SchemaDdlGenerator;
import net.hironico.minisql.model.schema.SchemaModel;
import net.hironico.minisql.model.schema.SchemaModelReader;
import net.hironico.minisql.model.schema.SqlDialect;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel used to re-create into a target connection the tables of a JSON model written by "Export Model":
 * columns, primary keys, indexes and foreign keys. The DDL script can be previewed, opened in a query
 * editor or executed directly.
 */
public class ModelImportPanel extends JRoundedPanel implements DbConfigFile.DbConfigFileListener {

    private static final Logger LOGGER = Logger.getLogger(ModelImportPanel.class.getName());

    /** Last directory used to open a model, shared between panels */
    private static File lastDirectory = null;

    private SchemaModel currentModel;
    private File modelFile;

    private JTextField txtFile;
    private JLabel lblSource;
    private JComboBox<SchemaModel> cmbSchema;
    private TablesTableModel tablesModel;

    private DefaultComboBoxModel<DbConfig> connectionModel;
    private JComboBox<DbConfig> cmbConnection;
    private JComboBox<SqlDialect> cmbDialect;
    private JTextField txtTargetSchema;
    private JCheckBox chkQuote;
    private JCheckBox chkDrop;
    private JCheckBox chkPrimaryKeys;
    private JCheckBox chkIndexes;
    private JCheckBox chkForeignKeys;
    private JCheckBox chkDefaults;
    private JCheckBox chkKeepNames;
    private JCheckBox chkContinueOnError;

    private RSyntaxTextArea txtScript;
    private JLabel lblSummary;
    private JProgressBar progressBar;
    private JTextArea txtLog;
    private JButton btnExecute;
    private JButton btnCancel;
    private JButton btnOpenInEditor;

    private volatile SchemaModelImporter runningImporter;

    public ModelImportPanel() {
        super();
        initialize();
        DbConfigFile.addListener(this);
    }

    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(0, 5));

        add(createSourcePanel(), BorderLayout.NORTH);

        JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablesPanel(), createTargetPanel());
        splitLeft.setResizeWeight(0.6d);
        splitLeft.setBorder(BorderFactory.createEmptyBorder());
        splitLeft.setOpaque(false);

        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createScriptPanel(), createLogPanel());
        splitRight.setResizeWeight(0.75d);
        splitRight.setBorder(BorderFactory.createEmptyBorder());
        splitRight.setOpaque(false);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeft, splitRight);
        splitMain.setResizeWeight(0.4d);
        splitMain.setBorder(BorderFactory.createEmptyBorder());
        splitMain.setOpaque(false);
        add(splitMain, BorderLayout.CENTER);

        add(createButtonsPanel(), BorderLayout.SOUTH);
        updateScript();
    }

    // ------------------------------------------------------------------ source: model file

    private JPanel createSourcePanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 2, 2, 2);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0;
        gc.gridy = 0;
        pnl.add(new JLabel("Model file:"), gc);

        txtFile = new JTextField();
        txtFile.setEditable(false);
        gc.gridx = 1;
        gc.weightx = 1.0;
        pnl.add(txtFile, gc);

        JButton btnBrowse = new JButton("Open...");
        btnBrowse.addActionListener(e -> chooseModelFile());
        gc.gridx = 2;
        gc.weightx = 0.0;
        pnl.add(btnBrowse, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        pnl.add(new JLabel("Schema:"), gc);

        cmbSchema = new JComboBox<>();
        cmbSchema.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                String text = value instanceof SchemaModel m
                        ? String.format("%s (%d objects)", m.schema(), m.tables().size()) : " ";
                return super.getListCellRendererComponent(list, text, index, selected, focus);
            }
        });
        cmbSchema.addActionListener(e -> selectSchema((SchemaModel) cmbSchema.getSelectedItem()));
        gc.gridx = 1;
        gc.gridwidth = 2;
        pnl.add(cmbSchema, gc);

        lblSource = new JLabel("Open a JSON model written by the Export Model action.");
        gc.gridx = 1;
        gc.gridy = 2;
        pnl.add(lblSource, gc);
        return pnl;
    }

    private void chooseModelFile() {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Open a model exported by MiniSQL");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadModel(chooser.getSelectedFile());
        }
    }

    /**
     * Loads the model file and displays its tables.
     *
     * @param file the JSON model file
     */
    public void loadModel(File file) {
        try {
            List<SchemaModel> loaded = SchemaModelReader.load(file);
            lastDirectory = file.getParentFile();
            modelFile = file;
            txtFile.setText(file.getAbsolutePath());
            cmbSchema.setModel(new DefaultComboBoxModel<>(loaded.toArray(new SchemaModel[0])));
            cmbSchema.setEnabled(loaded.size() > 1);
            selectSchema(loaded.get(0));
            MainWindow.getInstance().setEditorTabTitle(this, "Import Model: " + file.getName());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot load model " + file, ex);
            JOptionPane.showMessageDialog(this, "Cannot load the model:\n" + ex.getMessage(),
                    "Import Model", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectSchema(SchemaModel model) {
        if (model == null || model == currentModel) {
            return;
        }
        currentModel = model;
        SchemaModel.Source src = model.source();
        lblSource.setText(src == null ? "Unknown source database"
                : String.format("Exported from %s (%s %s) on %s", src.connection(), src.product(),
                Objects.toString(src.productVersion(), ""), Objects.toString(model.exportDate(), "?")));
        tablesModel.setModel(model);
        if (txtTargetSchema.getText().isBlank() && model.schema() != null) {
            txtTargetSchema.setText(model.schema());
        }
        updateScript();
    }

    // ------------------------------------------------------------------ tables list

    private JPanel createTablesPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 2));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createTitledBorder("Tables to create"));

        tablesModel = new TablesTableModel();
        tablesModel.addTableModelListener(e -> updateScript());
        JTable tbl = new JTable(tablesModel);
        tbl.setAutoCreateRowSorter(true);
        tbl.setFillsViewportHeight(true);
        tbl.getColumnModel().getColumn(0).setMaxWidth(40);
        pnl.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        buttons.setOpaque(false);
        JButton btnAll = new JButton("All");
        btnAll.addActionListener(e -> tablesModel.selectAll(true));
        JButton btnNone = new JButton("None");
        btnNone.addActionListener(e -> tablesModel.selectAll(false));
        buttons.add(btnAll);
        buttons.add(btnNone);
        buttons.add(new JLabel("Views are listed for information only: their query is not part of the model."));
        pnl.add(buttons, BorderLayout.SOUTH);
        return pnl;
    }

    // ------------------------------------------------------------------ target: connection and options

    private JPanel createTargetPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createTitledBorder("Target"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 2, 2, 2);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        connectionModel = new DefaultComboBoxModel<>();
        DbConfigFile.getConfigNames().stream().map(DbConfigFile::getConfig)
                .filter(cfg -> cfg != null && !cfg.isMongoDB()).forEach(connectionModel::addElement);
        cmbConnection = new JComboBox<>(connectionModel);
        cmbConnection.addActionListener(e -> {
            DbConfig cfg = (DbConfig) cmbConnection.getSelectedItem();
            if (cfg != null) {
                chkQuote.setSelected(Boolean.TRUE.equals(cfg.useQuotedIdentifiers));
                cmbDialect.setSelectedItem(guessDialect(cfg));
            }
            updateScript();
        });

        cmbDialect = new JComboBox<>(SqlDialect.values());
        cmbDialect.setToolTipText("Database type of the target, used to choose the column types. Detected from the connection URL.");
        cmbDialect.addActionListener(e -> updateScript());

        txtTargetSchema = new JTextField(12);
        txtTargetSchema.setToolTipText("Schema where the tables are created. Leave empty for the default schema of the connection.");
        txtTargetSchema.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateScript(); }
            public void removeUpdate(DocumentEvent e) { updateScript(); }
            public void changedUpdate(DocumentEvent e) { updateScript(); }
        });

        addRow(pnl, gc, 0, "Connection:", cmbConnection);
        addRow(pnl, gc, 1, "Database type:", cmbDialect);
        addRow(pnl, gc, 2, "Target schema:", txtTargetSchema);

        chkPrimaryKeys = option("Primary keys", true, null);
        chkIndexes = option("Indexes", true, null);
        chkForeignKeys = option("Foreign keys", true, "Foreign keys are created after all the tables.");
        chkKeepNames = option("Keep constraint and index names", true,
                "Uncheck to let the database name the constraints, for instance when they already exist in the target database.");
        chkQuote = option("Quote identifiers", false, "Keep the case of the names (\"MyTable\").");
        chkDefaults = option("Column default values", false,
                "Default values are copied as is: they may be specific to the source database.");
        chkDrop = option("Drop the tables first", false, "Existing tables are dropped, with their data.");
        chkContinueOnError = option("Continue on error", false, null);

        JPanel options = new JPanel(new GridLayout(0, 2, 5, 0));
        options.setOpaque(false);
        for (JCheckBox chk : List.of(chkPrimaryKeys, chkIndexes, chkForeignKeys, chkKeepNames,
                chkQuote, chkDefaults, chkDrop, chkContinueOnError)) {
            options.add(chk);
        }
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 2;
        gc.weightx = 1.0;
        pnl.add(options, gc);

        gc.gridy = 4;
        gc.weighty = 1.0;
        pnl.add(Box.createVerticalGlue(), gc);

        // all the components exist now: apply the first connection settings (quote, dialect)
        DbConfig first = (DbConfig) cmbConnection.getSelectedItem();
        if (first != null) {
            chkQuote.setSelected(Boolean.TRUE.equals(first.useQuotedIdentifiers));
            cmbDialect.setSelectedItem(guessDialect(first));
        }
        return pnl;
    }

    private static void addRow(JPanel pnl, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0.0;
        pnl.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        pnl.add(comp, gc);
    }

    private JCheckBox option(String label, boolean selected, String tooltip) {
        JCheckBox chk = new JCheckBox(label, selected);
        chk.setOpaque(false);
        chk.setToolTipText(tooltip);
        chk.addActionListener(e -> updateScript());
        return chk;
    }

    /** Guess the target dialect from the JDBC URL, without connecting to the database */
    static SqlDialect guessDialect(DbConfig cfg) {
        String url = cfg.jdbcUrl == null ? "" : cfg.jdbcUrl.toLowerCase(java.util.Locale.ROOT);
        if (url.startsWith("jdbc:postgresql")) {
            return SqlDialect.POSTGRESQL;
        }
        if (url.startsWith("jdbc:oracle")) {
            return SqlDialect.ORACLE;
        }
        if (url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:jtds:sqlserver")) {
            return SqlDialect.SQLSERVER;
        }
        if (url.startsWith("jdbc:sybase") || url.startsWith("jdbc:jtds:sybase")) {
            return SqlDialect.SYBASE;
        }
        if (url.startsWith("jdbc:mysql") || url.startsWith("jdbc:mariadb")) {
            return SqlDialect.MYSQL;
        }
        if (url.startsWith("jdbc:h2")) {
            return SqlDialect.H2;
        }
        return SqlDialect.GENERIC;
    }

    private SchemaDdlGenerator.Options getOptions() {
        SchemaDdlGenerator.Options opt = new SchemaDdlGenerator.Options();
        opt.dialect = (SqlDialect) cmbDialect.getSelectedItem();
        opt.targetSchema = txtTargetSchema.getText();
        opt.quoteIdentifiers = chkQuote.isSelected();
        opt.dropTables = chkDrop.isSelected();
        opt.defaultValues = chkDefaults.isSelected();
        opt.primaryKeys = chkPrimaryKeys.isSelected();
        opt.indexes = chkIndexes.isSelected();
        opt.foreignKeys = chkForeignKeys.isSelected();
        opt.keepConstraintNames = chkKeepNames.isSelected();
        return opt;
    }

    // ------------------------------------------------------------------ script preview and execution

    private JPanel createScriptPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 2));
        pnl.setOpaque(false);
        lblSummary = new JLabel(" ");
        pnl.add(lblSummary, BorderLayout.NORTH);
        txtScript = new RSyntaxTextArea(20, 60);
        txtScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        txtScript.setEditable(false);
        txtScript.setCodeFoldingEnabled(true);
        pnl.add(new RTextScrollPane(txtScript), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createLogPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 2));
        pnl.setOpaque(false);
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(" ");
        pnl.add(progressBar, BorderLayout.NORTH);
        txtLog = new JTextArea(6, 60);
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txtLog.getFont().getSize()));
        pnl.add(new JScrollPane(txtLog), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createButtonsPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnl.setOpaque(false);
        btnOpenInEditor = new JButton("Open in query editor");
        btnOpenInEditor.setToolTipText("Open the script in a new query editor to adapt it before running it.");
        btnOpenInEditor.addActionListener(e -> openInEditor());
        btnExecute = new JButton("Create tables");
        btnExecute.addActionListener(e -> startImport());
        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(e -> {
            SchemaModelImporter importer = runningImporter;
            if (importer != null) {
                importer.cancel();
                btnCancel.setEnabled(false);
                appendLog("Cancel requested, stopping after the current statement...");
            }
        });
        pnl.add(btnOpenInEditor);
        pnl.add(btnExecute);
        pnl.add(btnCancel);
        return pnl;
    }

    private SchemaDdlGenerator createGenerator() {
        if (currentModel == null || cmbDialect == null) {
            return null;
        }
        return new SchemaDdlGenerator(currentModel, tablesModel.getSelectedTables(), getOptions());
    }

    private String getStatementSeparator() {
        DbConfig cfg = (DbConfig) cmbConnection.getSelectedItem();
        SqlDialect dialect = (SqlDialect) cmbDialect.getSelectedItem();
        boolean batch = dialect == SqlDialect.SYBASE || dialect == SqlDialect.SQLSERVER;
        return batch && cfg != null && cfg.batchStatementSeparator != null && !cfg.batchStatementSeparator.isBlank()
                ? cfg.batchStatementSeparator : ";";
    }

    private void updateScript() {
        if (txtScript == null || lblSummary == null || btnExecute == null) {
            return;
        }
        SchemaDdlGenerator gen = createGenerator();
        boolean running = runningImporter != null;
        if (gen == null) {
            txtScript.setText("-- Open a model file to preview the script.");
            lblSummary.setText(" ");
            btnExecute.setEnabled(false);
            btnOpenInEditor.setEnabled(false);
            return;
        }
        List<SchemaDdlGenerator.Statement> statements = gen.generate();
        StringBuilder header = new StringBuilder();
        header.append(String.format("-- Model %s, schema %s, exported from %s%n", modelFile == null ? "" : modelFile.getName(),
                currentModel.schema(), currentModel.source() == null ? "?" : currentModel.source().product()));
        header.append(String.format("-- Target dialect: %s%n", cmbDialect.getSelectedItem()));
        gen.getSkippedForeignKeys().forEach(fk ->
                header.append("-- Skipped foreign key (referenced table not selected): ").append(fk).append('\n'));
        header.append('\n');

        txtScript.setText(header + SchemaDdlGenerator.toScript(statements, getStatementSeparator()));
        txtScript.setCaretPosition(0);
        lblSummary.setText(statements.isEmpty() ? "Nothing to create: select at least one table."
                : "Script: " + SchemaDdlGenerator.summary(statements));
        btnExecute.setEnabled(!running && !statements.isEmpty() && cmbConnection.getSelectedItem() != null);
        btnOpenInEditor.setEnabled(!statements.isEmpty());
    }

    private void openInEditor() {
        QueryPanel pnl = new QueryPanel();
        DbConfig cfg = (DbConfig) cmbConnection.getSelectedItem();
        if (cfg != null) {
            pnl.setConfig(cfg);
        }
        pnl.setQueryText(txtScript.getText());
        MainWindow.getInstance().addNewEditorTab(pnl, cfg == null ? "Import model script" : cfg.name);
    }

    private void appendLog(String message) {
        txtLog.append(message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void startImport() {
        DbConfig cfg = (DbConfig) cmbConnection.getSelectedItem();
        SchemaDdlGenerator gen = createGenerator();
        if (cfg == null || gen == null) {
            return;
        }
        List<SchemaDdlGenerator.Statement> statements = gen.generate();
        if (statements.isEmpty()) {
            return;
        }

        String message = String.format("Execute %s on connection %s?", SchemaDdlGenerator.summary(statements), cfg.name);
        if (chkDrop.isSelected()) {
            message += "\n\nWARNING: the existing tables will be dropped with all their data.";
        }
        int answer = JOptionPane.showConfirmDialog(this, message, "Import Model", JOptionPane.YES_NO_OPTION,
                chkDrop.isSelected() ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        SchemaModelImporter importer = new SchemaModelImporter(statements);
        importer.setContinueOnError(chkContinueOnError.isSelected());
        runningImporter = importer;
        btnExecute.setEnabled(false);
        btnCancel.setEnabled(true);
        txtLog.setText("");
        progressBar.setMaximum(statements.size());
        progressBar.setValue(0);
        progressBar.setString(String.format("0 / %d statements", statements.size()));
        appendLog(String.format("Creating %s on %s...", SchemaDdlGenerator.summary(statements), cfg.name));

        SchemaModelImporter.ImportListener listener = (done, total, st, error) -> SwingUtilities.invokeLater(() -> {
            progressBar.setValue(done);
            progressBar.setString(String.format("%d / %d statements", done, total));
            String what = st.phase() + " " + st.tableName();
            if (error == null) {
                appendLog("OK     " + what);
            } else if (st.phase() == SchemaDdlGenerator.Phase.DROP) {
                appendLog("IGNORE " + what + ": " + error.getMessage());
            } else {
                appendLog("ERROR  " + what + ": " + error.getMessage() + "\n       " + st.sql().replace("\n", " "));
            }
        });

        new SwingWorker<SchemaModelImporter.ImportResult, Void>() {
            @Override
            protected SchemaModelImporter.ImportResult doInBackground() throws Exception {
                try (Connection conn = cfg.getConnection()) {
                    return importer.run(conn, listener);
                }
            }

            @Override
            protected void done() {
                runningImporter = null;
                btnCancel.setEnabled(false);
                updateScript();
                try {
                    showResult(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "Model import failed.", ex.getCause());
                    appendLog("ERROR: " + ex.getCause().getMessage());
                    JOptionPane.showMessageDialog(ModelImportPanel.this, "The import failed:\n" + ex.getCause().getMessage(),
                            "Import Model", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showResult(SchemaModelImporter.ImportResult result) {
        long errors = result.failures().stream()
                .filter(f -> f.statement().phase() != SchemaDdlGenerator.Phase.DROP).count();
        String status = result.cancelled() ? "Import cancelled"
                : result.stopped() ? "Import stopped on error" : "Import finished";
        String summary = String.format("%s: %d statement(s) executed, %d error(s).", status, result.executed(), errors);
        appendLog(summary);
        JOptionPane.showMessageDialog(this,
                summary + (errors > 0 ? "\nSee the log for the details." : "\nRefresh the explorer to see the new tables."),
                "Import Model", errors > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------ DbConfigFileListener

    @Override
    public void configAdded(DbConfig config) {
        if (config != null && !config.isMongoDB()) {
            connectionModel.addElement(config);
        }
    }

    @Override
    public void configRemoved(DbConfig config) {
        connectionModel.removeElement(config);
    }

    // ------------------------------------------------------------------ tables table model

    /** Tables of the model with a check box. Views are displayed but cannot be selected. */
    private static class TablesTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"", "Name", "Type", "Columns", "Indexes", "Foreign keys"};
        private final List<String> names = new ArrayList<>();
        private final List<Boolean> selected = new ArrayList<>();
        private SchemaModel model;

        void setModel(SchemaModel model) {
            this.model = model;
            names.clear();
            selected.clear();
            model.tables().forEach((name, table) -> {
                names.add(name);
                selected.add(SchemaDdlGenerator.isTable(table));
            });
            fireTableDataChanged();
        }

        List<String> getSelectedTables() {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                if (selected.get(i)) {
                    result.add(names.get(i));
                }
            }
            return result;
        }

        void selectAll(boolean value) {
            if (model == null) {
                return;
            }
            for (int i = 0; i < names.size(); i++) {
                selected.set(i, value && SchemaDdlGenerator.isTable(model.tables().get(names.get(i))));
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return names.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 0 -> Boolean.class;
                case 3, 4, 5 -> Integer.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0 && SchemaDdlGenerator.isTable(model.tables().get(names.get(row)));
        }

        @Override
        public Object getValueAt(int row, int column) {
            SchemaModel.Table t = model.tables().get(names.get(row));
            return switch (column) {
                case 0 -> selected.get(row);
                case 1 -> names.get(row);
                case 2 -> t.type();
                case 3 -> t.columns() == null ? 0 : t.columns().size();
                case 4 -> t.indexes() == null ? 0 : (int) t.indexes().stream().filter(i -> !i.primaryKey()).count();
                case 5 -> t.foreignKeys() == null ? 0 : t.foreignKeys().size();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 0) {
                selected.set(row, Boolean.TRUE.equals(value));
                fireTableRowsUpdated(row, row);
            }
        }
    }
}
