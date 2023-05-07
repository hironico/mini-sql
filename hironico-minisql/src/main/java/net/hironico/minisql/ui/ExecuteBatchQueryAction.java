package net.hironico.minisql.ui;

import net.hironico.minisql.ui.editor.QueryPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ExecuteBatchQueryAction extends AbstractQueryAction {
    public ExecuteBatchQueryAction() {
        super("Execute Batch", "icons8_play_property_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Execute query of currently selected editor in BATCH mode using batch separator in db config.");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        QueryPanel queryPanel = null;
        if (!(comp instanceof QueryPanel)) {
            return;
        } else {
            queryPanel = (QueryPanel)comp;
        }

        ExecuteQueryAction.executeQueryAsync(queryPanel, true);
    }
}