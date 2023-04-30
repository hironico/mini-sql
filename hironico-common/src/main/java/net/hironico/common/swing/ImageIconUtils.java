package net.hironico.common.swing;

import javax.swing.*;
import java.awt.*;

public class ImageIconUtils {
    private static final ImageIconUtils instance = new ImageIconUtils();

    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = instance.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
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
