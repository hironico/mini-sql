package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;

public class ZoomPlusSceneAction extends AbstractSceneAction {
    public ZoomPlusSceneAction() {
        super("Zoom +", "icons8_zoom_in_64px.png");
    }

    @Override
    public void performSceneAction(DBGraphScene graphScene) {
        graphScene.zoomPlus();
    }
}
