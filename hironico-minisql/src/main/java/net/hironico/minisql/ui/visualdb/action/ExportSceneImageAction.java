package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * Base class for exporting visual database scenes to images.
 * This abstract class provides common functionality for actions that export
 * the current database graph scene to various image formats, either to the
 * clipboard or to a user-selected file.
 * 
 * <p>Subclasses should implement the specific destination logic (clipboard or file)
 * while inheriting the common image generation functionality.</p>
 * 
 * @author hironico
 * @since 2.1.0
 */
public abstract class ExportSceneImageAction extends AbstractSceneAction {
    
    /**
     * Logger for this class and its subclasses.
     */
    protected static final Logger logger = Logger.getLogger(ExportSceneImageAction.class.getName());

    /**
     * The database graph scene that will be exported to an image.
     * This field is set by subclasses when performing the export action.
     */
    protected DBGraphScene graphScene;

    /**
     * Constructs a new ExportSceneImageAction with the specified name and icon.
     * 
     * @param name the display name for this export action
     * @param icon the icon resource path for this export action
     */
    public ExportSceneImageAction(String name, String icon) {
        super(name, icon);
    }

    /**
     * Captures the current database graph scene as a BufferedImage.
     * 
     * <p>This method creates an image representation of the current scene by
     * rendering the graph scene's view onto a BufferedImage with 4-byte ABGR
     * color format. The image dimensions match the current size of the scene view.</p>
     * 
     * @return a BufferedImage containing the rendered scene data
     * @since 2.1.0
     */
    protected BufferedImage getSceneImage() {
        JComponent view = graphScene.getView();
        Dimension dim = view.getSize();
        BufferedImage buffImage = new BufferedImage(dim.width, dim.height,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = buffImage.createGraphics();
        graphScene.paint(graphics);
        graphics.dispose();
        return buffImage;
    }
}
