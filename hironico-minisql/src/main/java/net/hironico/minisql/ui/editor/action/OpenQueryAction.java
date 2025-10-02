package net.hironico.minisql.ui.editor.action;

import java.awt.event.ActionEvent;
import java.awt.Component;

import javax.swing.JFileChooser;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

public class OpenQueryAction extends AbstractRibbonAction {

    public OpenQueryAction()  {
        super("Open...", "icons8_opened_folder_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        QueryPanel queryPanel = null;
        if (!(comp instanceof QueryPanel)) {
            queryPanel = new QueryPanel();
            MainWindow.getInstance().displayCloseableComponent(queryPanel, "New Query");
        } else {
            queryPanel = (QueryPanel)comp;
        }

        JFileChooser chooser = new JFileChooser(queryPanel.getLastUserDirectory());
        int resp = chooser.showOpenDialog(queryPanel);
        if (resp == JFileChooser.APPROVE_OPTION) {
            queryPanel.loadFile(chooser.getSelectedFile());
        }
    }
}