package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.dataexport.DataExportPanel;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Opens the data export wizard in a new editor tab for the table or view selected in the object explorer.
 * The data is streamed from the database to a file (CSV, JSON or SQL INSERT).
 */
public class DbObjectExportDataAction extends AbstractDbExplorerAction {

    public static final String NAME = "Export data";

    public DbObjectExportDataAction() {
        super(NAME, "icons8_export_csv_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SQLObject obj = this.getExplorerPanel().getSelectedSQLObject();
        if (obj == null) {
            return;
        }

        DbConfig cfg = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (cfg == null || rejectIfMongoDB(cfg)) {
            return;
        }

        if (obj.type == null || !SQLObjectTypeEnum.isTableOrView(obj.type)) {
            JOptionPane.showMessageDialog(MainWindow.getInstance(),
                    "Please select a table or a view in the explorer to export its data.",
                    NAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MainWindow.getInstance().addNewEditorTab(new DataExportPanel(cfg, obj), NAME + " " + obj.name);
    }
}
