package net.hironico.minisql.ui.editor.action;

import java.awt.event.ActionEvent;
import java.awt.Component;

import javax.swing.JFileChooser;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

/**
 * Action for opening SQL query files in the query editor.
 * This action opens a file chooser dialog allowing users to select SQL files
 * to load into the current or a new query editor panel with syntax highlighting.
 */
public class OpenQueryAction extends AbstractRibbonAction {

    /**
     * Constructs a new OpenQueryAction.
     * Sets the action name to "Open..." and uses the opened folder icon.
     */
    public OpenQueryAction()  {
        super("Open...", "icons8_opened_folder_64px.png");
    }

    /**
     * Executes the open query action when triggered.
     * Opens a file chooser dialog in the last used directory. If no query panel
     * is currently active, creates a new one. Loads the selected file into the query editor.
     *
     * @param evt the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        QueryPanel queryPanel = null;
        if (!(comp instanceof QueryPanel)) {
            queryPanel = new QueryPanel();
            MainWindow.getInstance().addNewEditorTab(queryPanel, "New Query");
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
