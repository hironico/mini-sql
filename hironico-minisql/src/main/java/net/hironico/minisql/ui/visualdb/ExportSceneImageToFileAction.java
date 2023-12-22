package net.hironico.minisql.ui.visualdb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Permet de sauvegarder un graphique visual Db dans un fichier au fromat PNG.
 * @author hironico
 * @since 2.1.0
 */
public class ExportSceneImageToFileAction extends ExportSceneImageAction {

    public ExportSceneImageToFileAction() {
        super("Export to File", "icons8_png_64px.png");
    }

    @Override
    public void perfomSceneAction(DBGraphScene graphScene) {
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
