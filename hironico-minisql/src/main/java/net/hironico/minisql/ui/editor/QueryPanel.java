package net.hironico.minisql.ui.editor;

import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import net.hironico.common.swing.JRoundedPanel;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.action.OpenQueryAction;
import net.hironico.minisql.ui.editor.action.SaveQueryAction;
import net.hironico.minisql.ui.editor.ribbon.EditorRibbonTab;
import net.hironico.minisql.ui.renderer.*;
import net.hironico.common.swing.JSplitPaneNoDivider;
import net.hironico.common.swing.table.FilterableTable;
import net.hironico.common.utils.json.JSONFile;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;

/**
 * Main query editor panel for SQL query execution and result display.
 * This panel provides a comprehensive SQL editor with syntax highlighting, multiple result display formats,
 * database configuration selection, and integration with the application's ribbon interface.
 */
public class QueryPanel extends JRoundedPanel implements DbConfigFile.DbConfigFileListener {

    private static final Logger LOGGER = Logger.getLogger(QueryPanel.class.getName());

    private String lastUsedDirectory = null;

    private JToolBar toolbar = null;
    private JComboBox<String> cmbConfig = null;
    private JToggleButton toggleTable = null;
    private JToggleButton toggleJSON = null;
    private JToggleButton toggleText = null;
    private ButtonGroup groupResultType = null;
    private JSplitPaneNoDivider splitQuery = null;
    private RSyntaxTextArea txtQuery = null;
    private RTextScrollPane scrollQuery = null;
    private JPanel pnlQuery = null;
    private JXStatusBar stbEditorStatusBar = null;
    private JLabel lblPosition = null;
    private JLabel lblSelection = null;
    private JLabel lblBatchMode = null;
    private JLabel lblStatusMessage = null;
    private JPanel pnlResults = null;

    private ExecuteQueryAction executeQueryAction = null;
    private SaveQueryAction saveQueryAction = null;
    private OpenQueryAction openQueryAction = null;

    private boolean batchMode = false;

    /**
     * Constructs a new QueryPanel with default configuration.
     * Initializes UI components and registers as a database configuration file listener.
     */
    public QueryPanel() {
        initialize();
        DbConfigFile.addListener(this);
    }

    /**
     * Initializes the UI components of the query panel.
     * Sets up layout, toolbar, and split pane for query editor and results display.
     */
    private void initialize() {
        setBackground(JRoundedPanel.LIGHT_BLUE_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        setLayout(new BorderLayout());
        add(getToolbar(), BorderLayout.NORTH);
        add(getSplitQuery(), BorderLayout.CENTER);
        getSplitQuery().setDividerLocation(250);
    }

    /**
     * Gets or creates the toolbar component.
     * Creates a toolbar with database configuration combo box and result display type toggles.
     * 
     * @return the JToolBar instance for this query panel
     */
    private JToolBar getToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.add(getCmbConfig());
            toolbar.addSeparator();
            toolbar.add(this.getToggleTable());
            toolbar.add(this.getToggleJSON());
            toolbar.add(this.getToggleText());
        }

