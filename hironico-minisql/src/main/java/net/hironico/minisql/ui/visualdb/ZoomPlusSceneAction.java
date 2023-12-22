package net.hironico.minisql.ui.visualdb;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

import java.awt.*;
import java.awt.event.ActionEvent;

public class ZoomPlusSceneAction extends AbstractSceneAction {
    public ZoomPlusSceneAction() {
        super("Zoom +", "icons8_zoom_in_64px.png");
    }

    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
        graphScene.zoomPlus();
    }
}
