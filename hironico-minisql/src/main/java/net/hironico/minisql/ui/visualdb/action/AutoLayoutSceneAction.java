package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;

import javax.swing.*;

/**
 * Action that automatically arranges the layout of database objects in the visual scene.
 * This action provides an automatic layout algorithm that positions tables and their
 * relationships in an organized manner.
 * 
 * <p>When there are no foreign key relationships defined in the model, the action
 * will display a warning dialog asking for confirmation before proceeding with a
 * horizontal layout, which may not be optimal for large numbers of tables.</p>
 * 
 * @author hironico
 * @since 2.1.0
 */
public class AutoLayoutSceneAction extends AbstractSceneAction {

    /**
     * Constructs a new AutoLayoutSceneAction with default title and icon.
     * The action is initialized with the title "Auto layout" and uses the
     * genealogy icon to represent the layout functionality.
     */
    public AutoLayoutSceneAction() {
        super("Auto layout", "icons8_genealogy_64px.png");
    }
    
    /**
     * Performs the automatic layout action on the given database graph scene.
     * 
     * <p>This method first checks if there are any edges (foreign key relationships)
     * in the scene. If no edges are found, it displays a confirmation dialog warning
     * that tables will be laid out horizontally, which may not be optimal for large
     * numbers of tables.</p>
     * 
     * <p>If the user confirms or if edges exist, the method invokes the scene's
     * layout algorithm on the AWT event dispatch thread to ensure thread safety.</p>
     * 
     * @param graphScene the database graph scene to layout automatically
     */
    @Override
    public void performSceneAction(DBGraphScene graphScene) {
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
