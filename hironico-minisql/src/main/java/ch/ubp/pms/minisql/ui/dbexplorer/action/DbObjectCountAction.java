package ch.ubp.pms.minisql.ui.dbexplorer.action;

import ch.ubp.pms.minisql.model.SQLObject;
import ch.ubp.pms.minisql.model.SQLObjectTypeEnum;
import ch.ubp.pms.minisql.ui.ExecuteQueryAction;
import ch.ubp.pms.minisql.ui.MainWindow;
import ch.ubp.pms.minisql.ui.QueryPanel;
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