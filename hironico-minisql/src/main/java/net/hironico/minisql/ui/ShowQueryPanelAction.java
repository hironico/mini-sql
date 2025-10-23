package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.*;

import net.hironico.minisql.DbConfigFile;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.editor.QueryPanel;

public class ShowQueryPanelAction extends AbstractRibbonAction {

    public static final String NAME = "New query";

    private QueryPanel lastOpenedQueryPanel;

    public ShowQueryPanelAction() {
        super(NAME, "icons8_sql_64px_4.png");
        putValue(Action.SHORT_DESCRIPTION, "Opens a new SQL editor tab.");
    }

    public void actionPerformed(ActionEvent evt) {
        String conName = MainWindow.getInstance().getSchemaExplorerPanel().getSelectedConnectionName();
        if (conName == null) {
            return;
        }

        QueryPanel queryPanel = new QueryPanel();
        queryPanel.setConfig(DbConfigFile.getConfig(conName));
        MainWindow.getInstance().addNewEditorTab(queryPanel, "New query");
        queryPanel.setResultsComponent(new JPanel());
        lastOpenedQueryPanel = queryPanel;
    }

    public QueryPanel getLastOpenedQueryPanel() {
        return lastOpenedQueryPanel;
    }
}