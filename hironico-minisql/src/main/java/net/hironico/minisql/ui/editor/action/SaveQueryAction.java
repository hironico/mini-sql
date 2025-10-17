package net.hironico.minisql.ui.editor.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

public class SaveQueryAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 1L;

    public SaveQueryAction() {
        super("Save..." , "icons8_save_as_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        QueryPanel queryPanel = null;
        if (!(comp instanceof QueryPanel)) {
            return;
        } else {
            queryPanel = (QueryPanel)comp;
        }

        JFileChooser chooser = new JFileChooser(queryPanel.getLastUserDirectory());
        int resp = chooser.showSaveDialog(queryPanel);
        if (resp == JFileChooser.APPROVE_OPTION) {
            queryPanel.saveFile(chooser.getSelectedFile());
        }
    }
}