package net.hironico.minisql.ui.dataimport.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.dataimport.DataImportPanel;

import java.awt.event.ActionEvent;

/**
 * Action for opening the CSV data import wizard in a new editor tab.
 * The wizard lets the user load a CSV file into a new table of the selected connection.
 */
public class ShowDataImportPanelAction extends AbstractRibbonAction {

    /** The name identifier for this action */
    public static final String NAME = "Import CSV";

    /**
     * Constructs a new ShowDataImportPanelAction with the data sheet icon.
     */
    public ShowDataImportPanelAction() {
        super(NAME, "icons8_data_sheet_64px.png");
    }

    /**
     * Opens a new data import wizard tab.
     *
     * @param e the action event that triggered this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().addNewEditorTab(new DataImportPanel(), NAME);
    }
}
