package net.hironico.minisql.ui.editor;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class RedoAction extends AbstractRibbonAction {

    private static final Logger LOGGER = Logger.getLogger(RedoAction.class.getName());

    public RedoAction() {
        super("Redo", "icons8_redo_64px.png");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        if (queryPanel.getTxtQuery().canRedo()) {
            queryPanel.getTxtQuery().redoLastAction();
        } else {
            LOGGER.warning("Last action cannot be redone.");
        }
    }
}
