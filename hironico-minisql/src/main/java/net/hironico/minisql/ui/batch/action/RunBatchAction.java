package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.event.ActionEvent;

/**
 * Action for executing all files in a batch panel.
 * This action triggers the execution of all loaded batch files against
 * the selected database connection in the current batch panel.
 */
public class RunBatchAction extends AbstractRibbonAction {

    /**
     * Constructs a new RunBatchAction.
     * Sets the action name to "Run batch" and uses the play property icon.
     */
    public RunBatchAction() {
        super("Run batch", "icons8_play_property_64px.png");
    }

    /**
     * Executes the run batch action when triggered.
     * Starts execution of all files in the current batch panel asynchronously.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (! (MainWindow.getInstance().getCurrentEditorTabComponent() instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.runAll();
    }
}
