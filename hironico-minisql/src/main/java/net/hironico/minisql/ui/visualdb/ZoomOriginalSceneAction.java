package net.hironico.minisql.ui.visualdb;

public class ZoomOriginalSceneAction extends AbstractSceneAction {
    public ZoomOriginalSceneAction() {
        super("Zoom 1:1", "icons8_zoom_to_actual_size_64px.png");
    }

    @Override
    public void performSceneAction(DBGraphScene graphScene) {
        graphScene.zoomOriginal();
    }
}
