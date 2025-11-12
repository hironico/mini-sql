package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Action for adding all SQL/text files from a selected folder to a batch panel.
 * This action opens a directory chooser dialog and recursively loads all
 * files from the selected directory into the current batch execution.
 */
public class AddFolderBatchAction extends AbstractRibbonAction {

    /**
     * Constructs a new AddFolderBatchAction.
     * Sets the action name to "Add folder" and uses the add folder icon.
     */
    public AddFolderBatchAction() {
        super("Add folder", "icons8-add-folder-64.png");
    }

    /**
     * Executes the add folder action when triggered.
     * Opens a directory chooser dialog allowing selection of a folder.
     * All files in the selected folder are added to the current batch panel for execution.
     *
     * @param e the action event that triggered this action
     */
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
