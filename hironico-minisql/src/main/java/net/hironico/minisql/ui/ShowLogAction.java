package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.hironico.common.swing.log.LogPanel;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.DbConfigFile;

/**
 * Action implementation for displaying the application log panel.
 * This action opens a log viewer tab in the editor area for debugging and monitoring
 * application activity.
 */
public class ShowLogAction extends AbstractRibbonAction {

    /** The log panel instance for displaying application logs */
    private LogPanel logPanel = null;

    /**
     * Constructs a new ShowLogAction with default name and icon.
     * Sets the action name to "Log" and uses the pull-down icon for visual representation.
     */
    public ShowLogAction() {
        super("Log", "icons8_pull_down_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Open the log of the application for debugging purposes.");
    }

    /**
     * Gets or creates the log panel instance.
     * Uses lazy initialization to create the log panel only when needed.
     * 
     * @return the LogPanel instance for displaying application logs
     */
    protected LogPanel getLogPanel() {
        if (logPanel == null) {
            logPanel = new LogPanel();
        }

        // setup max number of rows all the time since it may have changed after log panel was created
        logPanel.setMaxRows(DbConfigFile.getInstance().getLogMaxRows());

        return logPanel;
    }

    /**
     * Displays the log panel when this action is triggered.
     * Checks if a log tab already exists and selects it, or creates a new one
     * if it doesn't exist.
     * 
     * @param arg0 the action event that triggered this log display action
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        MainWindow win = MainWindow.getInstance();
        int index = win.getEditorTabIndexOf("Log", true);
        
        if (index < 0) {
            win.addNewEditorTab(getLogPanel(), "Log");
        } 
	}

}
