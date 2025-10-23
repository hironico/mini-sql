package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.ui.MainWindow;

import java.awt.event.ActionEvent;

public class DbObjectRefreshAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "Refresh";

    public DbObjectRefreshAction() {
        super(NAME, "icons8_synchronize_64px_3.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().getSchemaExplorerPanel().refreshSelectedObject();
    }
}