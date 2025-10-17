package net.hironico.minisql.ui.editor.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

public class CutAction extends AbstractRibbonAction {

    public CutAction() {
        super("Cut", "icons8_cut_64px_1.png");
    }

    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        queryPanel.getTxtQuery().cut();
    }
}
