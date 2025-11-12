package net.hironico.minisql.ui.editor.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Action for copying selected text from the query editor to the clipboard.
 * This action copies the currently selected text (or the entire content if nothing
 * is selected) from the SQL editor to the system clipboard.
 */
public class CopyAction extends AbstractRibbonAction {

    /**
     * Constructs a new CopyAction.
     * Sets the action name to "Copy" and uses the copy icon.
     */
    public CopyAction() {
        super("Copy", "icons8_copy_64px_2.png");
    }

    /**
     * Executes the copy action when triggered.
     * Copies the selected text from the current query panel's editor to the clipboard.
     *
     * @param evt the action event that triggered this action
     */
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        queryPanel.getTxtQuery().copy();
    }
}
