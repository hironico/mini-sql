package net.hironico.minisql.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

public class ExitAction extends AbstractRibbonAction {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "Exit";

    public ExitAction() {
        super(NAME, "icons8_close_window_64px_2.png");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        WindowEvent wev = new WindowEvent(MainWindow.getInstance(), WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);

        /**
         * DO NIOT manually close the window nor exiting here.
         * Window listener will do this for you as well as saving the config before exiting.
         */
	}
}