package net.hironico.minisql.ui.config;

import java.awt.event.ActionEvent;

import net.hironico.minisql.ui.MainWindow;
import net.hironico.common.swing.ribbon.AbstractRibbonAction;

/**
 * Action for displaying the configuration panel in the main application window.
 * This action opens or switches to the configuration tab where users can modify
 * various application settings including database connections, drivers, and general preferences.
 */
public class ShowConfigPanelAction extends AbstractRibbonAction {

    /** The name identifier for this action */
    public static final String NAME = "Config";

    /**
     * Constructs a new ShowConfigPanelAction.
     * Sets the action name to "Config" and uses the services icon for visual representation.
     */
    public ShowConfigPanelAction() {
        super(NAME, "icons8_services_64px_2.png");
    }

    /**
     * Executes the show configuration action when triggered.
     * Checks if a configuration tab is already open and selects it, otherwise creates a new configuration panel tab.
     *
     * @param evt the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!MainWindow.getInstance().hasOneEditorNamed("Config")) {
            MainWindow.getInstance().addNewEditorTab(new ConfigPanel(), "Config");
        } else {
            MainWindow.getInstance().setSelectedEditor("Config");
        }
    }
}
