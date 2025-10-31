package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;

/**
 * Base class for ribbon actions of the Db Explorer Panel
 */
public abstract class AbstractDbExplorerAction extends AbstractRibbonAction {

    /**
     * Creates a new action with name and icon
     * @param name is the name of the action acting as a label for the ui
     * @param iconName name of the icon file to use for ribbon button. can be null
     */
    public AbstractDbExplorerAction(String name, String iconName) {
        super(name, iconName);
    }

    /**
     *
     * @return the Schema explorer panel from the main window
     */
    protected SchemaExplorerPanel getExplorerPanel() {
        return MainWindow.getInstance().getSchemaExplorerPanel();
    }

    /**
     * Display a new query panel tab in the editors tab
     * @return the newly created QueryPanel
     */
    protected QueryPanel showNewQueryPanel() {
        String title = this.getExplorerPanel().getSelectedConnectionName();

        QueryPanel pnl = new QueryPanel();
        DbConfig cfg = DbConfigFile.getConfig(title);
        pnl.setConfig(cfg);

        MainWindow.getInstance().addNewEditorTab(pnl, title);

        return pnl;
    }

}