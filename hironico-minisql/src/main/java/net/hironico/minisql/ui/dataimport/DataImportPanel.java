package net.hironico.minisql.ui.dataimport;

import net.hironico.common.swing.JRoundedPanel;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wizard like panel used to import the content of a CSV file into a new database table.
 * Steps:
 * <ol>
 *     <li>Source: select the connection and the CSV file to load</li>
 *     <li>Preview: check the file content and the header row holding the column names</li>
 *     <li>Table structure: enter the table name and the SQL data type of each column</li>
 *     <li>Import: create the table and import the data by batches of configurable size</li>
 * </ol>
 */
public class DataImportPanel extends JRoundedPanel implements DbConfigFile.DbConfigFileListener {

    private static final Logger LOGGER = Logger.getLogger(DataImportPanel.class.getName());

    /** Number of CSV records displayed in the preview step */
    public static final int PREVIEW_ROWS = 100;

    /** SQL data types proposed in the table structure step. Any other type can be typed in. */
    public static final String[] SQL_TYPES = {
            "VARCHAR(50)", "VARCHAR(150)", "VARCHAR(255)", "VARCHAR(1000)", "VARCHAR(4000)",
            "CHAR(1)", "CHAR(10)",
            "SMALLINT", "INTEGER", "BIGINT",
            "NUMERIC(10,0)", "NUMERIC(18,2)", "DECIMAL(18,4)", "DOUBLE PRECISION",
            "DATE", "TIME", "TIMESTAMP",
            "BOOLEAN", "CLOB", "TEXT"
    };

    private static final String[] STEP_TITLES = {
            "1. Source: select the connection and the CSV file",
            "2. Preview: check the file content and the header row",
            "3. Table structure: name the table and choose the data types",
            "4. Import: create the table and load the data"
    };

    private static final String[] DELIMITERS = {",", ";", "Tab", "|"};

    private static final String[] ENCODINGS = {"UTF-8", "ISO-8859-1", "windows-1252", "UTF-16"};

    private int currentStep = 0;

    private JLabel lblStepTitle;
    private CardLayout cardLayout;
    private JPanel pnlCards;
    private JButton btnBack;
    private JButton btnNext;
    private JButton btnImport;
    private JButton btnCancel;

    // step 1: source
    private DefaultComboBoxModel<DbConfig> cmbConnectionModel;
    private JComboBox<DbConfig> cmbConnection;
    private JTextField txtFile;
    private JButton btnBrowse;
    private JComboBox<String> cmbDelimiter;
    private JComboBox<String> cmbEncoding;
    private JCheckBox chkHeader;
    private File lastUsedDirectory;

    // step 2: preview
    private JLabel lblPreviewInfo;
    private DefaultTableModel previewModel;
    private JTable tblPreview;
    private CsvFileReader.Preview preview;
    private String previewSignature;

    // step 3: table structure
    private JTextField txtSchema;
    private JTextField txtTableName;
    private ImportColumnTableModel columnsModel;
    private JTable tblColumns;
    private JComboBox<String> cmbBulkType;
    private JTextField txtDatePattern;
    private JTextField txtTimestampPattern;
    private RSyntaxTextArea txtCreateSql;
    private String columnsSignature;

    // step 4: import
    private JSpinner spnBatchSize;
    private JCheckBox chkCreateTable;
    private JCheckBox chkDropTable;
    private JProgressBar progressBar;
    private JTextArea txtLog;
    private long totalRecords = -1;

    /** Importer of the running import, null when no import is running */
    private volatile CsvTableImporter runningImporter;

