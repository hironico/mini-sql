package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Select a folder and load all the files in this folder into the batch execution tab
 */
public class SelectFolderAction extends AbstractRibbonAction {

    public SelectFolderAction() {
        super("Select folder", "icons8-add-folder-64.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (!(comp instanceof BatchPanel batchPanel)) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (batchPanel.getLastUsedFolder() != null) {
            chooser.setCurrentDirectory(batchPanel.getLastUsedFolder());
        }

        if (chooser.showOpenDialog(batchPanel) == JFileChooser.APPROVE_OPTION) {
            File newBatchFolder = chooser.getSelectedFile();
            batchPanel.loadDirectory(newBatchFolder);
        }
    }
}
