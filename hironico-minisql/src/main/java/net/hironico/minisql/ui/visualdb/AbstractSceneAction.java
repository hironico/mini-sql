package net.hironico.minisql.ui.visualdb;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractSceneAction extends AbstractRibbonAction {
    protected VisualDbPanel visualDbPanel = null;

    public AbstractSceneAction(String title, String icon) {
        super(title, icon);
    }
    @Override
    public final void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (comp instanceof VisualDbPanel) {
            visualDbPanel = (VisualDbPanel) comp;
            DBGraphScene graphScene = visualDbPanel.getGraphScene();
            perfomSceneAction(graphScene);
            visualDbPanel.repaint();
        }
    }

    public abstract void perfomSceneAction(DBGraphScene graphScene);
}
