package net.hironico.minisql.ui.visualdb.action;

import net.hironico.minisql.ui.visualdb.DBGraphScene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Action that exports the visual database scene image to a PNG file.
 * This action captures the current database graph scene and allows the user
 * to save it as a PNG image file through a file selection dialog.
 * 
 * <p>The action provides a file chooser with PNG file filtering and automatically
 * appends the .png extension if not provided by the user. Error handling is
 * included to notify users of any save failures.</p>
 * 
 * @author hironico
 * @since 2.1.0
 */
public class ExportSceneImageToFileAction extends ExportSceneImageAction {

    /**
     * Constructs a new ExportSceneImageToFileAction with default title and icon.
     * The action is initialized with the title "To File" and uses the
     * PNG icon to represent the file export functionality.
     */
    public ExportSceneImageToFileAction() {
        super("To File", "icons8_png_64px.png");
    }

    /**
     * Performs the file export action on the given database graph scene.
     * 
     * <p>This method captures the current scene as an image using the inherited
     * {@link #getSceneImage()} method and displays a file chooser dialog for the user
     * to select a save location. The file chooser is configured with PNG file filtering
     * and automatically appends the .png extension if not provided by the user.</p>
     * 
     * <p>After successful save, an information dialog confirms the operation. If any
     * errors occur during the save process, an error dialog is displayed with the
     * exception message.</p>
     * 
     * @param graphScene the database graph scene to export to a PNG file
     */
    @Override
    public void performSceneAction(DBGraphScene graphScene) {
        BufferedImage buffImage = getSceneImage();

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return (f != null) && (f.getName().toLowerCase().endsWith(".png") || f.isDirectory());
            }

            @Override
            public String getDescription() {
                return "PNG files";
            }

        });
        chooser.setAcceptAllFileFilterUsed(true);
        int ret = chooser.showSaveDialog(graphScene.getView());
        if (ret != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png"))
            file = new File(file.getName() + ".png");

        try {
            ImageIO.write(buffImage, "PNG", file);
            JOptionPane.showMessageDialog(graphScene.getView(),
                    "Image saved into:\n" + file.getName(),
                    "Yeah...",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot export image data into file.", ex);
            JOptionPane.showMessageDialog(graphScene.getView(),
                    "Cannot write image data into the selected file.\n" + ex.getMessage(),
                    "Ohoh...",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
