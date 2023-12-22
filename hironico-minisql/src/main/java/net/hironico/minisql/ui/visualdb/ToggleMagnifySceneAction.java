package net.hironico.minisql.ui.visualdb;

import org.netbeans.api.visual.widget.BirdViewController;

public class ToggleMagnifySceneAction extends ExportSceneImageAction {
    private BirdViewController currentBirdView = null;

    public ToggleMagnifySceneAction() {
        super("Magnify", "icons8_search_64px.png");
        putValue(SHORT_DESCRIPTION, "When zoomed out, use to magnify view to quickly find a table");
    }

    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
        if (currentBirdView == null) {
            currentBirdView = graphScene.createBirdView();
            currentBirdView.setZoomFactor(1.0d);
            currentBirdView.show();
        } else {
            currentBirdView.hide();
            currentBirdView = null;
        }
    }
}
