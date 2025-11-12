package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Action for adding one or more SQL/text files to a batch panel.
 * This action opens a file chooser dialog that allows users to select
 * multiple SQL or text files to add to the current batch execution.
 */
public class AddFileBatchAction extends AbstractRibbonAction {

    /**
     * Constructs a new AddFileBatchAction.
     * Sets the action name to "Add file" and uses the add file icon.
     */
    public AddFileBatchAction() {
        super("Add file", "icons8-add-file-64.png");
    }

    /**
     * Executes the add file action when triggered.
     * Opens a file chooser dialog allowing selection of multiple SQL/text files.
     * The selected files are added to the current batch panel for execution.
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
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.resetChoosableFileFilters();
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".sql");
            }

            @Override
            public String getDescription() {
                return "SQL files";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Text files";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "All files";
            }
        });
        if (batchPanel.getLastUsedFolder() != null) {
            chooser.setCurrentDirectory(batchPanel.getLastUsedFolder());
        }

        if (chooser.showOpenDialog(batchPanel) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            batchPanel.loadFiles(files);
        }
    }
}
