package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;
import org.netbeans.api.visual.widget.BirdViewController;

public class ToggleMagnifySceneAction extends AbstractSceneAction {
    private BirdViewController currentBirdView = null;

    public ToggleMagnifySceneAction() {
        super("Magnify", "icons8_search_64px.png");
    }

    @Override
    public void performSceneAction(DBGraphScene graphScene) {
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
