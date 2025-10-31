package net.hironico.minisql.ui;

import net.hironico.minisql.ui.editor.QueryPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Action implementation for toggling batch mode on the currently active query panel.
 * This action is typically bound to a checkbox and enables or disables batch execution mode
 * for SQL queries in the selected editor tab.
 */
public class ExecuteBatchQueryAction extends AbstractQueryAction {
    
    /**
     * Constructs a new ExecuteBatchQueryAction with default name and no icon.
     * Sets the action name to "Batch mode" and provides a description for the action.
     */
    public ExecuteBatchQueryAction() {
        super("Batch mode", null);
        putValue(Action.SHORT_DESCRIPTION, "Set the currently selected editor in BATCH mode for query execution.");
    }

    /**
     * Toggles batch mode when this action is triggered.
     * Retrieves the currently selected editor tab component and sets its batch mode
     * based on the state of the checkbox that triggered this action.
     * 
     * @param evt the action event that triggered this batch mode toggle
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JCheckBox chk) {
            Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
            if (comp instanceof QueryPanel queryPanel) {
                queryPanel.setBatchMode(chk.isSelected());
            }
        }
    }
}
