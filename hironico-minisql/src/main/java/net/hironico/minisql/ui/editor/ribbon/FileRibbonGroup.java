package net.hironico.minisql.ui.editor.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.minisql.ui.ShowQueryPanelAction;
import net.hironico.minisql.ui.editor.action.OpenQueryAction;
import net.hironico.minisql.ui.editor.action.SaveQueryAction;

import static net.hironico.minisql.ui.MainWindow.appActions;

public class FileRibbonGroup extends RibbonGroup {
    public FileRibbonGroup() {
        super("File");
    }

    @Override
    public void initialize() {
        super.initialize();
        this.addButton(appActions.get(ShowQueryPanelAction.NAME), RibbonGroup.LARGE);
        this.addButton(new OpenQueryAction(), RibbonGroup.SMALL);
        this.addButton(new SaveQueryAction(), RibbonGroup.SMALL);
    }
}