        return toolbar;
    }

    /**
     * Gets or creates the button group for result display type selection.
     * 
     * @return ButtonGroup for result display type toggles
     */
    private ButtonGroup getGroupResultType() {
        if (groupResultType == null) {
            groupResultType = new ButtonGroup();
        }

        return groupResultType;
    }

    /**
     * Gets or creates the table display toggle button.
     * 
     * @return JToggleButton for table display mode
     */
    private JToggleButton getToggleTable() {
        if (toggleTable == null) {
            toggleTable = new JToggleButton("Table");
            toggleTable.setSelected(true);
            getGroupResultType().add(toggleTable);
        }

        return toggleTable;
    }

    /**
     * Gets or creates the JSON display toggle button.
     * 
     * @return JToggleButton for JSON display mode
     */
    private JToggleButton getToggleJSON() {
        if (toggleJSON == null) {
            toggleJSON = new JToggleButton("JSON");
            getGroupResultType().add(toggleJSON);
        }

        return toggleJSON;
    }

    /**
     * Gets or creates the text display toggle button.
     * 
     * @return JToggleButton for text display mode
     */
    private JToggleButton getToggleText() {
        if (toggleText == null) {
            toggleText = new JToggleButton("Text");
            getGroupResultType().add(toggleText);
        }

        return toggleText;
    }
    /**
     * Gets the last used directory when opening or saving a file in the editor.
     * 
     * @return the last used directory path, or user home if not set
     */
    public String getLastUserDirectory() {
        if (this.lastUsedDirectory == null) {
            this.lastUsedDirectory = System.getProperty("user.home");
        }

        return this.lastUsedDirectory;
    }

    /**
     * Sets the last used directory for file operations.
     * 
     * @param directoryName the directory path to set as last used
     */
    public void setLastUsedDirectory(String directoryName) {
        this.lastUsedDirectory = directoryName;
    }

    /**
     * Gets or creates the open query action.
     * 
     * @return OpenQueryAction instance
     */
    private OpenQueryAction getOpenQueryAction() {
        if (openQueryAction == null) {
            openQueryAction = new OpenQueryAction();
        }

        return openQueryAction;
    }

    /**
     * Gets or creates the save query action.
     * 
     * @return SaveQueryAction instance
     */
    private SaveQueryAction getSaveQueryAction() {
        if (saveQueryAction == null) {
            saveQueryAction = new SaveQueryAction();
        }

        return saveQueryAction;
    }

    /**
     * Gets or creates the execute query action.
     * 
     * @return ExecuteQueryAction instance
     */
    private ExecuteQueryAction getExecuteQueryAction() {
        if (executeQueryAction == null) {
            executeQueryAction = new ExecuteQueryAction();
        }

        return executeQueryAction;
    }

    /**
     * Gets or creates the database configuration combo box.
     * Populates with available database configurations and sets up selection listener.
     * 
     * @return JComboBox containing database configuration names
     */
    private JComboBox<String> getCmbConfig() {
        if (cmbConfig == null) {
            cmbConfig = new JComboBox<>();
            for (String cfg : DbConfigFile.getConfigNames()) {
                cmbConfig.addItem(cfg);
            }
            cmbConfig.addItemListener(e -> {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    MainWindow.getInstance().setEditorTabTitle(QueryPanel.this, (String) cmbConfig.getSelectedItem());
                }
            });
        }

        return cmbConfig;
    }

    /**
     * Sets the divider location of the split pane.
     * 
     * @param location the relative divider location (0.0 to 1.0)
     */
    public void setDividerLocation(double location) {
        getSplitQuery().setDividerLocation(location);
    }

    /**
     * Gets or creates the split pane for query editor and results.
     * 
     * @return JSplitPaneNoDivider separating query editor from results
     */
    private JSplitPaneNoDivider getSplitQuery() {
        if (splitQuery == null) {
            splitQuery = new JSplitPaneNoDivider();
            splitQuery.setBorder(BorderFactory.createEmptyBorder());
            splitQuery.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitQuery.setDividerLocation(250);

            splitQuery.add(getPnlQuery(), JSplitPane.TOP);
            splitQuery.add(getPnlResults(), JSplitPane.BOTTOM);
        }

        return splitQuery;
    }

    /**
     * Gets the currently selected database configuration.
     * 
     * @return DbConfig instance, or null if no selection
     */
    public DbConfig getConfig() {
        int index = getCmbConfig().getSelectedIndex();
        if (index < 0) {
            return null;
        }

        String cfgName = getCmbConfig().getItemAt(index);
        if (cfgName == null) {
            return null;
        }

        return DbConfigFile.getConfig(cfgName);
    }

    /**
     * Sets the database configuration selection.
     * 
     * @param config the DbConfig to select, or null to clear selection
     */
    public void setConfig(DbConfig config) {
        if (config == null) {
            return;
        }

        getCmbConfig().getModel().setSelectedItem(config.name);
    }

    /**
     * Gets the SQL query text from the editor.
     * Returns selected text if available, otherwise returns all text.
     * 
     * @return the SQL query string, trimmed and never null
     */
    public String getQueryText() {
        String sql = getTxtQuery().getSelectedText();
        if (sql == null) {
            sql = getTxtQuery().getText();
        }
        return sql == null ? "" : sql.trim();
    }

    /**
     * Sets the SQL query text in the editor.
     * 
     * @param text the SQL query text to set
     */
    public void setQueryText(String text) {
        getTxtQuery().setText(text);
    }

    /**
     * Gets the current result display type based on selected toggle button.
     * 
     * @return one of the SQLResultSetTableModel.DISPLAY_TYPE constants
     */
    public int getResultDisplayType() {
        if (getToggleTable().isSelected()) {
            return SQLResultSetTableModel.DISPLAY_TYPE_TABLE;
        } else if (getToggleJSON().isSelected()) {
            return SQLResultSetTableModel.DISPLAY_TYPE_JSON;
        } else if (getToggleText().isSelected()) {
            return SQLResultSetTableModel.DISPLAY_TYPE_TEXT;
        } else {
            LOGGER.warning("Cannot find out about the display type of the results. Using table as a default.");
            return SQLResultSetTableModel.DISPLAY_TYPE_TABLE;
        }
    }

    /**
     * Sets the results display component.
     * Clears any existing results and displays the new component.
     * 
     * @param resultsComp the component to display in the results area
     */
    public void setResultsComponent(JComponent resultsComp) {
        getPnlResults().removeAll();
        getPnlResults().add(resultsComp, BorderLayout.CENTER);
        getPnlResults().updateUI();
        getSplitQuery().setDividerLocation(250);
        getTxtQuery().setCaretPosition(0);
    }

    /**
     * Gets or creates the results panel.
     * 
     * @return JPanel for displaying query results
     */
    private JPanel getPnlResults() {
        if (pnlResults == null) {
            pnlResults = new JPanel();
            pnlResults.setBorder(BorderFactory.createEmptyBorder());
            pnlResults.setLayout(new BorderLayout());
        }

        return pnlResults;
    }

    /**
     * Gets or creates the scroll pane for the query editor.
     * 
     * @return RTextScrollPane containing the SQL editor
     */
    private RTextScrollPane getScrollQuery() {
        if (scrollQuery == null) {
            scrollQuery = new RTextScrollPane(getTxtQuery());
            scrollQuery.setBorder(BorderFactory.createEmptyBorder());

            scrollQuery.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }

        return scrollQuery;
    }

    /**
     * Gets or creates the query panel containing editor and status bar.
     * 
     * @return JPanel containing the SQL editor and status information
     */
    private JPanel getPnlQuery() {
        if (pnlQuery == null) {
            pnlQuery = new JPanel();
            pnlQuery.setLayout(new BorderLayout());
            pnlQuery.add(getScrollQuery(), BorderLayout.CENTER);
            pnlQuery.add(getStbEditorStatusBar(), BorderLayout.SOUTH);
        }

        return pnlQuery;
    }

    /**
     * Gets or creates the SQL text editor component.
     * Configures syntax highlighting, keyboard shortcuts, and event handlers.
     * 
     * @return RSyntaxTextArea for SQL editing
     */
    public RSyntaxTextArea getTxtQuery() {
        if (txtQuery == null) {
            txtQuery = new RSyntaxTextArea();
            txtQuery.setBorder(BorderFactory.createEmptyBorder());
            txtQuery.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
            txtQuery.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_F5) {
                        ActionEvent evt = new ActionEvent(getTxtQuery(), -1, "execute");
                        getExecuteQueryAction().actionPerformed(evt);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_E && e.isControlDown()) {
                        ActionEvent evt = new ActionEvent(getTxtQuery(), -1, "execute");
                        getExecuteQueryAction().actionPerformed(evt);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_F4 && e.isControlDown()) {
                        MainWindow.getInstance().removeCurrentEditorTab();
                    }

                    if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                        ActionEvent evt = new ActionEvent("", -1, "save");
                        getSaveQueryAction().actionPerformed(evt);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_O && e.isControlDown()) {
                        ActionEvent evt = new ActionEvent("", -1, "open");
                        getOpenQueryAction().actionPerformed(evt);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
                        QueryPanel pnl = new QueryPanel();
                        pnl.setConfig(QueryPanel.this.getConfig());
                        MainWindow.getInstance().addNewEditorTab(pnl, QueryPanel.this.getConfig().name);
                    }

                    // CTrl + 1,2,3...,9,0
                    // Should recall the text for this shortcut and add it to current editor
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });

            txtQuery.addCaretListener(e -> {
                int line = getTxtQuery().getCaretLineNumber() + 1;
                int y = getTxtQuery().getCaretOffsetFromLineStart();
                getLblPosition().setText(String.format("%d:%d (%d)", line, y, e.getDot()));

                String selectionText = getTxtQuery().getSelectedText();
                if (selectionText == null) {
                    getLblSelection().setText("0 chars");
                } else {
                    int lineBreaks = selectionText.length() - selectionText.replace("\n", "").length();
                    if (lineBreaks == 0) {
                        getLblSelection().setText(String.format("%d chars.", getTxtQuery().getSelectedText().length()));
                    } else {
                        getLblSelection().setText(String.format("%d chars, %d line breaks.", selectionText.length(), lineBreaks));
                    }
                }
            });

            txtQuery.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    super.focusGained(e);
                    QueryPanel.this.updateRibbon();
                }
            });

            // fix the scaled font size for HiDPI screens depending on the OS
            float fontSize = 11f; // windows
            if (SystemInfo.isLinux) {
                fontSize = SystemInfo.isKDE ? 13f : 15f;
            }
            if (SystemInfo.isMacOS) {
                fontSize = 13f;
            }
            float fontScaledSize = UIScale.scale(fontSize);
            Font scaledFont = txtQuery.getFont().deriveFont(fontScaledSize);
            txtQuery.setFont(scaledFont);
        }

        return txtQuery;
    }

    /**
     * Gets or creates the editor status bar.
     * 
     * @return JXStatusBar displaying cursor position and selection information
     */
    private JXStatusBar getStbEditorStatusBar() {
        if (stbEditorStatusBar == null) {
            stbEditorStatusBar = new JXStatusBar();
            stbEditorStatusBar.setBorder(BorderFactory.createEtchedBorder());
            stbEditorStatusBar.add(getLblPosition());
            stbEditorStatusBar.add(getLblSelection());
            stbEditorStatusBar.add(getLblBatchMode());
            stbEditorStatusBar.add(getLblStatusMessage());
        }

        return stbEditorStatusBar;
    }

    /**
     * Gets or creates the selection information label.
     * 
     * @return JLabel displaying text selection statistics
     */
    private JLabel getLblSelection() {
        if (lblSelection == null) {
            lblSelection = new JLabel("0 chars");
            lblSelection.setToolTipText("Number of characters and line breaks if any currently selected");
        }

        return lblSelection;
    }

    /**
     * Gets the formatted batch mode status text.
     * 
     * @return HTML formatted string showing batch mode status
     */
    private String getBatchModeStatusText() {
        return String.format("<html>Batch mode: <b>%s</b></html>", this.batchMode ? "ON" : "OFF");
    }

    /**
     * Gets or creates the batch mode status label.
     * 
     * @return JLabel displaying current batch mode status
     */
    private JLabel getLblBatchMode() {
        if (lblBatchMode == null) {
            lblBatchMode = new JLabel(getBatchModeStatusText());
            lblBatchMode.setToolTipText("Creates SQL statements executed separately using the statement separator in connection config.");
        }

        return lblBatchMode;
    }

    /**
     * Gets or creates the cursor position label.
     * 
     * @return JLabel displaying cursor position in the editor
     */
    private JLabel getLblPosition() {
        if (lblPosition == null) {
            lblPosition = new JLabel();
            lblPosition.setText("1:0 (0)");
            lblPosition.setToolTipText("Caret position at 'line:offset (char number)'");
        }

        return lblPosition;
    }

    /**
     * Gets or creates the status message label.
     * 
     * @return JLabel for displaying status messages
     */
    private JLabel getLblStatusMessage() {
        if (lblStatusMessage == null) {
            lblStatusMessage = new JLabel();
            lblStatusMessage.setText("");
        }

        return lblStatusMessage;
    }

    /**
     * Sets the status message in the editor status bar.
     * 
     * @param msg the status message to display
     */
    public void setStatusMessage(String msg) {
        getLblStatusMessage().setText(msg);
    }

    /**
     * Utility class to build a result component from a list of SQLResultSetTableModel that is produced by the query
     * actions or any other part that is querying the database using the MiniSQL framework.
     * @param modelListToDisplay list of SQLResultSetTableModel to be displayed in a tab component
     * @return JComponent to display results
     */
    public static JComponent getResultComponentTab(List<SQLResultSetTableModel> modelListToDisplay) {
        JTabbedPane tabResults = new JTabbedPane(JTabbedPane.BOTTOM);
        tabResults.setBorder(BorderFactory.createEmptyBorder(-2, -2, -1, -3));

        if (modelListToDisplay == null || modelListToDisplay.isEmpty()) {
            JComponent comp = getEmptyResultComponent();
            tabResults.addTab("No result", comp);
        } else {
            for (int index = 0; index < modelListToDisplay.size(); index++) {
                SQLResultSetTableModel model = modelListToDisplay.get(index);
                String title = model.getTitle() != null ? model.getTitle() : String.format("Result #%d", index);
                title += String.format(" (%d rows)", model.getRowCount());
                JComponent comp = getResultComponent(model);
                tabResults.addTab(title, comp);
            }
        }

        return tabResults;
    }

    private static JComponent getEmptyResultComponent() {
        JPanel pnl = new JPanel();
        pnl.add(new JLabel("Although the query was correct the database did not return anything."));
        pnl.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        return pnl;
    }

    private static JComponent getResultComponent(SQLResultSetTableModel modelToDisplay) {
        switch (modelToDisplay.getDisplayType()) {
            case SQLResultSetTableModel.DISPLAY_TYPE_TEXT -> {
                return getResultComponentText(modelToDisplay);
            }
            case SQLResultSetTableModel.DISPLAY_TYPE_JSON -> {
                return getResultComponentJSON(modelToDisplay);
            }
            case SQLResultSetTableModel.DISPLAY_TYPE_SQL -> {
                return getResultComponentSQL(modelToDisplay);
            }
            default -> {
                return getResultComponentTable(modelToDisplay);
            }
        }
    }

    private static RSyntaxTextArea getResultTextArea(String syntax) {
        RSyntaxTextArea textResults = new RSyntaxTextArea();
        textResults.setSyntaxEditingStyle(syntax);
        textResults.setBorder(BorderFactory.createEmptyBorder());
        textResults.setEditable(false);

        return textResults;
    }

    private static RTextScrollPane getResultTextAreaScroll(RSyntaxTextArea textResults) {
        RTextScrollPane scrollResults = new RTextScrollPane(textResults);
        scrollResults.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        scrollResults.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollResults.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollResults;
    }

    private static String tableModelToString(DefaultTableModel modelToDisplay) {
        StringBuilder textToDisplay = new StringBuilder();
        for (int row = 0; row < modelToDisplay.getRowCount(); row++) {
            for (int column = 0; column < modelToDisplay.getColumnCount(); column++) {
                if (column > 0) {
                    textToDisplay.append("\t");
                }
                Object value = modelToDisplay.getValueAt(row, column);
                String valueStr = value == null ? "NULL" : value.toString();
                textToDisplay.append(valueStr);
            }
        }
        return textToDisplay.toString();
    }

    private static String tableModelToJSON(DefaultTableModel modelToDisplay) {
        try {
            List<Map<String, Object>> flatRows = new ArrayList<>();
            for (int row = 0; row < modelToDisplay.getRowCount(); row++) {
                // Represent a row in DB. Key: Column name, Value: Column value
                Map<String, Object> rowValues = new HashMap<>();

                for (int col = 0; col < modelToDisplay.getColumnCount(); col++) {
                    String colName = modelToDisplay.getColumnName(col);
                    rowValues.put(colName, modelToDisplay.getValueAt(row, col));
                }

                flatRows.add(rowValues);
            }

            return JSONFile.serialize(flatRows);
        } catch (JsonProcessingException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            return sw.toString();
        }
    }

    private static JComponent getResultComponentSQL(DefaultTableModel modelToDisplay) {
        RSyntaxTextArea textResults = getResultTextArea(SyntaxConstants.SYNTAX_STYLE_SQL);
        textResults.setText(tableModelToString(modelToDisplay));
        return getResultTextAreaScroll(textResults);
    }

    private static JComponent getResultComponentJSON(DefaultTableModel modelToDisplay) {
        RSyntaxTextArea textResults = getResultTextArea(SyntaxConstants.SYNTAX_STYLE_JSON);
        textResults.setText(tableModelToJSON(modelToDisplay));
        return getResultTextAreaScroll(textResults);
    }

    private static JComponent getResultComponentText(DefaultTableModel modelToDisplay) {
        RSyntaxTextArea textResults = getResultTextArea(SyntaxConstants.SYNTAX_STYLE_CSV);
        textResults.setText(tableModelToString(modelToDisplay));
        return getResultTextAreaScroll(textResults);
    }

    private static JComponent getResultComponentTable(DefaultTableModel modelToDisplay) {

        FilterableTable table = new FilterableTable();
        table.setModel(modelToDisplay);
        table.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
        table.setEditable(false);
        table.setColumnControlVisible(false);
        table.setColumnSelectionAllowed(true);

        // setup renderers for the new table.
        for (int index = 0; index < modelToDisplay.getColumnCount(); index++) {
            Class<?> clazz = modelToDisplay.getColumnClass(index);
            String className = clazz.getName().toUpperCase();

            LOGGER.info("Classname: " + className);

            TableCellRenderer rendererDelegate = new RowHighlightRenderer(table.getDefaultRenderer(clazz));

            if (className.contains("CLOB")) {
                TableCellEditor editorDelegate = table.getDefaultEditor(clazz);
                ClobTableCellRenderer clobRenderer = new ClobTableCellRenderer(rendererDelegate);
                ClobTableCellEditor clobEditor = new ClobTableCellEditor(editorDelegate);
                table.setDefaultRenderer(clazz, clobRenderer);
                table.setDefaultEditor(clazz, clobEditor);
            } else if (className.contains("TIME")) {
                DateTableCellRenderer dateRenderer = new DateTableCellRenderer(rendererDelegate);
                table.setDefaultRenderer(clazz, dateRenderer);
            } else if (className.contains("DECIMAL")) {
                DecimalTableCellRenderer decimalRenderer = new DecimalTableCellRenderer(rendererDelegate);
                table.setDefaultRenderer(clazz, decimalRenderer);
            } else {
                table.setDefaultRenderer(clazz, rendererDelegate);
            }
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return scroll;
    }

    @Override
    public void configAdded(DbConfig config) {
        this.getCmbConfig().addItem(config.name);
    }

    @Override
    public void configRemoved(DbConfig config) {
        this.getCmbConfig().removeItem(config.name);
    }

    /**
     * Loads SQL query text from the specified file.
     * Reads the file content and sets it as the query text in the editor.
     * 
     * @param file the file to load SQL query from
     */
    public void loadFile(File file) {
        this.setLastUsedDirectory(file.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
            this.setQueryText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error while reading the file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves the current SQL query text to the specified file.
     * Prompts for confirmation if the file already exists.
     * 
     * @param saveFile the file to save the SQL query to
     */
    public void saveFile(File saveFile) {
        String lastDir = saveFile.getAbsolutePath();
        this.setLastUsedDirectory(lastDir);

        if (saveFile.exists()) {
            int confirm = JOptionPane.showConfirmDialog(this, "File exists. Overwrite ?", "Confirm...",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            bw.write(this.getTxtQuery().getText());
            bw.flush();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error while writing to the file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks if batch mode is currently enabled.
     * 
     * @return true if batch mode is enabled, false otherwise
     */
    public boolean isBatchMode() {
        return batchMode;
    }

    /**
     * Sets the batch mode status and updates the UI accordingly.
     * 
     * @param batchMode true to enable batch mode, false to disable
     */
    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
        getLblBatchMode().setText(getBatchModeStatusText());
    }

    /**
     * Updates the ribbon interface to show the Editor tab.
     * Selects the Editor ribbon tab and refreshes its display.
     */
    public void updateRibbon() {
        RibbonTab ribbonTab = MainWindow.getInstance().getRibbon().setSelectedRibbonTab("Editor");
        ribbonTab.updateDisplay();
    }
}
