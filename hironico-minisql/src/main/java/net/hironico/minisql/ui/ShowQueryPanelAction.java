package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.*;

import net.hironico.minisql.DbConfigFile;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.editor.QueryPanel;

/**
 * Action implementation for creating new SQL query editor tabs.
 * This action opens a new query panel with the currently selected database connection
 * configuration, allowing users to write and execute SQL queries.
 */
public class ShowQueryPanelAction extends AbstractRibbonAction {

    /** The name identifier for this action */
    public static final String NAME = "New query";

    /** Reference to the most recently opened query panel */
    private QueryPanel lastOpenedQueryPanel;

    /**
     * Constructs a new ShowQueryPanelAction with default name and icon.
     * Uses the SQL icon for visual representation and sets a descriptive tooltip.
     */
    public ShowQueryPanelAction() {
        super(NAME, "icons8_sql_64px_4.png");
        putValue(Action.SHORT_DESCRIPTION, "Opens a new SQL editor tab.");
    }

    /**
     * Creates a new query panel when this action is triggered.
     * Retrieves the currently selected database connection from the schema explorer,
     * creates a new QueryPanel with that configuration, and adds it as a new editor tab.
     * 
     * @param evt the action event that triggered this query panel creation
     */
    public void actionPerformed(ActionEvent evt) {
        String conName = MainWindow.getInstance().getSchemaExplorerPanel().getSelectedConnectionName();
        if (conName == null) {
            return;
        }

        QueryPanel queryPanel = new QueryPanel();
        queryPanel.setConfig(DbConfigFile.getConfig(conName));
        MainWindow.getInstance().addNewEditorTab(queryPanel, "New query");
        queryPanel.setResultsComponent(new JPanel());
        lastOpenedQueryPanel = queryPanel;
    }

    /**
     * Gets the most recently opened query panel.
     * 
     * @return the last opened QueryPanel instance, or null if no panel has been opened
     */
    public QueryPanel getLastOpenedQueryPanel() {
        return lastOpenedQueryPanel;
    }
}
