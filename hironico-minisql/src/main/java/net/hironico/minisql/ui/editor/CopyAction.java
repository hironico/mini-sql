package net.hironico.minisql.ui.editor;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.ExecuteQueryAction;
import net.hironico.minisql.ui.MainWindow;

import java.awt.*;
import java.awt.event.ActionEvent;

public class CopyAction extends AbstractRibbonAction {

    public CopyAction() {
        super("Copy", "icons8_copy_64px_2.png");
    }

    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        queryPanel.getTxtQuery().copy();
    }
}
