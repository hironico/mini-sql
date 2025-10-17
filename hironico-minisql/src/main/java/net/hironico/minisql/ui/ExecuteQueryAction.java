package net.hironico.minisql.ui;

import net.hironico.minisql.ui.editor.QueryPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ExecuteQueryAction extends AbstractQueryAction {
    public ExecuteQueryAction() {
        super("Execute", "icons8_play_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Execute query of currently selected editor.");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (comp instanceof QueryPanel queryPanel) {
            ExecuteQueryAction.executeQueryAsync(queryPanel);
        }
    }

    public static void executeQueryAsync(QueryPanel queryPanel) {
        AbstractQueryAction.executeQueryAsync(queryPanel);
    }
}