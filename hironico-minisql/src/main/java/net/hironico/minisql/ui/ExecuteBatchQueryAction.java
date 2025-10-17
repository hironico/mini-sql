package net.hironico.minisql.ui;

import net.hironico.minisql.ui.editor.QueryPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ExecuteBatchQueryAction extends AbstractQueryAction {
    public ExecuteBatchQueryAction() {
        super("Batch mode", null);
        putValue(Action.SHORT_DESCRIPTION, "Set the currently selected editor in BATCH mode for query execution.");
    }

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