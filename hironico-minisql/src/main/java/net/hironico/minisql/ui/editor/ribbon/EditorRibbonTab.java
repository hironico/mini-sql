package net.hironico.minisql.ui.editor.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.ui.ExecuteBatchQueryAction;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.editor.action.*;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class EditorRibbonTab extends RibbonTab {

    private static final Logger LOG = Logger.getLogger(EditorRibbonTab.class.getName());

    private RibbonGroup fileRibbonGroup = null;
    private RibbonGroup clipBoardRibbonGroup = null;
    private RibbonGroup undoRedoRibbonGroup = null;
    private RibbonGroup executeRibbonGroup = null;

    /**
     * Keep reference of the checkbox for batch mode execution.
     * it will be updated when editor tab selection changes.
     */
    private JCheckBox chkBatchMode = null;

    public EditorRibbonTab() {
        super("Editor");
        addGroup(getFileRibbonGroup());
        addGroup(getUndoRedoRibbonGroup());
        addGroup(getClipboardRibbonGroup());
        addGroup(getExecuteRibbonGroup());
    }

    private RibbonGroup getFileRibbonGroup() {
        if (fileRibbonGroup == null) {
            fileRibbonGroup = new FileRibbonGroup();
        }

        return fileRibbonGroup;
    }

    private RibbonGroup getClipboardRibbonGroup() {
        if (clipBoardRibbonGroup == null) {
            clipBoardRibbonGroup = new RibbonGroup("Clipboard");

            clipBoardRibbonGroup.addButton(new PasteAction(), RibbonGroup.LARGE);
            clipBoardRibbonGroup.addButton(new CopyAction(), RibbonGroup.SMALL);
            clipBoardRibbonGroup.addButton(new CutAction(), RibbonGroup.SMALL);
        }

        return clipBoardRibbonGroup;
    }

    private RibbonGroup getUndoRedoRibbonGroup() {
        if (undoRedoRibbonGroup == null) {
            undoRedoRibbonGroup = new RibbonGroup("Undo/Redo");

            undoRedoRibbonGroup.addButton(new UndoAction(), RibbonGroup.SMALL);
            undoRedoRibbonGroup.addButton(new RedoAction(), RibbonGroup.SMALL);
        }

        return undoRedoRibbonGroup;
    }

    private RibbonGroup getExecuteRibbonGroup() {
        if (executeRibbonGroup == null) {
            executeRibbonGroup = new RibbonGroup("Execute");

            executeRibbonGroup.addButton(new ExecuteQueryAction(), RibbonGroup.LARGE);
            chkBatchMode = executeRibbonGroup.addCheckBox(new ExecuteBatchQueryAction());
            executeRibbonGroup.addButton(new CheckSQLAction(), RibbonGroup.LARGE);
        }
        return executeRibbonGroup;
    }

    /**
     * Updates the display of the checkbox representing the batch mode of the current query editor.
     */
    @Override
    public void updateDisplay() {
        super.updateDisplay();
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (comp instanceof QueryPanel queryPanel) {
            if (chkBatchMode != null) {
                chkBatchMode.setSelected(queryPanel.isBatchMode());
            }
        }
    }
}
