package ch.ubp.pms.minisql.ui.dbexplorer.action;

import ch.ubp.pms.minisql.DbConfig;
import ch.ubp.pms.minisql.DbConfigFile;
import ch.ubp.pms.minisql.ui.MainWindow;
import ch.ubp.pms.minisql.ui.QueryPanel;
import ch.ubp.pms.minisql.ui.dbexplorer.SchemaExplorerPanel;
import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;

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

        MainWindow.getInstance().displayCloseableComponent(pnl, title);

        return pnl;
    }

}