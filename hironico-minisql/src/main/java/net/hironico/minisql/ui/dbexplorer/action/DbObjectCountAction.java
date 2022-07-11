package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.QueryPanel;

import java.awt.event.ActionEvent;

public class DbObjectCountAction extends AbstractDbExplorerAction {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "Count";

    public DbObjectCountAction() {
        super(NAME, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        QueryPanel pnl = showNewQueryPanel();

        SQLObject obj = MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }
        switch (SQLObjectTypeEnum.valueOf(obj.type)) {
        case TABLE:
        case VIEW:
            String query = "SELECT count(*) FROM " + obj.schemaName + "." + obj.name;
            pnl.setQueryText(query);
            pnl.setDividerLocation(0.25d);
            ExecuteQueryAction.executeQueryAsync(pnl);
            break;

        default:
            break;
        }
    }
}