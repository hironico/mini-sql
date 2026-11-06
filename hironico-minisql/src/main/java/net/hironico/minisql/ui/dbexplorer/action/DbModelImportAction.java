package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.dbexplorer.ModelImportPanel;

import java.awt.event.ActionEvent;

/**
 * Opens the model import panel used to re-create, into a target connection, the tables of a
 * JSON model written by the "Export Model" action.
 */
public class DbModelImportAction extends AbstractDbExplorerAction {

    public DbModelImportAction() {
        super("Import Model", "icons8_import_64.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ModelImportPanel panel = new ModelImportPanel();
        MainWindow.getInstance().addNewEditorTab(panel, "Import Model");
    }
}
