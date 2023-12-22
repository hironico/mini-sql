package net.hironico.minisql.ui.visualdb;

public class ZoomMinusSceneAction extends AbstractSceneAction {
    public ZoomMinusSceneAction() {
        super("Zoom -", "icons8_zoom_out_64px.png");
    }

    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
        graphScene.zoomMinus();
    }
}
