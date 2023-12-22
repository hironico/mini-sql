package net.hironico.minisql.ui.visualdb;

import javax.swing.*;

public class AutoLayoutSceneAction extends AbstractSceneAction {

    public AutoLayoutSceneAction() {
        super("Auto layout", "icons8_genealogy_64px.png");
    }
    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
        if (graphScene.getEdges().size() == 0) {
            int confirm = JOptionPane.showConfirmDialog(graphScene.getView(),
                    "There is no foreign key defined in this model.\n"
                            + "Tables will be laid out horizontaly.\n"
                            + "For a large number of table this could not be optimal.\n"
                            + "Do the layout anyway ?",
                    "Warning...",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        SwingUtilities.invokeLater(() -> {
            graphScene.layoutScene();
            graphScene.revalidate ();
            graphScene.validate ();
        });
    }
}
