package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;

import java.awt.event.ActionEvent;

public class DbObjectExpandAllAction extends AbstractDbExplorerAction {
    public DbObjectExpandAllAction() {
        super("Expand all", "icons8_expand_64px.png");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        SchemaExplorerPanel pnl = MainWindow.getInstance().getSchemaExplorerPanel();
        pnl.expandAll();
    }
}
