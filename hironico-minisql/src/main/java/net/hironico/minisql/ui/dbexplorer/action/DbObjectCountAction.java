package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.event.ActionEvent;

public class DbObjectCountAction extends AbstractDbExplorerAction {

    public static final String NAME = "Count";

    private static final String countQuery = "SELECT count(*) FROM %s.%s";
    private static final String countQueryQuoted = "SELECT count(*) from \"%s\".\"%s\"";

    public DbObjectCountAction() {
        super(NAME, "icons8_counter_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SQLObject obj = this.getExplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }

        final DbConfig configToUse = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (configToUse == null) {
            return;
        }

        QueryPanel pnl = this.showNewQueryPanel();

        if (SQLObjectTypeEnum.isTableOrView(obj.type)) {
            String query = String.format(getCountQuery(configToUse.useQuotedIdentifiers), obj.schemaName, obj.name);
            pnl.setQueryText(query);
            pnl.setDividerLocation(0.25d);
            ExecuteQueryAction.executeQueryAsync(pnl);
        }
    }

    private String getCountQuery(boolean useQuotedIdentifiers) {
        return useQuotedIdentifiers ? countQueryQuoted : countQuery;
    }
}