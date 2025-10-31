package net.hironico.minisql.ui;

import net.hironico.minisql.ui.editor.QueryPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Action implementation for executing SQL queries in the currently active query panel.
 * This action retrieves the current editor tab component and executes the SQL query
 * if the component is an instance of QueryPanel.
 */
public class ExecuteQueryAction extends AbstractQueryAction {
    /**
     * Constructs a new ExecuteQueryAction with default name and icon.
     * Sets the action name to "Execute" uses the play icon for visual representation.
     */
    public ExecuteQueryAction() {
        super("Execute", "icons8_play_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Execute query of currently selected editor.");
    }

    /**
     * Executes the SQL query when this action is triggered.
     * Retrieves the currently selected editor tab component and executes the query
     * if it's a QueryPanel instance.
     * 
     * @param evt the action event that triggered this execution
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (comp instanceof QueryPanel queryPanel) {
            ExecuteQueryAction.executeQueryAsync(queryPanel);
        }
    }

    /**
     * Executes the given query panel's SQL query asynchronously.
     * This is a convenience method that delegates to the parent class's
     * executeQueryAsync method.
     * 
     * @param queryPanel the QueryPanel containing the SQL query to execute
     */
    public static void executeQueryAsync(QueryPanel queryPanel) {
        AbstractQueryAction.executeQueryAsync(queryPanel);
    }
}
