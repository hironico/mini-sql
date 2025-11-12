package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.event.ActionEvent;

/**
 * Action for opening a new or existing batch execution panel.
 * This action creates or switches to a batch tab where users can add SQL files
 * and execute them in batch mode against a selected database connection.
 */
public class NewBatchAction extends AbstractRibbonAction {

    /** The singleton batch panel instance created by this action */
    private final BatchPanel batchPanel = new BatchPanel();

    /**
     * Constructs a new NewBatchAction.
     * Sets the action name to "Batch" and uses the play property icon.
     */
    public NewBatchAction() {
        super("Batch", "icons8_play_property_64px.png");
    }

    /**
     * Executes the new batch action when triggered.
     * Checks if a batch tab already exists and selects it, otherwise creates a new batch panel tab.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow win = MainWindow.getInstance();
        int index = win.getEditorTabIndexOf("Batch", true);

        if (index < 0) {
            win.addNewEditorTab(batchPanel, "Batch");
        } else {
            win.setSelectedEditor("Batch");
        }
    }
}
