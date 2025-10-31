package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

public class RemoveBatchAction extends AbstractRibbonAction {

    public RemoveBatchAction() {
        super("Remove", "icons8_delete_file_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        batchPanel.removeSelection();
    }
}
