package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Action for removing selected files from a batch panel.
 * This action removes the currently selected files or folders from the
 * batch execution list in the current batch panel.
 */
public class RemoveBatchAction extends AbstractRibbonAction {

    /**
     * Constructs a new RemoveBatchAction.
     * Sets the action name to "Remove" and uses the delete file icon.
     */
    public RemoveBatchAction() {
        super("Remove", "icons8_delete_file_64px.png");
    }

    /**
     * Executes the remove action when triggered.
     * Removes the currently selected files from the batch panel.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.removeSelection();
    }
}
