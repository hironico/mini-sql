package net.hironico.minisql.ui.editor.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;

/**
 * Action for saving SQL query content to a file.
 * This action opens a file chooser save dialog allowing users to specify
 * a location and filename to save the current query editor content.
 */
public class SaveQueryAction extends AbstractRibbonAction {

    /** Serial version UID for object serialization */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new SaveQueryAction.
     * Sets the action name to "Save..." and uses the save as icon.
     */
    public SaveQueryAction() {
        super("Save..." , "icons8_save_as_64px.png");
    }

    /**
     * Executes the save query action when triggered.
     * Opens a file chooser save dialog in the last used directory. If a query panel
     * is active, saves the query content to the selected file location.
     *
     * @param evt the action event that triggered this action
     */
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
