package net.hironico.minisql.ui.visualdb.action;

import net.hironico.common.swing.image.TransferableImage;
import net.hironico.minisql.ui.visualdb.DBGraphScene;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

/**
 * Action that exports the visual database scene image to the system clipboard.
 * This action captures the current database graph scene and places it on the
 * clipboard as an image, allowing users to paste it into other applications
 * such as word processors, image editors, or presentation software.
 * 
 * @author hironico
 * @since 2.1.0
 */
public class ExportSceneImageToClipboardAction extends ExportSceneImageAction {

    /**
     * Constructs a new ExportSceneImageToClipboardAction with default title and icon.
     * The action is initialized with the title "To clipboard" and uses the
     * clipboard list icon to represent the clipboard export functionality.
     */
    public ExportSceneImageToClipboardAction() {
        super("To clipboard", "icons8_clipboard_list_64px.png");
    } 

    /**
     * Performs the clipboard export action on the given database graph scene.
     * 
     * <p>This method captures the current scene as an image using the inherited
     * {@link #getSceneImage()} method, wraps it in a {@link TransferableImage},
     * and places it on the system clipboard. After successful placement,
     * an information dialog is displayed to confirm the action.</p>
     * 
     * @param graphScene the database graph scene to export to the clipboard
     */
    @Override
    public void performSceneAction(DBGraphScene graphScene) {
        BufferedImage img = getSceneImage();
        TransferableImage transferable = new TransferableImage(img);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);

        JOptionPane.showMessageDialog(graphScene.getView(),
                "Image copied into the clipboard.",
                "Yeah...",
                JOptionPane.INFORMATION_MESSAGE);
    }

}
