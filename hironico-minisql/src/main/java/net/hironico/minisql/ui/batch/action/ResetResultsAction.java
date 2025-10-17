package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Reset the batch to its original state and clear results
 */
public class ResetResultsAction extends AbstractRibbonAction {

    public ResetResultsAction() {
        super("Reset", "icons8_synchronize_64px_3.png");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.resetResults();
    }
}
