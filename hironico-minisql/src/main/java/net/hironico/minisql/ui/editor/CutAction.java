package net.hironico.minisql.ui.editor;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

import java.awt.*;
import java.awt.event.ActionEvent;

public class CutAction extends AbstractRibbonAction {

    public CutAction() {
        super("Cut", "icons8_cut_64px_1.png");
    }

    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        queryPanel.getTxtQuery().cut();
    }
}
