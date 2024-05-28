package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;

public class ZoomMinusSceneAction extends AbstractSceneAction {
    public ZoomMinusSceneAction() {
        super("Zoom -", "icons8_zoom_out_64px.png");
    }

    @Override
    public void performSceneAction(DBGraphScene graphScene) {
        graphScene.zoomMinus();
    }
}
