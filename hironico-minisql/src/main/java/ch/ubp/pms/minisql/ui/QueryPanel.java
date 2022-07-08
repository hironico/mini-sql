package ch.ubp.pms.minisql.ui;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.minisql.DbConfigFile;
import ch.ubp.pms.minisql.model.SQLResultSetTableModel;
import ch.ubp.pms.minisql.ui.renderer.ClobTableCellEditor;
import ch.ubp.pms.minisql.ui.renderer.ClobTableCellRenderer;
import ch.ubp.pms.minisql.ui.renderer.DateTableCellRenderer;
import ch.ubp.pms.minisql.ui.renderer.RowHighlightRenderer;
import ch.ubp.pms.swing.JSplitPaneNoDivider;
import ch.ubp.pms.swing.table.FilterableTable;
import ch.ubp.pms.utils.json.JSONFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import oracle.sql.TIMESTAMPTZ;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class QueryPanel extends JPanel {

    private static final long serialVersionUID = 1L;

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
    private JPanel pnlResults = null;

    private ExecuteQueryAction executeQueryAction = null;
    private SaveQueryAction saveQueryAction = null;
    private OpenQueryAction openQueryAction = null;

    public QueryPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        add(getToolbar(), BorderLayout.NORTH);
        add(getSplitQuery(), BorderLayout.CENTER);
        getSplitQuery().setDividerLocation(0.50d);
    }

    private JToolBar getToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.add(getCmbConfig());
            toolbar.addSeparator();
            toolbar.add(this.getToggleTable());
            toolbar.add(this.getToggleJSON());
            toolbar.add(this.getToggleText());
        }

        return toolbar;
    }

    private ButtonGroup getGroupResultType() {
        if (groupResultType == null) {
            groupResultType = new ButtonGroup();
        }

        return groupResultType;
    }

    private JToggleButton getToggleTable() {
        if (toggleTable == null) {
            toggleTable = new JToggleButton("Table");
            toggleTable.setSelected(true);
            getGroupResultType().add(toggleTable);
        }

        return toggleTable;
    }

    private JToggleButton getToggleJSON() {
        if (toggleJSON == null) {
            toggleJSON = new JToggleButton("JSON");
            getGroupResultType().add(toggleJSON);
        }

        return toggleJSON;
    }

    private JToggleButton getToggleText() {
        if (toggleText == null) {
            toggleText = new JToggleButton("Text");
            getGroupResultType().add(toggleText);
        }

        return toggleText;
    }
    /**
     * Get the last used directroy when openning or saving a file into the editor.
     */
    String getLastUserDirectory() {
        if (this.lastUsedDirectory == null) {
            this.lastUsedDirectory = System.getProperty("user.home");
        }

        return this.lastUsedDirectory;
    }

    void setLastUsedDirectory(String directoryName) {
        this.lastUsedDirectory = directoryName;
    }

    private OpenQueryAction getOpenQueryAction() {
        if (openQueryAction == null) {
            openQueryAction = new OpenQueryAction();
        }

        return openQueryAction;
    }

    private SaveQueryAction getSaveQueryAction() {
        if (saveQueryAction == null) {
            saveQueryAction = new SaveQueryAction();
        }

        return saveQueryAction;
    }

    private ExecuteQueryAction getExecuteQueryAction() {
        if (executeQueryAction == null) {
            executeQueryAction = new ExecuteQueryAction();
        }

        return executeQueryAction;
    }

    private JComboBox<String> getCmbConfig() {
        if (cmbConfig == null) {
            cmbConfig = new JComboBox<>();
            for (String cfg : DbConfigFile.getConfigNames()) {
                cmbConfig.addItem(cfg);
            }
            cmbConfig.addItemListener(e -> {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    MainWindow.getInstance().setTabComponentTitle(QueryPanel.this, (String) cmbConfig.getSelectedItem());
                }
            });

        }

        return cmbConfig;
    }

    public void setDividerLocation(double location) {
        getSplitQuery().setDividerLocation(location);
    }

    private JSplitPaneNoDivider getSplitQuery() {
        if (splitQuery == null) {
            splitQuery = new JSplitPaneNoDivider();
            splitQuery.setBorder(BorderFactory.createEmptyBorder());
            splitQuery.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitQuery.setDividerLocation(250);

            splitQuery.add(getScrollQuery(), JSplitPane.TOP);
            splitQuery.add(getPnlResults(), JSplitPane.BOTTOM);
        }

        return splitQuery;
    }

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

    public void setConfig(DbConfig config) {
        if (config == null) {
            return;
        }

        getCmbConfig().getModel().setSelectedItem(config.name);
    }

    public String getQueryText() {
        String sql = getTxtQuery().getSelectedText();
        if (sql == null) {
            sql = getTxtQuery().getText();
        }
        return sql == null ? "" : sql.trim();
    }

    public void setQueryText(String text) {
        getTxtQuery().setText(text);
    }

    int getResultDisplayType() {
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

    void setResultsComponent(JComponent resultsComp) {
        getPnlResults().removeAll();
        getPnlResults().add(resultsComp, BorderLayout.CENTER);
        getPnlResults().updateUI();
        getSplitQuery().setDividerLocation(250);
        getTxtQuery().setCaretPosition(0);
    }

    private JPanel getPnlResults() {
        if (pnlResults == null) {
            pnlResults = new JPanel();
            pnlResults.setBorder(BorderFactory.createEmptyBorder());
            pnlResults.setLayout(new BorderLayout());
        }

        return pnlResults;
    }

    private RTextScrollPane getScrollQuery() {
        if (scrollQuery == null) {
            scrollQuery = new RTextScrollPane(getTxtQuery());
            scrollQuery.setBorder(BorderFactory.createEmptyBorder());

            scrollQuery.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }

        return scrollQuery;
    }

    RSyntaxTextArea getTxtQuery() {
        if (txtQuery == null) {
            txtQuery = new RSyntaxTextArea();
            txtQuery.setBorder(BorderFactory.createEmptyBorder());
            txtQuery.setFont(new Font("Consolas", Font.PLAIN, 12));
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
                        MainWindow.getInstance().closeCurrentTab();
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
                        MainWindow.getInstance().displayCloseableComponent(pnl, QueryPanel.this.getConfig().name);
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
        }

        return txtQuery;
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

        LOGGER.info("Need to display : " + modelListToDisplay.size() + " results.");

        for (int index = 0; index < modelListToDisplay.size(); index++) {
            SQLResultSetTableModel model = modelListToDisplay.get(index);
            String title = model.getTitle() != null ? model.getTitle() : String.format("Result #%d", index);
            title += String.format(" (%d rows)", model.getRowCount());
            JComponent comp = getResultComponent(model);
            tabResults.addTab(title, comp);
        }

        return tabResults;
    }

    private static JComponent getResultComponent(SQLResultSetTableModel modelToDisplay) {
        switch (modelToDisplay.getDisplayType()) {
            case SQLResultSetTableModel.DISPLAY_TYPE_TEXT:
                return getResultComponentText(modelToDisplay);

            case SQLResultSetTableModel.DISPLAY_TYPE_JSON:
                return getResultComponentJSON(modelToDisplay);

            case SQLResultSetTableModel.DISPLAY_TYPE_SQL:
                return getResultComponentSQL(modelToDisplay);

            default:
                return getResultComponentTable(modelToDisplay);
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
        scrollResults.setBorder(BorderFactory.createEmptyBorder());
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
        table.adaptAllColumnsWidth(true);

        // setup renderers for the new table.
        for (int index = 0; index < modelToDisplay.getColumnCount(); index++) {
            Class<?> clazz = modelToDisplay.getColumnClass(index);
            String className = clazz.getName().toUpperCase();

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
            } else {
                table.setDefaultRenderer(clazz, rendererDelegate);
            }
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        return scroll;
    }

}