package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Action for resetting batch execution results.
 * This action clears all execution results and timing information from
 * the current batch panel, effectively resetting the batch to a clean state
 * without removing the loaded files.
 */
public class ResetResultsAction extends AbstractRibbonAction {

    /**
     * Constructs a new ResetResultsAction.
     * Sets the action name to "Reset" and uses the synchronize icon.
     */
    public ResetResultsAction() {
        super("Reset", "icons8_synchronize_64px_3.png");
    }

    /**
     * Executes the reset results action when triggered.
     * Clears all execution results and timing data from the current batch panel.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.resetResults();
    }
}
