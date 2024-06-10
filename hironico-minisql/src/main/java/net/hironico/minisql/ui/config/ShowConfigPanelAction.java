package net.hironico.minisql.ui.config;

import java.awt.event.ActionEvent;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;

public class ShowConfigPanelAction extends AbstractRibbonAction {

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