    /**
     * Creates a new data import wizard panel.
     */
    public DataImportPanel() {
        super();
        initialize();
        DbConfigFile.addListener(this);
    }

    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(0, 10));

        add(getLblStepTitle(), BorderLayout.NORTH);
        add(getPnlCards(), BorderLayout.CENTER);
        add(createNavigationBar(), BorderLayout.SOUTH);

        showStep(0);
    }

    // ------------------------------------------------------------------ navigation

    private JLabel getLblStepTitle() {
        if (lblStepTitle == null) {
            lblStepTitle = new JLabel();
            lblStepTitle.setFont(lblStepTitle.getFont().deriveFont(Font.BOLD, lblStepTitle.getFont().getSize2D() + 3f));
        }
        return lblStepTitle;
    }

    private JPanel getPnlCards() {
        if (pnlCards == null) {
            cardLayout = new CardLayout();
            pnlCards = new JPanel(cardLayout);
            pnlCards.setOpaque(false);
            pnlCards.add(createSourceStep(), "0");
            pnlCards.add(createPreviewStep(), "1");
            pnlCards.add(createStructureStep(), "2");
            pnlCards.add(createImportStep(), "3");
        }
        return pnlCards;
    }

    private JPanel createNavigationBar() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnl.setOpaque(false);

        btnBack = new JButton("< Back");
        btnBack.addActionListener(e -> showStep(currentStep - 1));

        btnNext = new JButton("Next >");
        btnNext.addActionListener(e -> goNext());

        btnImport = new JButton("Import");
        btnImport.addActionListener(e -> startImport());

        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.setToolTipText("Stop the running import after the current batch.");
        btnCancel.addActionListener(e -> cancelImport());

        pnl.add(btnBack);
        pnl.add(btnNext);
        pnl.add(btnImport);
        pnl.add(btnCancel);
        return pnl;
    }

    private void showStep(int step) {
        currentStep = Math.max(0, Math.min(step, STEP_TITLES.length - 1));
        cardLayout.show(getPnlCards(), String.valueOf(currentStep));
        getLblStepTitle().setText(STEP_TITLES[currentStep]);
        updateButtons();
    }

    private void updateButtons() {
        boolean running = runningImporter != null;
        btnBack.setEnabled(!running && currentStep > 0);
        btnNext.setEnabled(!running && currentStep < STEP_TITLES.length - 1);
        btnImport.setVisible(currentStep == STEP_TITLES.length - 1);
        btnImport.setEnabled(!running);
        btnCancel.setVisible(currentStep == STEP_TITLES.length - 1);
        btnCancel.setEnabled(running);
    }

    /**
     * Validates the current step and moves to the next one.
     */
    private void goNext() {
        switch (currentStep) {
            case 0 -> {
                if (validateSource()) {
                    loadPreview();
                }
            }
            case 1 -> {
                prepareColumns();
                showStep(2);
            }
            case 2 -> {
                if (validateStructure()) {
                    getTxtLog().setText("");
                    getProgressBar().setValue(0);
                    getProgressBar().setString(totalRecords >= 0 ? String.format("0 / %d rows", totalRecords) : "0 rows");
                    showStep(3);
                }
            }
            default -> showStep(currentStep + 1);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Import CSV", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------ step 1: source

    private JPanel createSourceStep() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 0, 5);

        gc.gridwidth = 2;
        pnl.add(new JLabel("Connection:"), gc);

        gc.gridy++;
        gc.insets.top = 0;
        gc.weightx = 1.0;
        pnl.add(getCmbConnection(), gc);

        gc.gridy++;
        gc.insets.top = 10;
        gc.weightx = 0.0;
        pnl.add(new JLabel("CSV file:"), gc);

        gc.gridy++;
        gc.gridwidth = 1;
        gc.insets.top = 0;
        gc.weightx = 1.0;
        pnl.add(getTxtFile(), gc);

        gc.gridx = 1;
        gc.weightx = 0.0;
        pnl.add(getBtnBrowse(), gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        gc.insets.top = 10;
        JPanel pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlOptions.setOpaque(false);
        pnlOptions.add(new JLabel("Delimiter:"));
        pnlOptions.add(getCmbDelimiter());
        pnlOptions.add(Box.createHorizontalStrut(15));
        pnlOptions.add(new JLabel("Encoding:"));
        pnlOptions.add(getCmbEncoding());
        pnlOptions.add(Box.createHorizontalStrut(15));
        pnlOptions.add(getChkHeader());
        pnl.add(pnlOptions, gc);

        // push everything to the top
        gc.gridy++;
        gc.weighty = 1.0;
        pnl.add(Box.createVerticalGlue(), gc);

        return pnl;
    }

    private DefaultComboBoxModel<DbConfig> getCmbConnectionModel() {
        if (cmbConnectionModel == null) {
            cmbConnectionModel = new DefaultComboBoxModel<>();
            DbConfigFile.getConfigNames().stream()
                    .map(DbConfigFile::getConfig)
                    .filter(cfg -> cfg != null && !cfg.isMongoDB())
                    .forEach(cmbConnectionModel::addElement);
        }
        return cmbConnectionModel;
    }

    private JComboBox<DbConfig> getCmbConnection() {
        if (cmbConnection == null) {
            cmbConnection = new JComboBox<>(getCmbConnectionModel());
            cmbConnection.setToolTipText("JDBC connection where the table will be created.");
        }
        return cmbConnection;
    }

    private JTextField getTxtFile() {
        if (txtFile == null) {
            txtFile = new JTextField();
            txtFile.setToolTipText("Full path of the CSV file to import.");
        }
        return txtFile;
    }

    private JButton getBtnBrowse() {
        if (btnBrowse == null) {
            btnBrowse = new JButton("Browse...");
            btnBrowse.addActionListener(e -> browseFile());
        }
        return btnBrowse;
    }

    private JComboBox<String> getCmbDelimiter() {
        if (cmbDelimiter == null) {
            cmbDelimiter = new JComboBox<>(DELIMITERS);
            cmbDelimiter.setEditable(true);
            cmbDelimiter.setToolTipText("Field delimiter. Type any other single character if needed.");
        }
        return cmbDelimiter;
    }

    private JComboBox<String> getCmbEncoding() {
        if (cmbEncoding == null) {
            cmbEncoding = new JComboBox<>(ENCODINGS);
            cmbEncoding.setEditable(true);
        }
        return cmbEncoding;
    }

    private JCheckBox getChkHeader() {
        if (chkHeader == null) {
            chkHeader = new JCheckBox("First row contains the column names", true);
            chkHeader.setOpaque(false);
        }
        return chkHeader;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Select the CSV file to import");
//        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV files (*.csv, *.txt, *.tsv)", "csv", "txt", "tsv");
//        chooser.addChoosableFileFilter(csvFilter);
//        chooser.setFileFilter(csvFilter);

        File current = new File(getTxtFile().getText().trim());
        if (current.isFile()) {
            chooser.setSelectedFile(current);
        } else if (lastUsedDirectory != null) {
            chooser.setCurrentDirectory(lastUsedDirectory);
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastUsedDirectory = file.getParentFile();
            getTxtFile().setText(file.getAbsolutePath());
            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".tsv")) {
                getCmbDelimiter().setSelectedItem("Tab");
            }
        }
    }

    private boolean validateSource() {
        if (getCmbConnection().getSelectedItem() == null) {
            showError("Please select a connection.");
            return false;
        }
        File file = new File(getTxtFile().getText().trim());
        if (!file.isFile() || !file.canRead()) {
            showError("Please select a readable CSV file.");
            return false;
        }
        try {
            Charset.forName(String.valueOf(getCmbEncoding().getSelectedItem()).trim());
        } catch (Exception ex) {
            showError("Unknown file encoding: " + getCmbEncoding().getSelectedItem());
            return false;
        }
        return true;
    }

    /**
     * Creates a reader for the selected file with the options of the source step.
     */
    private CsvFileReader createReader() {
        File file = new File(getTxtFile().getText().trim());
        Charset charset = Charset.forName(String.valueOf(getCmbEncoding().getSelectedItem()).trim());
        char delimiter = CsvFileReader.parseDelimiter(String.valueOf(getCmbDelimiter().getSelectedItem()));
        return new CsvFileReader(file, charset, delimiter, getChkHeader().isSelected());
    }

    /** @return a string identifying the source options, used to detect changes between steps */
    private String getSourceSignature() {
        File file = new File(getTxtFile().getText().trim());
        return String.join("|", file.getAbsolutePath(), String.valueOf(file.lastModified()),
                String.valueOf(getCmbDelimiter().getSelectedItem()), String.valueOf(getCmbEncoding().getSelectedItem()),
                String.valueOf(getChkHeader().isSelected()));
    }

    // ------------------------------------------------------------------ step 2: preview

    private JPanel createPreviewStep() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);

        lblPreviewInfo = new JLabel(" ");
        pnl.add(lblPreviewInfo, BorderLayout.NORTH);

        previewModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPreview = new JTable(previewModel);
        tblPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblPreview.getTableHeader().setReorderingAllowed(false);
        tblPreview.getTableHeader().setFont(tblPreview.getTableHeader().getFont().deriveFont(Font.BOLD));
        tblPreview.setFillsViewportHeight(true);

        pnl.add(new JScrollPane(tblPreview), BorderLayout.CENTER);
        return pnl;
    }

    /**
     * Reads the column names and the first records of the file in the background,
     * then displays the preview step. The total number of records is counted afterwards.
     */
    private void loadPreview() {
        final CsvFileReader reader = createReader();
        final String signature = getSourceSignature();
        btnNext.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<CsvFileReader.Preview, Void>() {
            @Override
            protected CsvFileReader.Preview doInBackground() throws Exception {
                return reader.readPreview(PREVIEW_ROWS);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    CsvFileReader.Preview result = get();
                    if (result.headers().isEmpty()) {
                        updateButtons();
                        showError("The file is empty.");
                        return;
                    }
                    if (!signature.equals(previewSignature)) {
                        // source changed: columns definition must be computed again
                        columnsSignature = null;
                    }
                    preview = result;
                    previewSignature = signature;
                    displayPreview(result);
                    showStep(1);
                    countRecords(reader);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "Cannot read CSV file.", ex.getCause());
                    updateButtons();
                    showError("Cannot read the CSV file:\n" + ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void displayPreview(CsvFileReader.Preview data) {
        List<String> headers = data.headers();
        Object[][] rows = new Object[data.rows().size()][headers.size()];
        for (int r = 0; r < data.rows().size(); r++) {
            List<String> row = data.rows().get(r);
            for (int c = 0; c < headers.size() && c < row.size(); c++) {
                rows[r][c] = row.get(c);
            }
        }
        previewModel.setDataVector(rows, headers.toArray());
        for (int c = 0; c < tblPreview.getColumnCount(); c++) {
            tblPreview.getColumnModel().getColumn(c).setPreferredWidth(120);
        }
        totalRecords = -1;
        lblPreviewInfo.setText(String.format("%d columns. Showing the first %d records (counting records...).",
                headers.size(), data.rows().size()));
    }

    private void countRecords(CsvFileReader reader) {
        final List<String> headers = preview.headers();
        final int shown = preview.rows().size();
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return reader.countRecords();
            }

            @Override
            protected void done() {
                try {
                    totalRecords = get();
                    lblPreviewInfo.setText(String.format("%d columns, %d records in the file. Showing the first %d records.",
                            headers.size(), totalRecords, shown));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.WARNING, "Cannot count CSV records.", ex.getCause());
                    lblPreviewInfo.setText(String.format("%d columns. Showing the first %d records. Error while reading the file: %s",
                            headers.size(), shown, ex.getCause().getMessage()));
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ step 3: table structure

    private JPanel createStructureStep() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);

        // table name and value formats
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTop.setOpaque(false);
        txtSchema = new JTextField(10);
        txtSchema.setToolTipText("Optional schema. Leave empty to use the default schema of the connection.");
        txtTableName = new JTextField(20);
        txtDatePattern = new JTextField(CsvTableImporter.DEFAULT_DATE_PATTERN, 10);
        txtDatePattern.setToolTipText("Pattern used to read DATE values, java DateTimeFormatter syntax.");
        txtTimestampPattern = new JTextField(CsvTableImporter.DEFAULT_TIMESTAMP_PATTERN, 14);
        txtTimestampPattern.setToolTipText("Pattern used to read TIMESTAMP values, java DateTimeFormatter syntax.");
        pnlTop.add(new JLabel("Schema:"));
        pnlTop.add(txtSchema);
        pnlTop.add(new JLabel("Table name:"));
        pnlTop.add(txtTableName);
        pnlTop.add(Box.createHorizontalStrut(15));
        pnlTop.add(new JLabel("Date format:"));
        pnlTop.add(txtDatePattern);
        pnlTop.add(new JLabel("Timestamp format:"));
        pnlTop.add(txtTimestampPattern);
        pnl.add(pnlTop, BorderLayout.NORTH);

        // columns definition table
        columnsModel = new ImportColumnTableModel();
        tblColumns = new JTable(columnsModel);
        tblColumns.setRowHeight(Math.max(tblColumns.getRowHeight(), new JComboBox<String>().getPreferredSize().height));
        tblColumns.getTableHeader().setReorderingAllowed(false);
        tblColumns.setFillsViewportHeight(true);
        tblColumns.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JComboBox<String> cmbTypeEditor = new JComboBox<>(SQL_TYPES);
        cmbTypeEditor.setEditable(true);
        tblColumns.getColumnModel().getColumn(ImportColumnTableModel.COL_TYPE).setCellEditor(new DefaultCellEditor(cmbTypeEditor));
        tblColumns.getColumnModel().getColumn(ImportColumnTableModel.COL_INCLUDE).setMaxWidth(70);
        tblColumns.getColumnModel().getColumn(ImportColumnTableModel.COL_NULLABLE).setMaxWidth(70);
        columnsModel.addTableModelListener(e -> updateCreateSql());

        JPanel pnlColumns = new JPanel(new BorderLayout(0, 5));
        pnlColumns.setOpaque(false);
        pnlColumns.add(new JScrollPane(tblColumns), BorderLayout.CENTER);
        pnlColumns.add(createBulkTypePanel(), BorderLayout.SOUTH);

        // generated CREATE TABLE statement
        txtCreateSql = new RSyntaxTextArea(8, 60);
        txtCreateSql.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        txtCreateSql.setEditable(false);
        RTextScrollPane scrollSql = new RTextScrollPane(txtCreateSql);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlColumns, scrollSql);
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setResizeWeight(0.65d);
        pnl.add(split, BorderLayout.CENTER);

        DocumentListener sqlUpdater = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCreateSql(); }
            public void removeUpdate(DocumentEvent e) { updateCreateSql(); }
            public void changedUpdate(DocumentEvent e) { updateCreateSql(); }
        };
        txtSchema.getDocument().addDocumentListener(sqlUpdater);
        txtTableName.getDocument().addDocumentListener(sqlUpdater);

        return pnl;
    }

    private JPanel createBulkTypePanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnl.setOpaque(false);
        cmbBulkType = new JComboBox<>(SQL_TYPES);
        cmbBulkType.setEditable(true);
        cmbBulkType.setSelectedItem(ImportColumn.DEFAULT_SQL_TYPE);
        JButton btnApply = new JButton("Apply to selected columns");
        btnApply.addActionListener(e -> {
            int[] viewRows = tblColumns.getSelectedRows();
            Object type = cmbBulkType.getEditor().getItem();
            if (viewRows.length == 0 || type == null || type.toString().isBlank()) {
                return;
            }
            if (tblColumns.isEditing()) {
                tblColumns.getCellEditor().stopCellEditing();
            }
            int[] modelRows = new int[viewRows.length];
            for (int i = 0; i < viewRows.length; i++) {
                modelRows[i] = tblColumns.convertRowIndexToModel(viewRows[i]);
            }
            columnsModel.setSqlType(modelRows, type.toString().trim());
        });
        pnl.add(new JLabel("SQL type:"));
        pnl.add(cmbBulkType);
        pnl.add(btnApply);
        return pnl;
    }

    /**
     * Builds the columns definition from the CSV header. Keeps the user changes
     * if the source file and options did not change since the last time.
     */
    private void prepareColumns() {
        if (previewSignature != null && previewSignature.equals(columnsSignature)) {
            updateCreateSql();
            return;
        }

        Set<String> used = new HashSet<>();
        List<ImportColumn> cols = new ArrayList<>();
        List<String> headers = preview.headers();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            cols.add(new ImportColumn(i, header, CsvTableImporter.sanitizeIdentifier(header, used)));
        }
        columnsModel.setColumns(cols);

        String fileName = createReader().getFile().getName();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot > 0 ? fileName.substring(0, dot) : fileName;
        txtTableName.setText(CsvTableImporter.sanitizeIdentifier(baseName, new HashSet<>()));

        columnsSignature = previewSignature;
        updateCreateSql();
    }

    private DbConfig getSelectedConfig() {
        return (DbConfig) getCmbConnection().getSelectedItem();
    }

    /**
     * Creates the importer configured with the current state of the wizard.
     */
    private CsvTableImporter createImporter() {
        DbConfig cfg = getSelectedConfig();
        boolean quoted = cfg != null && Boolean.TRUE.equals(cfg.useQuotedIdentifiers);
        CsvTableImporter importer = new CsvTableImporter(createReader(), columnsModel.getColumns(),
                txtSchema.getText(), txtTableName.getText(), quoted);
        importer.setDatePattern(txtDatePattern.getText().trim());
        importer.setTimestampPattern(txtTimestampPattern.getText().trim());
        return importer;
    }

    private void updateCreateSql() {
        if (txtCreateSql == null || columnsModel == null) {
            return;
        }
        DbConfig cfg = getSelectedConfig();
        boolean quoted = cfg != null && Boolean.TRUE.equals(cfg.useQuotedIdentifiers);
        CsvTableImporter importer = new CsvTableImporter(null, columnsModel.getColumns(),
                txtSchema.getText(), txtTableName.getText(), quoted);
        txtCreateSql.setText(importer.buildCreateTableSql());
        txtCreateSql.setCaretPosition(0);
    }

    private boolean validateStructure() {
        if (tblColumns.isEditing()) {
            tblColumns.getCellEditor().stopCellEditing();
        }
        if (txtTableName.getText().isBlank()) {
            showError("Please enter the name of the table to create.");
            return false;
        }
        List<ImportColumn> included = columnsModel.getColumns().stream().filter(ImportColumn::isIncluded).toList();
        if (included.isEmpty()) {
            showError("Please include at least one column.");
            return false;
        }
        Set<String> names = new HashSet<>();
        for (ImportColumn col : included) {
            if (!names.add(col.getName().toUpperCase(Locale.ROOT))) {
                showError("Duplicate column name: " + col.getName());
                return false;
            }
        }
        try {
            java.time.format.DateTimeFormatter.ofPattern(txtDatePattern.getText().trim());
            java.time.format.DateTimeFormatter.ofPattern(txtTimestampPattern.getText().trim());
        } catch (IllegalArgumentException ex) {
            showError("Invalid date or timestamp format: " + ex.getMessage());
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ step 4: import

    private JPanel createImportStep() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);

        JPanel pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlOptions.setOpaque(false);
        spnBatchSize = new JSpinner(new SpinnerNumberModel(CsvTableImporter.DEFAULT_BATCH_SIZE, 1, 100000, 50));
        spnBatchSize.setToolTipText("Number of rows sent to the database and committed at once.");
        chkCreateTable = new JCheckBox("Create the table", true);
        chkCreateTable.setOpaque(false);
        chkCreateTable.setToolTipText("Uncheck to import into an existing table with the same columns.");
        chkDropTable = new JCheckBox("Drop the table first if it exists", false);
        chkDropTable.setOpaque(false);
        pnlOptions.add(new JLabel("Batch size:"));
        pnlOptions.add(spnBatchSize);
        pnlOptions.add(Box.createHorizontalStrut(15));
        pnlOptions.add(chkCreateTable);
        pnlOptions.add(chkDropTable);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 5));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlOptions, BorderLayout.NORTH);
        pnlTop.add(getProgressBar(), BorderLayout.SOUTH);
        pnl.add(pnlTop, BorderLayout.NORTH);

        pnl.add(new JScrollPane(getTxtLog()), BorderLayout.CENTER);
        return pnl;
    }

    private JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setString(" ");
        }
        return progressBar;
    }

    private JTextArea getTxtLog() {
        if (txtLog == null) {
            txtLog = new JTextArea();
            txtLog.setEditable(false);
            txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txtLog.getFont().getSize()));
        }
        return txtLog;
    }

    private void appendLog(String message) {
        getTxtLog().append(message + "\n");
        getTxtLog().setCaretPosition(getTxtLog().getDocument().getLength());
    }

    private void updateProgress(long rows, long batches) {
        if (totalRecords > 0) {
            getProgressBar().setIndeterminate(false);
            getProgressBar().setValue((int) Math.min(100, rows * 100 / totalRecords));
            getProgressBar().setString(String.format("%d / %d rows - %d batch(es)", rows, totalRecords, batches));
        } else {
            getProgressBar().setString(String.format("%d rows - %d batch(es)", rows, batches));
        }
    }

    private void cancelImport() {
        CsvTableImporter importer = runningImporter;
        if (importer != null) {
            importer.cancel();
            appendLog("Cancel requested, the import will stop after the current batch...");
            btnCancel.setEnabled(false);
        }
    }

    /**
     * Starts the import in a background thread. Progress and log messages are sent
     * to the event dispatch thread.
     */
    private void startImport() {
        final DbConfig cfg = getSelectedConfig();
        if (cfg == null) {
            showError("Please select a connection.");
            return;
        }

        final CsvTableImporter importer = createImporter();
        importer.setBatchSize((Integer) spnBatchSize.getValue());
        importer.setCreateTable(chkCreateTable.isSelected());
        importer.setDropTableFirst(chkDropTable.isSelected());

        if (chkDropTable.isSelected()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    String.format("The table %s will be dropped if it exists. Continue?", importer.getQualifiedTableName()),
                    "Import CSV", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        runningImporter = importer;
        updateButtons();
        getTxtLog().setText("");
        getProgressBar().setValue(0);
        getProgressBar().setIndeterminate(totalRecords <= 0);
        appendLog(String.format("Importing %s into %s on connection %s",
                createReader().getFile().getAbsolutePath(), importer.getQualifiedTableName(), cfg.name));

        CsvTableImporter.ImportListener listener = new CsvTableImporter.ImportListener() {
            @Override
            public void progress(long rowsImported, long batchesCommitted) {
                SwingUtilities.invokeLater(() -> updateProgress(rowsImported, batchesCommitted));
            }

            @Override
            public void log(String message) {
                SwingUtilities.invokeLater(() -> appendLog(message));
            }
        };

        new SwingWorker<CsvTableImporter.ImportResult, Void>() {
            @Override
            protected CsvTableImporter.ImportResult doInBackground() throws Exception {
                try (Connection conn = cfg.getConnection()) {
                    return importer.runImport(conn, listener);
                }
            }

            @Override
            protected void done() {
                runningImporter = null;
                getProgressBar().setIndeterminate(false);
                updateButtons();
                try {
                    CsvTableImporter.ImportResult result = get();
                    if (!result.cancelled() && totalRecords > 0) {
                        getProgressBar().setValue(100);
                    }
                    String msg = String.format("%s: %d rows imported into %s.",
                            result.cancelled() ? "Import cancelled" : "Import finished",
                            result.rowsImported(), importer.getQualifiedTableName());
                    JOptionPane.showMessageDialog(DataImportPanel.this, msg, "Import CSV", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    LOGGER.log(Level.SEVERE, "CSV import failed.", cause);
                    appendLog("ERROR: " + cause.getMessage());
                    showError("The import failed:\n" + cause.getMessage());
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ DbConfigFileListener

    @Override
    public void configAdded(DbConfig config) {
        if (config != null && !config.isMongoDB()) {
            getCmbConnectionModel().addElement(config);
        }
    }

    @Override
    public void configRemoved(DbConfig config) {
        getCmbConnectionModel().removeElement(config);
    }
}
