package net.hironico.minisql.ui.visualdb.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.visualdb.DBGraphScene;
import net.hironico.minisql.ui.visualdb.VisualDbPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Abstract base class for all actions that operate on a visual database scene.
 * This class provides the common functionality for actions that need to interact
 * with the {@link DBGraphScene} in the currently active {@link VisualDbPanel}.
 * 
 * <p>Subclasses must implement the {@link #performSceneAction(DBGraphScene)} method
 * to define the specific action to be performed on the scene.</p>
 * 
 * @author hironico
 * @since 2.1.0
 */
public abstract class AbstractSceneAction extends AbstractRibbonAction {
    
    /**
     * The visual database panel that contains the scene this action operates on.
     * This field is set when the action is performed and the current tab component
     * is verified to be a {@link VisualDbPanel}.
     */
    protected VisualDbPanel visualDbPanel = null;

    /**
     * Constructs a new AbstractSceneAction with the specified title and icon.
     * 
     * @param title the display title for this action
     * @param icon the icon resource path for this action
     */
    public AbstractSceneAction(String title, String icon) {
        super(title, icon);
    }

    /**
     * Performs the action by retrieving the current visual database panel and
     * executing the scene-specific action.
     * 
     * <p>This method checks if the current editor tab component is a {@link VisualDbPanel},
     * and if so, sets the {@link #visualDbPanel} field and calls {@link #performSceneAction(DBGraphScene)}.</p>
     * 
     * @param e the action event that triggered this action
     */
    @Override
    public final void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentEditorTabComponent();
        if (comp instanceof VisualDbPanel) {
            visualDbPanel = (VisualDbPanel) comp;
            DBGraphScene graphScene = visualDbPanel.getGraphScene();
            performSceneAction(graphScene);
            visualDbPanel.repaint();
        }
    }

    /**
     * Performs the specific action on the given database graph scene.
     * Subclasses must implement this method to define their specific behavior.
     * 
     * @param graphScene the database graph scene to perform the action on
     */
    public abstract void performSceneAction(DBGraphScene graphScene);
}
