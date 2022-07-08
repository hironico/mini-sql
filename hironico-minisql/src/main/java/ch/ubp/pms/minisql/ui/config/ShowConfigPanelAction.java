package ch.ubp.pms.minisql.ui.config;

import java.awt.event.ActionEvent;

import ch.ubp.pms.minisql.ui.MainWindow;
import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;

public class ShowConfigPanelAction extends AbstractRibbonAction {

    private static final long serialVersionUID = 2881804828604120909L;

    public static final String NAME = "Config";

    public ShowConfigPanelAction() {
        super(NAME, "icons8_services_64px_2.png");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!MainWindow.getInstance().hasOneEditorNamed("Config")) {
            MainWindow.getInstance().displayCloseableComponent(new ConfigPanel(), "Config");
        } else {
            MainWindow.getInstance().setSelectedEditor("Config");
        }
    }
}