package net.hironico.minisql.ui.editor.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

public class PasteAction extends AbstractRibbonAction {

    public PasteAction() {
        super("Paste", "icons8_paste_64px.png");
    }

    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        QueryPanel queryPanel = (QueryPanel)comp;
        queryPanel.getTxtQuery().paste();
    }
}
