package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;

public abstract class AbstractDbExplorerAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 1L;

    public AbstractDbExplorerAction(String name, String iconName) {
        super(name, iconName);
    }

    protected SchemaExplorerPanel getExplorerPanel() {
        return MainWindow.getInstance().getSchemaExcplorerPanel();
    }

    protected QueryPanel showNewQueryPanel() {
        String title = this.getExplorerPanel().getSelectedConnectionName();

        QueryPanel pnl = new QueryPanel();
        DbConfig cfg = DbConfigFile.getConfig(title);
        pnl.setConfig(cfg);

        MainWindow.getInstance().addNewEditorTab(pnl, title);

        return pnl;
    }

}