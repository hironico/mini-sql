package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.ui.MainWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DbObjectShowSystAction extends AbstractDbExplorerAction {

    public DbObjectShowSystAction() {
        super("Show System Objects", null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JCheckBox chk = (JCheckBox)e.getSource();
        MainWindow.getInstance().getSchemaExcplorerPanel().setShowSystemObjects(chk.isSelected());
    }
}
