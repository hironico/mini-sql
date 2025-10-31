package net.hironico.minisql.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

/**
 * Action implementation for exiting the application.
 * This action triggers the application shutdown process by posting a window closing event
 * to the system event queue, which allows proper cleanup and configuration saving.
 */
public class ExitAction extends AbstractRibbonAction {
    private static final long serialVersionUID = 1L;

    /** The name identifier for this action */
    public static final String NAME = "Exit";

    /**
     * Constructs a new ExitAction with default name and icon.
     * Uses the close window icon for visual representation.
     */
    public ExitAction() {
        super(NAME, "icons8_close_window_64px_2.png");
    }

    /**
     * Executes the exit action when triggered.
     * Posts a window closing event to the system event queue, which triggers
     * the proper application shutdown sequence including configuration saving.
     * 
     * Note: This method does not manually close the window or exit the application.
     * The window listener handles the actual shutdown process and saves configuration
     * before exiting.
     * 
     * @param arg0 the action event that triggered this exit action
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {

        WindowEvent wev = new WindowEvent(MainWindow.getInstance(), WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);

        /**
         * DO NOT manually close the window nor exiting here.
         * Window listener will do this for you as well as saving the config before exiting.
         */
	}
}
