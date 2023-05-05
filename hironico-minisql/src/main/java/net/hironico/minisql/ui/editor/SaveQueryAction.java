package net.hironico.minisql.ui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SaveQueryAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 1L;

    public SaveQueryAction() {
        super("Save..." , "icons8_save_as_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        QueryPanel queryPanel = null;
        if ((comp == null) || !(comp instanceof QueryPanel)) {
            return;
        } else {
            queryPanel = (QueryPanel)comp;
        }

        JFileChooser chooser = new JFileChooser(queryPanel.getLastUserDirectory());
        int resp = chooser.showSaveDialog(queryPanel);
        if (resp == JFileChooser.APPROVE_OPTION) {
            String lastDir = chooser.getSelectedFile().getAbsolutePath();
            queryPanel.setLastUsedDirectory(lastDir);

            File saveFile = chooser.getSelectedFile();
            if (saveFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(queryPanel, "File exists. Overwrite ?", "Confirm...",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
                bw.write(queryPanel.getTxtQuery().getText());
                bw.flush();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(queryPanel, "Error while writing to the file:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}