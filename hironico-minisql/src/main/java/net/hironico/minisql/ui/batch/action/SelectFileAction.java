package net.hironico.minisql.ui.batch.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.batch.BatchPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class SelectFileAction extends AbstractRibbonAction {

    public SelectFileAction() {
        super("Select file", "icons8-add-file-64.png");
    }

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
