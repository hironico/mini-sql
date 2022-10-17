package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;

import java.awt.event.ActionEvent;

public class DbObjectCollapseAllAction extends AbstractDbExplorerAction {
    public DbObjectCollapseAllAction() {
        super("Collapse all", null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SchemaExplorerPanel explorerPanel = MainWindow.getInstance().getSchemaExcplorerPanel();
        explorerPanel.collapseAll();
    }
}
