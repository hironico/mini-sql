package net.hironico.minisql.ui.editor.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class UndoAction extends AbstractRibbonAction {
    private static final Logger LOGGER = Logger.getLogger(UndoAction.class.getName());

    public UndoAction() {
        super("Undo", "icons8_undo_64px.png");
    }

    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        if (queryPanel.getTxtQuery().canUndo()) {
            queryPanel.getTxtQuery().undoLastAction();
        } else {
            LOGGER.warning("Last action cannot be undone.");
        }
    }
}
