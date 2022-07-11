package net.hironico.common.swing.ribbon;

import java.awt.Image;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public abstract class AbstractRibbonAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private static final String BASE_ICON_PATH = "/icons/png_64/";

    public AbstractRibbonAction(String name) {
        super(name);
    }

    public AbstractRibbonAction(String name, String iconName) {
        super(name);        
        if (iconName != null) {
            String fullIconName = String.format("%s%s", BASE_ICON_PATH, iconName);
            ImageIcon icon = this.createImageIcon(fullIconName, "");
            if (icon != null) {
                putValue(AbstractAction.LARGE_ICON_KEY, getScaledImage(icon, 32, 32));
                putValue(AbstractAction.SMALL_ICON, getScaledImage(icon, 16, 16));
            }
        }
    }

    public ImageIcon getSmallIcon() {
        return (ImageIcon)getValue(SMALL_ICON);
    }

    public ImageIcon getLargeIcon() {
        return (ImageIcon)getValue(LARGE_ICON_KEY);
    }

    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private ImageIcon getScaledImage(ImageIcon imageIcon, int w, int h) {
        if (imageIcon == null) {
            return null;
        }
        Image image = imageIcon.getImage(); // transform it 
        Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        imageIcon = new ImageIcon(newimg);  // transform it back
        return imageIcon;
    }
}