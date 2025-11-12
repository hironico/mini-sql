package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Action for clearing all files from a batch panel.
 * This action removes all loaded batch files from the current batch panel,
 * effectively resetting it to an empty state.
 */
public class ClearFilesAction extends AbstractRibbonAction {

    /**
     * Constructs a new ClearFilesAction.
     * Sets the action name to "Clear" and uses the broom (clean) icon.
     */
    public ClearFilesAction() {
        super("Clear", "icons8-broom-64.png");
    }

    /**
     * Executes the clear action when triggered.
     * Removes all files from the current batch panel.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.clear();
    }
}
