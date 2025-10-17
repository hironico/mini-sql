package net.hironico.minisql.ui.visualdb.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.visualdb.DBGraphScene;
import net.hironico.minisql.ui.visualdb.VisualDbPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractSceneAction extends AbstractRibbonAction {
    protected VisualDbPanel visualDbPanel = null;

    public AbstractSceneAction(String title, String icon) {
        super(title, icon);
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (comp instanceof VisualDbPanel) {
            visualDbPanel = (VisualDbPanel) comp;
            DBGraphScene graphScene = visualDbPanel.getGraphScene();
            performSceneAction(graphScene);
            visualDbPanel.repaint();
        }
    }

    public abstract void performSceneAction(DBGraphScene graphScene);
}
