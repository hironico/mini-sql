package net.hironico.minisql.ui.editor.ribbon;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;
import net.hironico.minisql.ui.ExecuteBatchQueryAction;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.editor.action.*;

public class EditorRibbonTab extends RibbonTab {

    private RibbonGroup fileRibbonGroup = null;
    private RibbonGroup clipBoardRibbonGroup = null;
    private RibbonGroup undoRedoRibbonGroup = null;
    private RibbonGroup executeRibbonGroup = null;

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
            executeRibbonGroup.addButton(new ExecuteBatchQueryAction(), RibbonGroup.LARGE);
            executeRibbonGroup.addButton(new CheckSQLAction(), RibbonGroup.LARGE);
        }
        return executeRibbonGroup;
    }
}
