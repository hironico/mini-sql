package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.event.ActionEvent;

public class RunBatchAction extends AbstractRibbonAction {

    public RunBatchAction() {
        super("Run batch", "icons8_play_property_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (! (MainWindow.getInstance().getCurrentEditorTabComponent() instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.runAll();
    }
}
