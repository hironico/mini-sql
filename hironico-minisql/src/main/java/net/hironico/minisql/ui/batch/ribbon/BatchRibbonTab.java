package net.hironico.minisql.ui.batch.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.ui.batch.action.*;

import javax.swing.*;

/**
 * Ribbon tab to hold all the batch execution actions on the Batch Execution Tab
 */
public class BatchRibbonTab extends RibbonTab {
    private RibbonGroup fileSelectionGroup = null;
    private RibbonGroup executionGroup = null;

    public BatchRibbonTab() {
        super("Batch");
        addGroup(getFileSelectionGroup());
        addGroup(getExecutionGroup());
    }

    private RibbonGroup getFileSelectionGroup() {
        if (fileSelectionGroup == null) {
            fileSelectionGroup = new RibbonGroup("Files selection");

            fileSelectionGroup.addButton(new SelectFolderAction(), RibbonGroup.SMALL);
            fileSelectionGroup.addButton(new SelectFileAction(), RibbonGroup.SMALL);
            fileSelectionGroup.addButton(new ClearFilesAction(), RibbonGroup.LARGE);
        }

        return fileSelectionGroup;
    }

    private RibbonGroup getExecutionGroup() {
        if (executionGroup == null) {
            executionGroup = new RibbonGroup("Execution");

            executionGroup.addButton(new ResetResultsAction(), RibbonGroup.LARGE);
            executionGroup.addButton(new RunBatchAction(), RibbonGroup.LARGE);
        }

        return executionGroup;
    }
}
