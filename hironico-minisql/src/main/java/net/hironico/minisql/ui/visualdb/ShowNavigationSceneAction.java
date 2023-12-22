package net.hironico.minisql.ui.visualdb;

public class ShowNavigationSceneAction extends AbstractSceneAction {
    public ShowNavigationSceneAction() {
        super("Navigation", "icons8_binoculars_64px.png");
    }

    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
        super.visualDbPanel.showNavigation();
    }
}
