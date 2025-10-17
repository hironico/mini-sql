package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.event.ActionEvent;

/**
 * Open the batch execution tab to run a set of files
 */
public class NewBatchAction extends AbstractRibbonAction {

    private final BatchPanel batchPanel = new BatchPanel();

    public NewBatchAction() {
        super("Batch", "icons8_play_property_64px.png");
    }

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
