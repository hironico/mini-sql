package net.hironico.minisql.ui.dataexport;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single step wizard like panel used to export a table or a view to a file. The user selects the columns,
 * the format (CSV, JSON, SQL INSERT), the field delimiter, the row separator, the date formats and the target file.
 * The data is streamed from the database to the file by {@link DataExporter}, it is never loaded in a result panel.
 */
public class DataExportPanel extends JRoundedPanel {

    private static final Logger LOGGER = Logger.getLogger(DataExportPanel.class.getName());

    private static final String TITLE = "Export data";

    private static final String[] DELIMITERS = {",", ";", "Tab", "|"};
    private static final String[] ENCODINGS = {"UTF-8", "ISO-8859-1", "windows-1252", "UTF-16"};
    private static final String[] ROW_SEPARATORS = {"CRLF (Windows)", "LF (Unix)", "CR (old Mac)"};

    /** Remembers the directory of the last export for the whole session */
    private static File lastUsedDirectory;

    private final DbConfig config;
    private final SQLObject object;

    private final ExportColumnTableModel columnsModel = new ExportColumnTableModel();

    private JComboBox<ExportFormat> cmbFormat;
    private JComboBox<String> cmbDelimiter;
    private JCheckBox chkHeader;
    private JCheckBox chkQuoteAll;
    private JComboBox<NullHandling> cmbNulls;
    private JCheckBox chkBom;
    private JCheckBox chkJsonLines;
    private JTextField txtTargetTable;
    private JSpinner spnRowsPerStatement;
    private JComboBox<String> cmbRowSeparator;
    private JComboBox<String> cmbEncoding;
    private JTextField txtDatePattern;
    private JTextField txtTimestampPattern;
    private JTextField txtTimePattern;
    private JLabel lblDateSample;
    private JSpinner spnFetchSize;
    private JTextField txtFile;
    private final List<JComponent> csvRows = new ArrayList<>();
    private final List<JComponent> jsonRows = new ArrayList<>();
    private final List<JComponent> sqlRows = new ArrayList<>();
    private final List<JLabel> optionLabels = new ArrayList<>();
    private JProgressBar progressBar;
    private JTextArea txtLog;
    private JButton btnExport;
    private JButton btnCancel;
    private JLabel lblStatus;

    /** Exporter of the running export, null when no export is running */
    private volatile DataExporter runningExporter;

