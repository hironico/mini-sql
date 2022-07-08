package ch.ubp.pms.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ch.ubp.pms.swing.log.LogPanel;
import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;

public class ShowLogAction extends AbstractRibbonAction {

    private static final long serialVersionUID = -1L;

    private LogPanel logPanel = null;

    public ShowLogAction() {
        super("Log", "icons8_pull_down_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Open the log of the application for debugging purposes.");
    }

    protected LogPanel getLogPanel() {
        if (logPanel == null) {
            logPanel = new LogPanel();
        }

        return logPanel;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        MainWindow win = MainWindow.getInstance();
        int index = win.getTabIndexOfTitle("Log", true);
        
        if (index < 0) {
            win.displayCloseableComponent(getLogPanel(), "Log");
        } 
	}

}