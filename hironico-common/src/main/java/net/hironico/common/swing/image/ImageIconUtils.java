package net.hironico.common.swing.image;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class ImageIconUtils {
    private static final Logger LOGGER = Logger.getLogger(ImageIconUtils.class.getName());

    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = ImageIconUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            LOGGER.severe(String.format("Couldn't find file: %s", path));
            return null;
        }
    }

    public static ImageIcon getScaledImage(ImageIcon imageIcon, int w, int h) {
        if (imageIcon == null) {
            return null;
        }
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg);  // transform it back
        return imageIcon;
    }
}
