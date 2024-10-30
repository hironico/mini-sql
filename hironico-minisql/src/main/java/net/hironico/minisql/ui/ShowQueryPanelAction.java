package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.*;

import net.hironico.minisql.DbConfigFile;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.editor.QueryPanel;

public class ShowQueryPanelAction extends AbstractRibbonAction {

    public static final String NAME = "New query";

    public ShowQueryPanelAction() {
        super(NAME, "icons8_sql_64px_4.png");
        putValue(Action.SHORT_DESCRIPTION, "Opens a new SQL editor tab.");
    }

    public void actionPerformed(ActionEvent evt) {
        QueryPanel queryPanel = new QueryPanel();
        String conName = MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedConnectionName();
        if (conName == null) {
            return;
        }
        queryPanel.setConfig(DbConfigFile.getConfig(conName));
        MainWindow.getInstance().displayCloseableComponent(queryPanel, "New query");
        queryPanel.setResultsComponent(new JPanel());
    }
}