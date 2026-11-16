package net.hironico.minisql.ui.dbexplorer.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.ui.dbexplorer.action.*;

public class DbExplorerRibbonTab extends RibbonTab {
    private RibbonGroup viewRibbonGroup = null;
    private RibbonGroup objectsRibbonGroup = null;
    private RibbonGroup selectRibbonGroup = null;
    private RibbonGroup dataRibbonGroup = null;

    public DbExplorerRibbonTab() {
        super("Explorer");
        addGroup(getViewRibbonGroup());
        addGroup(getObjectsRibbonGroup());
        addGroup(getSelectRibbonGroup());
        addGroup(getDataRibbonGroup());
    }

    private RibbonGroup getDataRibbonGroup() {
        if (this.dataRibbonGroup == null) {
            this.dataRibbonGroup = new RibbonGroup("Data");

            this.dataRibbonGroup.addButton(new DbObjectExportDataAction(), RibbonGroup.LARGE);
        }

        return this.dataRibbonGroup;
    }

    private RibbonGroup getViewRibbonGroup() {
        if (this.viewRibbonGroup == null) {
            this.viewRibbonGroup = new RibbonGroup("View");

            DbObjectExpandAllAction expandAction = new DbObjectExpandAllAction();
            this.viewRibbonGroup.addButton(expandAction, RibbonGroup.SMALL);

            DbObjectCollapseAllAction collapseAction = new DbObjectCollapseAllAction();
            this.viewRibbonGroup.addButton(collapseAction, RibbonGroup.SMALL);
        }

        return this.viewRibbonGroup;
    }

    private RibbonGroup getObjectsRibbonGroup() {
        if (this.objectsRibbonGroup == null) {
            this.objectsRibbonGroup = new RibbonGroup("Objects");

            DbObjectRefreshAction refreshAction = new DbObjectRefreshAction();
            this.objectsRibbonGroup.addButton(refreshAction, RibbonGroup.LARGE);

            DbObjectShowSystAction systObjAction = new DbObjectShowSystAction();
            this.objectsRibbonGroup.addCheckBox(systObjAction);

            DbObjectStructureAction structureAction = new DbObjectStructureAction();
            this.objectsRibbonGroup.addButton(structureAction, RibbonGroup.LARGE);

            this.objectsRibbonGroup.addButton(new DbObjectExportStructureAction(), RibbonGroup.SMALL);
            this.objectsRibbonGroup.addButton(new DbModelImportAction(), RibbonGroup.SMALL);
        }

        return this.objectsRibbonGroup;
    }

    private RibbonGroup getSelectRibbonGroup() {
        if (this.selectRibbonGroup == null) {
            this.selectRibbonGroup = new RibbonGroup("Select...");

            this.selectRibbonGroup.addButton(new DbObjectSelect1kAction(), RibbonGroup.LARGE);
            this.selectRibbonGroup.addButton(new DbObjectCountAction(), RibbonGroup.LARGE);
        }

        return selectRibbonGroup;
    }
}
