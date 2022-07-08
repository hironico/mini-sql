package ch.ubp.pms.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import ch.ubp.pms.minisql.DbConfigFile;
import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;

public class ShowQueryPanelAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 1L;

    public static final String NAME = "New query";

    public ShowQueryPanelAction() {
        super(NAME, "icons8_sql_64px_4.png");
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