    /**
     * Creates the export panel for the given table or view.
     *
     * @param config connection holding the object to export
     * @param object the table or view to export
     */
    public DataExportPanel(DbConfig config, SQLObject object) {
        super();
        this.config = config;
        this.object = object;
        initialize();
        loadColumns();
    }

    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(0, 10));

        add(createHeading(String.format("Export %s from connection %s", object, config.name)), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createColumnsPanel(), createOptionsPanel());
        split.setOpaque(false);
        split.setResizeWeight(0.4d);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
        updateFormatOptions();
        updateDateSample();
        updateButtons();
    }

    /**
     * Creates a heading label using the same style as the step titles of the import wizard.
     */
    private static JLabel createHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D() + 3f));
        return label;
    }

    private JPanel createColumnsPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        pnl.add(createHeading("1. Columns: select the columns to export"), BorderLayout.NORTH);

        JTable table = new JTable(columnsModel);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setMaxWidth(70);
        pnl.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlButtons.setOpaque(false);
        JButton btnAll = new JButton("Select all");
        btnAll.addActionListener(e -> columnsModel.selectAll(true));
        JButton btnNone = new JButton("Select none");
        btnNone.addActionListener(e -> columnsModel.selectAll(false));
        lblStatus = new JLabel(" ");
        pnlButtons.add(btnAll);
        pnlButtons.add(btnNone);
        pnlButtons.add(lblStatus);
        pnl.add(pnlButtons, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel createOptionsPanel() {
        createFormatControls();

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 5, 3, 5);

        gc.gridwidth = 2;
        pnl.add(createHeading("2. Format: choose the format and the options"), gc);
        gc.gridwidth = 1;

        addRow(pnl, gc, "Format:", getCmbFormat(), null);

        // options of each format, same grid and same look as the common options
        addRow(pnl, gc, "Field delimiter:", cmbDelimiter, csvRows);
        addRow(pnl, gc, "Header row:", chkHeader, csvRows);
        addRow(pnl, gc, "Null values:", cmbNulls, csvRows);
        addRow(pnl, gc, "Quoting:", chkQuoteAll, csvRows);
        addRow(pnl, gc, "JSON layout:", chkJsonLines, jsonRows);
        addRow(pnl, gc, "Target table:", txtTargetTable, sqlRows);
        addRow(pnl, gc, "Rows per INSERT:", spnRowsPerStatement, sqlRows);

        addRow(pnl, gc, "Row separator:", getCmbRowSeparator(), null);
        addRow(pnl, gc, "Encoding:", getCmbEncoding(), null);
        addRow(pnl, gc, "UTF-8 BOM:", chkBom, null);
        addRow(pnl, gc, "Date format:", getTxtDatePattern(), null);
        addRow(pnl, gc, "Date time format:", getTxtTimestampPattern(), null);
        addRow(pnl, gc, "Time format:", getTxtTimePattern(), null);
        addRow(pnl, gc, "Sample:", getLblDateSample(), null);
        addRow(pnl, gc, "Fetch size:", getSpnFetchSize(), null);

        // same width for all the labels, hidden ones included, so the controls do not move when the format changes
        int labelWidth = optionLabels.stream().mapToInt(l -> l.getPreferredSize().width).max().orElse(0);
        for (JLabel l : optionLabels) {
            l.setPreferredSize(new Dimension(labelWidth, l.getPreferredSize().height));
        }

        // the target file keeps its own full width line
        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        pnl.add(new JLabel("Target file:"), gc);

        gc.gridy++;
        JPanel pnlFile = new JPanel(new BorderLayout(5, 0));
        pnlFile.setOpaque(false);
        txtFile = new JTextField();
        txtFile.setToolTipText("Full path of the file to write. It is overwritten if it exists.");
        JButton btnBrowse = new JButton("Browse...");
        btnBrowse.addActionListener(e -> browseFile());
        pnlFile.add(txtFile, BorderLayout.CENTER);
        pnlFile.add(btnBrowse, BorderLayout.EAST);
        pnl.add(pnlFile, gc);

        gc.gridy++;
        gc.weighty = 1.0;
        pnl.add(Box.createVerticalGlue(), gc);
        return pnl;
    }

    /**
     * Adds a row to the options grid: the label in the first column and the control in the second one.
     *
     * @param formatRows list collecting the components of the rows that are specific to a format, so that they
     *                   can be shown or hidden together. Null for rows shown whatever the format.
     */
    private void addRow(JPanel pnl, GridBagConstraints gc, String label, JComponent comp,
                               List<JComponent> formatRows) {
        JLabel lbl = new JLabel(label);
        optionLabels.add(lbl);
        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0.0;
        pnl.add(lbl, gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        pnl.add(comp, gc);
        if (formatRows != null) {
            formatRows.add(lbl);
            formatRows.add(comp);
        }
    }


    private JComboBox<ExportFormat> getCmbFormat() {
        if (cmbFormat == null) {
            cmbFormat = new JComboBox<>(ExportFormat.values());
            cmbFormat.addActionListener(e -> {
                updateFormatOptions();
                updateTargetExtension();
            });
        }
        return cmbFormat;
    }

    /** Creates the controls of the format specific options. They are laid out by {@link #createOptionsPanel()}. */
    private void createFormatControls() {
        cmbDelimiter = new JComboBox<>(DELIMITERS);
        cmbDelimiter.setEditable(true);
        cmbDelimiter.setToolTipText("Field delimiter. Type any other single character if needed.");

        chkHeader = new JCheckBox("Write the column names in the first row", true);
        chkHeader.setOpaque(false);

        cmbNulls = new JComboBox<>(NullHandling.values());
        cmbNulls.setSelectedItem(NullHandling.EMPTY);
        cmbNulls.setToolTipText("What to write for NULL values. A NULL is never quoted.");

        chkQuoteAll = new JCheckBox("Quote every value with double quotes", true);
        chkQuoteAll.setOpaque(false);
        chkQuoteAll.setToolTipText("Keeps spreadsheets such as Excel from splitting values containing the delimiter.");

        chkJsonLines = new JCheckBox("JSON Lines: one object per line, no enclosing array", false);
        chkJsonLines.setOpaque(false);
        chkJsonLines.setToolTipText("Recommended for very large exports.");

        txtTargetTable = new JTextField(object.name, 16);
        txtTargetTable.setToolTipText("Table name written in the INSERT statements.");

        spnRowsPerStatement = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        spnRowsPerStatement.setToolTipText("Number of rows grouped in each INSERT statement.");

        chkBom = new JCheckBox("Add a byte order mark at the start of the file", false);
        chkBom.setOpaque(false);
        chkBom.setToolTipText("Lets Excel detect UTF-8. Only used with the UTF-8 encoding.");
    }


    private JComboBox<String> getCmbRowSeparator() {
        if (cmbRowSeparator == null) {
            cmbRowSeparator = new JComboBox<>(ROW_SEPARATORS);
            cmbRowSeparator.setSelectedIndex(0);
        }
        return cmbRowSeparator;
    }

    private JComboBox<String> getCmbEncoding() {
        if (cmbEncoding == null) {
            cmbEncoding = new JComboBox<>(ENCODINGS);
            cmbEncoding.setEditable(true);
        }
        return cmbEncoding;
    }

    private JTextField getTxtDatePattern() {
        if (txtDatePattern == null) {
            txtDatePattern = createPatternField(DataExporter.DEFAULT_DATE_PATTERN,
                    "Format of the DATE columns, java.time pattern.");
        }
        return txtDatePattern;
    }

    private JTextField getTxtTimestampPattern() {
        if (txtTimestampPattern == null) {
            txtTimestampPattern = createPatternField(DataExporter.DEFAULT_TIMESTAMP_PATTERN,
                    "Format of the TIMESTAMP / date time columns, java.time pattern.");
        }
        return txtTimestampPattern;
    }

    private JTextField getTxtTimePattern() {
        if (txtTimePattern == null) {
            txtTimePattern = createPatternField(DataExporter.DEFAULT_TIME_PATTERN,
                    "Format of the TIME columns, java.time pattern.");
        }
        return txtTimePattern;
    }

    private JTextField createPatternField(String value, String tooltip) {
        JTextField field = new JTextField(value, 20);
        field.setToolTipText(tooltip);
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateDateSample(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateDateSample(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateDateSample(); }
        });
        return field;
    }

    private JLabel getLblDateSample() {
        if (lblDateSample == null) {
            lblDateSample = new JLabel(" ");
        }
        return lblDateSample;
    }

    private JSpinner getSpnFetchSize() {
        if (spnFetchSize == null) {
            spnFetchSize = new JSpinner(new SpinnerNumberModel(5000, 1, 1000000, 1000));
            spnFetchSize.setToolTipText("Number of rows the JDBC driver reads from the database at once.");
        }
        return spnFetchSize;
    }

    /** Shows the rows of the selected format only, the grid closes the gaps left by the hidden rows. */
    private void updateFormatOptions() {
        ExportFormat selected = (ExportFormat) getCmbFormat().getSelectedItem();
        csvRows.forEach(c -> c.setVisible(selected == ExportFormat.CSV));
        jsonRows.forEach(c -> c.setVisible(selected == ExportFormat.JSON));
        sqlRows.forEach(c -> c.setVisible(selected == ExportFormat.SQL));
    }

    /** Previews the current time with the three patterns, or the error of the first invalid one. */
    private void updateDateSample() {
        if (txtDatePattern == null || txtTimestampPattern == null || txtTimePattern == null || lblDateSample == null) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            lblDateSample.setForeground(UIManager.getColor("Label.foreground"));
            lblDateSample.setText(String.join("  |  ",
                    DateTimeFormatter.ofPattern(txtDatePattern.getText()).format(now),
                    DateTimeFormatter.ofPattern(txtTimestampPattern.getText()).format(now),
                    DateTimeFormatter.ofPattern(txtTimePattern.getText()).format(now)));
        } catch (RuntimeException ex) {
            lblDateSample.setForeground(Color.RED);
            lblDateSample.setText("Invalid pattern: " + ex.getMessage());
        }
    }

    private JPanel createBottomPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString(" ");
        JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
        pnlTop.setOpaque(false);
        pnlTop.add(createHeading("3. Export: write the data to the file"), BorderLayout.NORTH);
        pnlTop.add(progressBar, BorderLayout.SOUTH);
        pnl.add(pnlTop, BorderLayout.NORTH);

        txtLog = new JTextArea(5, 40);
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txtLog.getFont().getSize()));
        pnl.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setOpaque(false);
        btnExport = new JButton("Export");
        btnExport.addActionListener(e -> startExport());
        btnCancel = new JButton("Cancel");
        btnCancel.setToolTipText("Stop the running export. The incomplete file is deleted.");
        btnCancel.addActionListener(e -> {
            DataExporter exporter = runningExporter;
            if (exporter != null) {
                exporter.cancel();
            }
        });
        pnlButtons.add(btnExport);
        pnlButtons.add(btnCancel);
        pnl.add(pnlButtons, BorderLayout.SOUTH);
        return pnl;
    }

    private void updateButtons() {
        boolean running = runningExporter != null;
        btnExport.setEnabled(!running);
        btnCancel.setEnabled(running);
    }

    private void appendLog(String message) {
        txtLog.append(message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, TITLE, JOptionPane.ERROR_MESSAGE);
    }

    private boolean isQuoted() {
        return Boolean.TRUE.equals(config.useQuotedIdentifiers);
    }

    /**
     * Reads the columns of the object in the background, using an empty SELECT so that it works the same
     * way for tables and views whatever the database.
     */
    private void loadColumns() {
        lblStatus.setText("Loading columns...");
        final String qualified = new DataExporter(object.schemaName, object.name, List.of("*"), isQuoted()).getQualifiedName();
        new SwingWorker<List<ExportColumn>, Void>() {
            @Override
            protected List<ExportColumn> doInBackground() throws Exception {
                List<ExportColumn> cols = new ArrayList<>();
                try (Connection conn = config.getConnection();
                     java.sql.Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + qualified + " WHERE 1 = 0")) {
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        cols.add(new ExportColumn(md.getColumnName(i), md.getColumnTypeName(i), md.getColumnType(i),
                                md.getPrecision(i), md.getScale(i),
                                md.isNullable(i) != java.sql.ResultSetMetaData.columnNoNulls));
                    }
                }
                return cols;
            }

            @Override
            protected void done() {
                try {
                    List<ExportColumn> cols = get();
                    columnsModel.setColumns(cols);
                    lblStatus.setText(cols.size() + " columns");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "Cannot read the columns of " + object, ex.getCause());
                    lblStatus.setText("Cannot read columns");
                    showError("Cannot read the columns of " + object + ":\n" + ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Select the file to write");
        File current = new File(txtFile.getText().trim());
        if (!txtFile.getText().isBlank()) {
            chooser.setSelectedFile(current);
        } else if (lastUsedDirectory != null) {
            chooser.setCurrentDirectory(lastUsedDirectory);
            chooser.setSelectedFile(new File(lastUsedDirectory, defaultFileName()));
        } else {
            chooser.setSelectedFile(new File(defaultFileName()));
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastUsedDirectory = file.getParentFile();
            txtFile.setText(file.getAbsolutePath());
        }
    }

    private String defaultFileName() {
        return object.name + "." + ((ExportFormat) getCmbFormat().getSelectedItem()).getExtension();
    }

    /** Replaces the extension of the target file when the format changes, if a file name was already chosen. */
    private void updateTargetExtension() {
        if (txtFile == null || txtFile.getText().isBlank()) {
            return;
        }
        String path = txtFile.getText().trim();
        int dot = path.lastIndexOf('.');
        int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String base = dot > sep ? path.substring(0, dot) : path;
        txtFile.setText(base + "." + ((ExportFormat) getCmbFormat().getSelectedItem()).getExtension());
    }

    private static String toRowSeparator(int index) {
        return switch (index) {
            case 1 -> "\n";
            case 2 -> "\r";
            default -> DataExporter.CRLF;
        };
    }

    private static char parseDelimiter(String text) {
        String value = text == null ? "" : text;
        if ("Tab".equalsIgnoreCase(value.trim()) || "\\t".equals(value)) {
            return '\t';
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("The field delimiter cannot be empty.");
        }
        return value.charAt(0);
    }

    /**
     * Builds the exporter from the options of the panel.
     *
     * @throws IllegalArgumentException with a message for the user when an option is invalid
     */
    private DataExporter createExporter() {
        List<String> selected = columnsModel.getSelectedColumnNames();
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one column to export.");
        }

        DataExporter exporter = new DataExporter(object.schemaName, object.name, selected, isQuoted());
        exporter.setFormat((ExportFormat) getCmbFormat().getSelectedItem());
        exporter.setRowSeparator(toRowSeparator(getCmbRowSeparator().getSelectedIndex()));
        exporter.setFetchSize((Integer) getSpnFetchSize().getValue());

        try {
            exporter.setCharset(Charset.forName(String.valueOf(getCmbEncoding().getSelectedItem()).trim()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown file encoding: " + getCmbEncoding().getSelectedItem());
        }

        try {
            exporter.setDatePatterns(getTxtDatePattern().getText(), getTxtTimestampPattern().getText(),
                    getTxtTimePattern().getText());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid date format: " + ex.getMessage());
        }

        exporter.setDelimiter(parseDelimiter(String.valueOf(cmbDelimiter.getSelectedItem())));
        exporter.setHeader(chkHeader.isSelected());
        exporter.setQuoteAll(chkQuoteAll.isSelected());
        exporter.setNullHandling((NullHandling) cmbNulls.getSelectedItem());
        exporter.setUtf8Bom(chkBom.isSelected());
        exporter.setJsonLines(chkJsonLines.isSelected());
        exporter.setTargetTable(txtTargetTable.getText());
        exporter.setRowsPerStatement((Integer) spnRowsPerStatement.getValue());
        return exporter;
    }

    private void startExport() {
        if (runningExporter != null) {
            return;
        }

        final DataExporter exporter;
        try {
            exporter = createExporter();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        String path = txtFile.getText().trim();
        if (path.isEmpty()) {
            showError("Please choose the file to write.");
            return;
        }
        final File file = new File(path);
        File dir = file.getAbsoluteFile().getParentFile();
        if (dir == null || !dir.isDirectory()) {
            showError("The target directory does not exist: " + dir);
            return;
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "The file " + file.getName() + " already exists. Overwrite it?",
                TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        runningExporter = exporter;
        updateButtons();
        txtLog.setText("");
        progressBar.setIndeterminate(true);
        progressBar.setString("0 rows");
        appendLog(DataExporter.stamp(String.format("Exporting %s from connection %s", object, config.name)));

        final long startNanos = System.nanoTime();
        DataExporter.ExportListener listener = new DataExporter.ExportListener() {
            @Override
            public void progress(long rowsExported) {
                double seconds = (System.nanoTime() - startNanos) / 1_000_000_000d;
                double rate = seconds > 0 ? rowsExported / seconds : 0;
                SwingUtilities.invokeLater(() -> progressBar.setString(String.format("%,d rows - %,.0f rows/s", rowsExported, rate)));
            }

            @Override
            public void log(String message) {
                SwingUtilities.invokeLater(() -> appendLog(message));
            }
        };

        new SwingWorker<DataExporter.ExportResult, Void>() {
            @Override
            protected DataExporter.ExportResult doInBackground() throws Exception {
                try (Connection conn = config.getConnection()) {
                    return exporter.runExport(conn, file, listener);
                }
            }

            @Override
            protected void done() {
                runningExporter = null;
                progressBar.setIndeterminate(false);
                updateButtons();
                try {
                    DataExporter.ExportResult result = get();
                    progressBar.setValue(result.cancelled() ? 0 : 100);
                    String msg = result.cancelled()
                            ? DataExporter.summarize("Export cancelled", result) + ".\nThe incomplete file was deleted."
                            : DataExporter.summarize("Export finished", result) + " to\n" + file.getAbsolutePath();
                    JOptionPane.showMessageDialog(DataExportPanel.this, msg, TITLE, JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    LOGGER.log(Level.SEVERE, "Data export failed.", cause);
                    appendLog(DataExporter.stamp("ERROR: " + cause.getMessage()));
                    showError("The export failed:\n" + cause.getMessage());
                }
            }
        }.execute();
    }
}
