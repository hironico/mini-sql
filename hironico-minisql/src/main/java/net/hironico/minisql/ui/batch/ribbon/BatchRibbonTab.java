package net.hironico.minisql.ui.batch.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.ui.batch.action.*;

/**
 * Ribbon tab containing all batch execution actions for the batch panel.
 * This tab provides organized access to file management and execution actions
 * for batch processing of SQL files, arranged in logical groups for better usability.
 */
public class BatchRibbonTab extends RibbonTab {

    /** Ribbon group containing file selection and management actions */
    private RibbonGroup fileSelectionGroup = null;

    /** Ribbon group containing execution-related actions */
    private RibbonGroup executionGroup = null;

    /**
     * Constructs a new BatchRibbonTab with all batch-related actions.
     * Initializes the ribbon tab with "Batch" title and adds the file selection
     * and execution action groups.
     */
    public BatchRibbonTab() {
        super("Batch");
        addGroup(getFileSelectionGroup());
        addGroup(getExecutionGroup());
    }

    /**
     * Gets or creates the file selection ribbon group.
     * Contains actions for adding/removing files and folders to/from the batch.
     *
     * @return the RibbonGroup containing file selection actions
     */
    private RibbonGroup getFileSelectionGroup() {
        if (fileSelectionGroup == null) {
            fileSelectionGroup = new RibbonGroup("Files selection");

            fileSelectionGroup.addButton(new AddFolderBatchAction(), RibbonGroup.SMALL);
            fileSelectionGroup.addButton(new AddFileBatchAction(), RibbonGroup.SMALL);
            fileSelectionGroup.addButton(new RemoveBatchAction(), RibbonGroup.SMALL);
            fileSelectionGroup.addButton(new ClearFilesAction(), RibbonGroup.LARGE);
        }

        return fileSelectionGroup;
    }

    /**
     * Gets or creates the execution ribbon group.
     * Contains actions for executing batches and resetting results.
     *
     * @return the RibbonGroup containing execution actions
     */
    private RibbonGroup getExecutionGroup() {
        if (executionGroup == null) {
            executionGroup = new RibbonGroup("Execution");

            executionGroup.addButton(new ResetResultsAction(), RibbonGroup.LARGE);
            executionGroup.addButton(new RunBatchAction(), RibbonGroup.LARGE);
        }

        return executionGroup;
    }
}
